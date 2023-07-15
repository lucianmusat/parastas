package online.lucianmusat.Parastas.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;

import online.lucianmusat.Parastas.services.DockerService;
import online.lucianmusat.Parastas.services.EmailService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import online.lucianmusat.Parastas.utils.DockerContainer;

@Controller
public class MainController {

    @Value("${RECIPIENT_EMAIL:}")
    private String recipient_email;

    private static final Logger logger = LogManager.getLogger(MainController.class);

    private EmailService emailService;
    private final DockerService dockerService;
    private Map<String, Boolean> watchedContainers = new HashMap<>();
    private final List<String> downContainers = new ArrayList<>();
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private static int WATCH_TIME_S = 1;

    @Autowired
    public MainController(DockerService dockerService, EmailService emailService) {
        this.dockerService = dockerService;
        this.emailService = emailService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<DockerContainer> containers = dockerService.ListAllDockerContainers();
        containers.removeIf(container -> container.name().contains("parastas"));
        model.addAttribute("containers", containers);
        containers.forEach(container -> {
            if (!this.watchedContainers.containsKey(container.id())) {
                this.watchedContainers.put(container.id(), false);
            }
        });
        
        executor.scheduleAtFixedRate(watchContainers, 0, WATCH_TIME_S, TimeUnit.SECONDS);

        model.addAttribute("selectedContainers", watchedContainers);
        return "index";
    }

    @GetMapping("/container/{id}")
    public String selectContainer(@PathVariable String id) {
        logger.debug("Selected container: " + id);
        watchedContainers.put(id, !watchedContainers.getOrDefault(id, false));
        return "redirect:/";
    }

    Runnable watchContainers = new Runnable() {
        public void run() {
            watchedContainers.forEach((id, selected) -> {
                if (selected) {
                    logger.debug("Checking container: " + id);
                    if (!dockerService.isRunning(id) && !downContainers.contains(id)) {
                        downContainers.add(id);
                        logger.warn("Container: " + id + " is down!");
                        sendNotification(dockerService.getContainerName(id), id, true);
                    }
                    if (dockerService.isRunning(id) && downContainers.contains(id)) {
                        downContainers.remove(id);
                        logger.info("Container: " + id + " is back up!");
                        sendNotification(dockerService.getContainerName(id), id, false);
                    }
                }
            });
        }
    };

    public void sendNotification(final String containerName, final String containerId, boolean isDown) {
        if (recipient_email.isEmpty()) {
            logger.error("No recipient email set!");
            return;
        }
        String subject = "Container state change";
        String body = "Container " + containerName + " (" + containerId.substring(0, 12) + ") is";
        body += isDown ? " down!" : " back up!";
        emailService.sendEmail(recipient_email, subject, body);
    }

}
