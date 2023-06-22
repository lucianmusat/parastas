package online.lucianmusat.Parastas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import com.github.dockerjava.api.DockerClient;

// import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.List;

@Component
public class DockerService {
    
    private static final Logger logger = LogManager.getLogger(DockerService.class);
    private DockerClient dockerClient;

    @Autowired
    DockerService(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    public void ListAllDockerContainers(Model model) {
        StringBuilder outputBuilder = new StringBuilder();
        try {
            dockerClient.listContainersCmd().withStatusFilter(List.of("running")).exec().forEach(container -> {
                outputBuilder.append("Container ID: ").append(container.getId()).append("\n");
                outputBuilder.append("Container Name: ").append(container.getNames()[0]).append("\n");
            });
            
            String containerOutput = outputBuilder.toString();
            model.addAttribute("containerOutput", containerOutput);
            logger.info("Container output: " + containerOutput);
        } catch (Exception e) {
            logger.error("Error while listing containers: " + e.getMessage());
        }
    }

    // @PostConstruct
    // public void initialize() {
    //     ListAllDockerContainers();
    // }

}
