package online.lucianmusat.Parastas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;

import online.lucianmusat.Parastas.utils.DockerContainer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.HashMap;


@Service
public class DockerService {
    
    private static final Logger logger = LogManager.getLogger(DockerService.class);
    private static DockerClient dockerClient;

    @Autowired
    DockerService(DockerClient dockerClient) {
        DockerService.dockerClient = dockerClient;
    }

    public Map<DockerContainer, Boolean> ListAllDockerContainers() {
        logger.debug("Listing all containers");
        Map<DockerContainer, Boolean> containers = new HashMap<>();
        try {
            dockerClient.listContainersCmd().withShowAll(true).exec().forEach(container -> {
                containers.put(new DockerContainer(container.getId(), container.getImage()), container.getState().equals("running"));
            });
        } catch (Exception e) {
            logger.error("Error while listing containers: " + e.getMessage());
        }
        logger.debug("Found " + containers.size() + " containers");
        return containers;
    }

    public Boolean isRunning(String containerId) {
        logger.debug("Checking if container " + containerId + " is running");
        try {
            return dockerClient.inspectContainerCmd(containerId).exec().getState().getRunning();
        } catch (Exception e) {
            logger.error("Error while checking if container " + containerId + " is running: " + e.getMessage());
            return false;
        }
    }

    public String getContainerName(String containerId) {
        logger.debug("Getting container name for container " + containerId);
        try {
            return dockerClient.inspectContainerCmd(containerId).exec().getName().substring(1);
        } catch (Exception e) {
            logger.error("Error while getting container name for container " + containerId + ": " + e.getMessage());
            return "";
        }
    }

}
