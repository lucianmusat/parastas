package online.lucianmusat.Parastas.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import online.lucianmusat.Parastas.services.DockerService;

@Controller
public class MainController {

    private final DockerService dockerService;

    @Autowired
    public MainController(DockerService dockerService) {
        this.dockerService = dockerService;
    }

    @GetMapping("/")
    public String index(Model model) {
        dockerService.ListAllDockerContainers(model);
        return "index";
    }

}
