package online.lucianmusat.Parastas.presentation.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.ui.Model;

import online.lucianmusat.Parastas.application.services.DockerService;
import online.lucianmusat.Parastas.infrastructure.DockerContainer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;


@Controller
public class MainController {

    private static final Logger logger = LogManager.getLogger(MainController.class);

    private static final int SHORT_ID_LENGTH = 12;

    private final DockerService dockerService;
    private Map<DockerContainer, Boolean> containers;

    @Autowired
    public MainController(DockerService dockerService) {
        this.dockerService = dockerService;
    }

    @GetMapping("/")
    public DeferredResult<String> index(Model model) {
        DeferredResult<String> deferredResult = new DeferredResult<>();

        CompletableFuture<Map<DockerContainer, Boolean>> future = CompletableFuture.supplyAsync(dockerService::listAllDockerContainers);

        future.thenAccept(containers -> {
            this.containers = containers;
            dockerService.updateExecutorSettings();
            updateModels(model);
            deferredResult.setResult("index");
        });

        deferredResult.setResult("index");
        return deferredResult;
    }

    private void updateModels(Model model) {
        Map<String, Boolean> watchedContainers = dockerService.getWatchedContainers();
        model.addAttribute("containers", containers);
        model.addAttribute("selectedContainers", watchedContainers);
        model.addAttribute("allWatched", !watchedContainers.containsValue(false));
    }

    @GetMapping("/container/{id}/toggleSelect")
    public String toggleContainer(@PathVariable String id) {
        if (id.length() == SHORT_ID_LENGTH) {
            id = getFullID(id);
        }

        logger.info("Selected container: {}", id);
        dockerService.toggleWatchedContainer(id);
        return "redirect:/";
    }

    private String getFullID(final String shortID) {
        if (containers == null) return shortID;
        return containers.keySet().stream()
                .filter(container -> container.shortID().equals(shortID))
                .findFirst()
                .map(DockerContainer::id)
                .orElse(shortID);
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
        dockerService.toggleAllWatchedContainers();
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

    @GetMapping("/containers")
    @ResponseBody
    public List<Map<String, Object>> getContainers() {
        if (containers == null) {
            containers = dockerService.listAllDockerContainers();
        }
        Map<String, Boolean> watchedContainers = dockerService.getWatchedContainers();
        return containers.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> containerMap = new LinkedHashMap<>();
                    containerMap.put("name", entry.getKey().name());
                    containerMap.put("id", entry.getKey().shortID());
                    containerMap.put("status", entry.getValue().toString());
                    containerMap.put("watched", watchedContainers.getOrDefault(entry.getKey().id(), false).toString());
                    return containerMap;
                }).collect(Collectors.toList());
    }

}
