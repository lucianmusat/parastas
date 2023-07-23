package online.lucianmusat.Parastas.controllers;

import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Optional;

import com.google.common.base.Strings;

@Controller
public class MainController {

    private static final Logger logger = LogManager.getLogger(MainController.class);

    private final EmailService emailService;
    private final DockerService dockerService;
    private final Map<String, Boolean> watchedContainers = new HashMap<>();
    Map<DockerContainer, Boolean>  containers = new HashMap<>();
    private ScheduledExecutorService executor;

    @Autowired
    private StateSettingsRepository stateSettingsRepository;
    @Autowired
    private SmtpSettingsRepository smtpSettingsRepository;

    @Autowired
    public MainController(DockerService dockerService, EmailService emailService) {
        this.dockerService = dockerService;
        this.emailService = emailService;
    }

    @GetMapping("/")
    public String index(Model model) {
        containers = dockerService.ListAllDockerContainers();
        containers.entrySet().removeIf(entry -> entry.getKey().name().contains("parastas"));
        updateWatchedContainers();
        startWatching();
        model.addAttribute("containers", containers);
        model.addAttribute("selectedContainers", watchedContainers);
        return "index";
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

    private void startWatching() {
        StateSettings stateSettings = stateSettingsRepository.findById(1L).orElse(new StateSettings());
        stopWatching();
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(watchContainers, 0, stateSettings.getRefreshPeriodSeconds(), TimeUnit.SECONDS);
    }

    @GetMapping("/container/{id}")
    public String selectContainer(@PathVariable String id) {
        logger.debug("Selected container: {}", id);
        watchedContainers.put(id, !watchedContainers.getOrDefault(id, false));
        return "redirect:/";
    }

    private void updateContainerStatus(String containerId, Boolean newStatus) {
        containers.entrySet()
                .stream()
                .filter(entry -> entry.getKey().id().equals(containerId))
                .findFirst()
                .ifPresent(entry -> containers.put(entry.getKey(), newStatus));
    }

    private Boolean getContainerStatus(final String containerId) {
        Optional<Boolean> status = containers.entrySet()
            .stream()
            .filter(entry -> entry.getKey().id().equals(containerId))
            .map(Map.Entry::getValue)
            .findFirst();

        return status.orElse(null);
    }

    Runnable watchContainers = new Runnable() {
        public void run() {
            watchedContainers.forEach((id, selected) -> {
                if (Boolean.TRUE.equals(selected)) {
                    logger.debug("Checking container: {}", id.substring(0, 12));
                    if (!dockerService.isRunning(id) && !getContainerStatus(id)) {
                        updateContainerStatus(id, false);
                        logger.warn("Container: {} is down", id);
                        sendNotification(dockerService.getContainerName(id), id, true);
                    }
                    if (dockerService.isRunning(id) && getContainerStatus(id)) {
                        updateContainerStatus(id, true);
                        logger.info("Container: {} is back up!", id);
                        sendNotification(dockerService.getContainerName(id), id, false);
                    }
                }
            });
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
        emailService.sendEmail(smtpSettings.getRecipients(), subject, body);
    }

    @GetMapping("/container/{id}/status/{status}")
    public String setContainerStatus(@PathVariable String id, @PathVariable boolean status) {
        logger.debug("Setting container: {} status to: {}", id, status);
        updateContainerStatus(id, status);
        return "redirect:/";
    }

}
