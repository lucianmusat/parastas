package online.lucianmusat.Parastas.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.exception.NotFoundException;

import online.lucianmusat.Parastas.domain.entities.SmtpSettings;
import online.lucianmusat.Parastas.domain.repositories.SmtpSettingsRepository;
import online.lucianmusat.Parastas.domain.repositories.StateSettingsRepository;
import online.lucianmusat.Parastas.infrastructure.DockerContainer;
import online.lucianmusat.Parastas.domain.entities.StateSettings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.base.Strings;

@Service
public class DockerServiceImpl implements DockerService {

    private static final Logger logger = LogManager.getLogger(DockerServiceImpl.class);
    private final DockerClient dockerClient;
    private final EmailService emailService;
    private final StateSettingsRepository stateSettingsRepository;
    private final SmtpSettingsRepository smtpSettingsRepository;

    private final Map<String, Boolean> watchedContainers = new ConcurrentHashMap<>();
    private final Map<DockerContainer, Boolean> containers = new ConcurrentHashMap<>();
    private ScheduledExecutorService executor;
    private final AtomicInteger refreshPeriodSeconds = new AtomicInteger(60);
    private final int nrThreads = 1;

    @Autowired
    public DockerServiceImpl(DockerClient dockerClient, EmailService emailService, StateSettingsRepository stateSettingsRepository, SmtpSettingsRepository smtpSettingsRepository) {
        this.dockerClient = dockerClient;
        this.emailService = emailService;
        this.stateSettingsRepository = stateSettingsRepository;
        this.smtpSettingsRepository = smtpSettingsRepository;

        executor = Executors.newScheduledThreadPool(nrThreads);
        StateSettings stateSettings = stateSettingsRepository.findTopByOrderByIdAsc().orElse(new StateSettings());
        refreshPeriodSeconds.set(stateSettings.getRefreshPeriodSeconds());
        executor.scheduleAtFixedRate(watchContainers, 0, refreshPeriodSeconds.get(), TimeUnit.SECONDS);
    }

    public Map<DockerContainer, Boolean> listAllDockerContainers() {
        logger.debug("Listing all containers");
        Map<DockerContainer, Boolean> currentContainers = new HashMap<>();
        try {
            dockerClient.listContainersCmd().withShowAll(true).exec().forEach(container -> {
                currentContainers.put(new DockerContainer(container.getId(), container.getImage()), container.getState().equals("running"));
            });
        } catch (Exception e) {
            logger.error("Error while listing containers: {}", e.getMessage());
        }
        logger.debug("Found {} containers", currentContainers.size());

        // Update local cache
        this.containers.clear();
        this.containers.putAll(currentContainers);
        this.containers.entrySet().removeIf(entry -> entry.getKey().name().contains("parastas"));

        cleanWatchedContainers();
        updateWatchedContainers();

        return this.containers;
    }

    private void cleanWatchedContainers() {
        watchedContainers.keySet()
                .removeIf(containerId -> containers.keySet().stream()
                        .noneMatch(container -> container.id().equals(containerId)));
    }

    private void updateWatchedContainers() {
        containers.forEach((container, status) -> {
            if (!watchedContainers.containsKey(container.id())) {
                watchedContainers.put(container.id(), false);
            }
        });
    }

    @Override
    public Map<String, Boolean> getWatchedContainers() {
        return watchedContainers;
    }

    @Override
    public void toggleWatchedContainer(@Nonnull String containerId) {
        watchedContainers.put(containerId, !watchedContainers.getOrDefault(containerId, false));
    }

    @Override
    public void toggleAllWatchedContainers() {
        boolean hasTrueValue = watchedContainers.containsValue(true);
        boolean allFalse = !watchedContainers.containsValue(true);
        boolean allTrue = !watchedContainers.containsValue(false);

        if ((hasTrueValue && !allTrue) || allFalse) {
            watchedContainers.replaceAll((key, value) -> true);
        } else {
            watchedContainers.replaceAll((key, value) -> false);
        }
    }

    private void stopWatching() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Override
    public void updateExecutorSettings() {
        int newRefreshPeriodSeconds = stateSettingsRepository.findTopByOrderByIdAsc().orElse(new StateSettings()).getRefreshPeriodSeconds();
        if (newRefreshPeriodSeconds != refreshPeriodSeconds.get()) {
            logger.info("Updating refresh period to {} seconds", newRefreshPeriodSeconds);
            stopWatching();
            executor = Executors.newScheduledThreadPool(nrThreads);
            refreshPeriodSeconds.set(newRefreshPeriodSeconds);
            executor.scheduleAtFixedRate(watchContainers, 0, refreshPeriodSeconds.get(), TimeUnit.SECONDS);
        }
    }

