package online.lucianmusat.Parastas.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;

import online.lucianmusat.Parastas.entities.SmtpSettings;
import online.lucianmusat.Parastas.entities.SmtpSettingsRepository;
import online.lucianmusat.Parastas.entities.StateSettingsRepository;
import online.lucianmusat.Parastas.services.DockerService;
import online.lucianmusat.Parastas.services.EmailService;
import online.lucianmusat.Parastas.utils.DockerContainer;
import online.lucianmusat.Parastas.entities.StateSettings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.Optional;
import javax.annotation.Nonnull;

import com.google.common.base.Strings;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;


@Controller
public class MainController {

    private static final Logger logger = LogManager.getLogger(MainController.class);

    private final EmailService emailService;
    private final DockerService dockerService;
    private StateSettingsRepository stateSettingsRepository;
    private final Map<String, Boolean> watchedContainers = new ConcurrentHashMap<>();
    private Map<DockerContainer, Boolean>  containers = new ConcurrentHashMap<>();
    private ScheduledExecutorService executor;
    private AtomicInteger refreshPeriodSeconds = new AtomicInteger(60);
    private StateSettings stateSettings;
    private final int nrThreads = 1;


    @Autowired
    private SmtpSettingsRepository smtpSettingsRepository;

    @Autowired
    public MainController(DockerService dockerService, EmailService emailService, StateSettingsRepository stateSettingsRepository) {
        this.dockerService = dockerService;
        this.emailService = emailService;
        this.stateSettingsRepository = stateSettingsRepository;
        // There is a thing called @Scheduled method in Spring, but I need an easy way to update the refresh period on the fly
        executor = Executors.newScheduledThreadPool(nrThreads);
        stateSettings = stateSettingsRepository.findById(1L).orElse(new StateSettings());
        refreshPeriodSeconds.set(stateSettings.getRefreshPeriodSeconds());
        executor.scheduleAtFixedRate(watchContainers, 0, refreshPeriodSeconds.get(), TimeUnit.SECONDS);
    }

    @GetMapping("/")
    public String index(Model model) {
        containers = dockerService.ListAllDockerContainers();
        containers.entrySet().removeIf(entry -> entry.getKey().name().contains("parastas"));
        updateWatchedContainers();
        updateExecutorSettings();
        updateModels(model);
        return "index";
    }

    private void updateModels(Model model) {
        model.addAttribute("containers", containers);
        model.addAttribute("selectedContainers", watchedContainers);
        model.addAttribute("allWatched", !watchedContainers.containsValue(false));
    }

    private void updateWatchedContainers() {
        containers.forEach((container, status) -> {
            if (!watchedContainers.containsKey(container.id())) {
                watchedContainers.put(container.id(), false);
            }
        });
    }

    private void stopWatching() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private void updateExecutorSettings() {
        int newRefreshPeriodSeconds = stateSettingsRepository.findById(1L).orElse(stateSettings).getRefreshPeriodSeconds();
        if (newRefreshPeriodSeconds != refreshPeriodSeconds.get()) {
            logger.info("Updating refresh period to {} seconds", newRefreshPeriodSeconds);
            stopWatching();
            executor = Executors.newScheduledThreadPool(nrThreads);
            refreshPeriodSeconds.set(newRefreshPeriodSeconds);
            executor.scheduleAtFixedRate(watchContainers, 0, refreshPeriodSeconds.get(), TimeUnit.SECONDS);
        }
    }

    @GetMapping("/container/{id}/toggleSelect")
    public String toggleContainer(@PathVariable String id, Model model) {
        logger.info("Selected container: {}", id);
        watchedContainers.put(id, !watchedContainers.getOrDefault(id, false));
        return "redirect:/";
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
        Optional<Boolean> status = containers.entrySet()
            .stream()
            .filter(entry -> entry.getKey().id().equals(containerId))
            .map(Map.Entry::getValue)
            .findFirst();
        return status.orElse(false);
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
                Boolean isRunning = dockerService.isRunning(containerId);
                if (Boolean.FALSE.equals(isRunning) && Boolean.TRUE.equals(getContainerStatus(containerId))) {
                    logger.warn("Container: {} is down", containerId);
                    updateContainerStatus(containerId, false);
                    sendNotification(dockerService.getContainerName(containerId), containerId, true);
                }
                if (Boolean.TRUE.equals(isRunning) && Boolean.FALSE.equals(getContainerStatus(containerId))) {
                    logger.info("Container: {} is back up!", containerId);
                    updateContainerStatus(containerId, true);
                    sendNotification(dockerService.getContainerName(containerId), containerId, false);
                }
            } catch (Exception e) {
                logger.error("Error checking containers: {}", e);
            }
        }
    };

    public void sendNotification(final String containerName, final String containerId, boolean isDown) {
        SmtpSettings smtpSettings = smtpSettingsRepository.findById(1L).orElse(new SmtpSettings());
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
        body += dockerService.getContainerLogs(containerId, 10).stream().collect(Collectors.joining());
        emailService.sendEmail(smtpSettings.getRecipients(), subject, body);
    }

    @GetMapping("/container/{id}/toggleStatus")
    public String setContainerStatus(@PathVariable @Nonnull String id) {
        dockerService.toggleContainerStatus(id);
        return "redirect:/";
    }

    @GetMapping("/container/{id}/status")
    public ResponseEntity<String> containerStatus(@PathVariable @Nonnull String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(dockerService.isRunning(id).toString(), headers, HttpStatus.OK);
    }

    @GetMapping("/container/toggleAll")
    public String toggleAllContainers() {
        boolean hasTrueValue = watchedContainers.containsValue(true);
        boolean allFalse = !watchedContainers.containsValue(true);
        boolean allTrue = !watchedContainers.containsValue(false);

        if ((hasTrueValue && !allTrue) || allFalse) {
            watchedContainers.replaceAll((key, value) -> true);
        } else {
            watchedContainers.replaceAll((key, value) -> false);
        }

        return "redirect:/";
    }

    @GetMapping("/container/{id}/logs/{lines}")
    public String displayContainerLogs(@PathVariable @Nonnull String id, @PathVariable(required = false) Integer lines, Model model) {
        if (lines == null) {
            lines = 10;
        }
        model.addAttribute("containerLogs", dockerService.getContainerLogs(id, lines));
        return "logs";
    }

}
