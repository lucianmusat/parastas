package online.lucianmusat.Parastas.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;

import online.lucianmusat.Parastas.services.DockerService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import online.lucianmusat.Parastas.utils.DockerContainer;

@Controller
public class MainController {

    private static final Logger logger = LogManager.getLogger(MainController.class);
    private final DockerService dockerService;
    private Map<String, Boolean> watchedContainers = new HashMap<>();

    @Autowired
    public MainController(DockerService dockerService) {
        this.dockerService = dockerService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<DockerContainer> containers = dockerService.ListAllDockerContainers();
        model.addAttribute("containers", containers);
        containers.forEach(container -> {
            if (!this.watchedContainers.containsKey(container.id())) {
                this.watchedContainers.put(container.id(), false);
            }
        });
        model.addAttribute("selectedContainers", watchedContainers);
        return "index";
    }

    @GetMapping("/container/{id}")
    public String selectContainer(@PathVariable String id) {
        logger.debug("Selected container: " + id);
        watchedContainers.put(id, !watchedContainers.getOrDefault(id, false));
        return "redirect:/";
    }

}
