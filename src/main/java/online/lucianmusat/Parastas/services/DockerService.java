package online.lucianmusat.Parastas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.dockerjava.api.DockerClient;

import jakarta.annotation.PostConstruct;

import java.util.List;

@Component
public class DockerService {
    
    private DockerClient dockerClient;

    @Autowired
    DockerService(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    public void ListAllDockerContainers() {
        dockerClient.listContainersCmd().withStatusFilter(List.of("running")).exec().forEach(container -> {
            System.out.println("Container ID: " + container.getId());
            System.out.println("Container Name: " + container.getNames()[0]);
        });
    }

    @PostConstruct
    public void initialize() {
        ListAllDockerContainers();
    }

}