    private void updateContainerStatus(String containerId, Boolean newStatus) {
        logger.debug("Updating container: {} status to: {}", containerId.substring(0, 12), newStatus);
        containers.entrySet()
                .stream()
                .filter(entry -> entry.getKey().id().equals(containerId))
                .findFirst()
                .ifPresent(entry -> containers.put(entry.getKey(), newStatus));
    }

    private Boolean getContainerStatus(final String containerId) {
        logger.debug("Getting container: {} status", containerId.substring(0, 12));
        return containers.entrySet()
                .stream()
                .filter(entry -> entry.getKey().id().equals(containerId))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(false);
    }

    Runnable watchContainers = new Runnable() {
        public void run() {
            watchedContainers.entrySet().stream()
                    .filter(Map.Entry::getValue)
                    .map(Map.Entry::getKey)
                    .forEach(this::watchContainer);
        }

        void watchContainer(@Nonnull String containerId) {
            try {
                logger.debug("Checking container: {}", containerId);
                Boolean isRunning = isRunning(containerId);
                if (Boolean.FALSE.equals(isRunning) && getContainerStatus(containerId)) {
                    logger.warn("Container: {} is down", containerId);
                    updateContainerStatus(containerId, false);
                    sendNotification(getContainerName(containerId), containerId, true);
                }
                if (Boolean.TRUE.equals(isRunning) && !getContainerStatus(containerId)) {
                    logger.info("Container: {} is back up!", containerId);
                    updateContainerStatus(containerId, true);
                    sendNotification(getContainerName(containerId), containerId, false);
                }
            } catch (Exception e) {
                logger.error("Error checking containers", e);
            }
        }
    };

    public void sendNotification(final String containerName, final String containerId, boolean isDown) {
        SmtpSettings smtpSettings = smtpSettingsRepository.findTopByOrderByIdAsc().orElse(new SmtpSettings());
        if (Strings.isNullOrEmpty(smtpSettings.getRecipients())) {
            logger.error("No recipient email set!");
            return;
        }
        if (Strings.isNullOrEmpty(smtpSettings.getSmtpHost()) || smtpSettings.getSmtpPort() == 0) {
            logger.error("No SMTP configuration set!");
            return;
        }
        if (Strings.isNullOrEmpty(smtpSettings.getSmtpUsername()) || Strings.isNullOrEmpty(smtpSettings.getSmtpPassword())) {
            logger.error("No SMTP credentials set!");
            return;
        }
        String subject = "Container state change";
        String body = "Container " + containerName + " (" + containerId.substring(0, 12) + ") is";
        body += isDown ? " down!" : " back up!";
        body += "\n\n";
        body += String.join("", getContainerLogs(containerId, 10));
        emailService.sendEmail(smtpSettings.getRecipients(), subject, body);
    }

    public Boolean isRunning(@Nonnull final String containerId) {
        try {
           return dockerClient.inspectContainerCmd(containerId).exec().getState().getRunning();
        } catch (Exception e) {
            logger.error("Error while checking if container {} is running: {}", containerId, e.getMessage());
            return false;
        }
    }

    public String getContainerName(@Nonnull final String containerId) {
        logger.debug("Getting container name for container {}", containerId);
        try {
            InspectContainerResponse response = dockerClient.inspectContainerCmd(containerId).exec();
            if (response != null && response.getName() != null) {
                return response.getName().substring(1);
            } else {
                logger.error("Container {} does not exist or has no name.", containerId);
                return "";
            }
        } catch (NotFoundException e) {
            logger.error("Container {} not found: {}", containerId, e.getMessage());
            return "";
        } catch (Exception e) {
            logger.error("Error while getting container name for container {}: {}", containerId, e.getMessage());
            return "";
        }
    }

    public void toggleContainerStatus(@Nonnull final String containerId) {
        logger.debug("Toggling container {}", containerId);
        try {
            if (isRunning(containerId)) {
                dockerClient.stopContainerCmd(containerId).exec();
            } else {
                dockerClient.startContainerCmd(containerId).exec();
            }
        } catch (Exception e) {
            logger.error("Error while toggling container {}: {}", containerId, e.getMessage());
        }
    }

public List<String> getContainerLogs(@Nonnull String containerId, int numberOfLines) {
        List<String> logs = new ArrayList<>();

        LogContainerCmd logCmd = dockerClient.logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withTail(numberOfLines);

        logCmd.exec(new LogContainerResultCallback() {
            @Override
            public void onNext(Frame item) {
                logs.add(new String(item.getPayload()));
            }
        });

        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            logger.error("Error while waiting for logs to be retrieved: {}", e.getMessage());
        }

        return logs;
    }

}
