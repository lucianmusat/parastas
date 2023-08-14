package online.lucianmusat.Parastas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.exception.NotFoundException;

import online.lucianmusat.Parastas.utils.DockerContainer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Service
public class DockerService {

    private static final Logger logger = LogManager.getLogger(DockerService.class);
    private static DockerClient dockerClient;

    @Autowired
    public DockerService(DockerClient dockerClient) {
        DockerService.dockerClient = dockerClient;
    }

    public Map<DockerContainer, Boolean> listAllDockerContainers() {
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

    public Boolean isRunning(@Nonnull final String containerId) {
        try {
           return dockerClient.inspectContainerCmd(containerId).exec().getState().getRunning();
        } catch (Exception e) {
            logger.error("Error while checking if container " + containerId + " is running: " + e.getMessage());
            return false;
        }
    }

    public String getContainerName(@Nonnull final String containerId) {
        logger.debug("Getting container name for container " + containerId);
        try {
            InspectContainerResponse response = dockerClient.inspectContainerCmd(containerId).exec();
            if (response != null && response.getName() != null) {
                return response.getName().substring(1);
            } else {
                logger.error("Container " + containerId + " does not exist or has no name.");
                return "";
            }
        } catch (NotFoundException e) {
            logger.error("Container " + containerId + " not found: " + e.getMessage());
            return "";
        } catch (Exception e) {
            logger.error("Error while getting container name for container " + containerId + ": " + e.getMessage());
            return "";
        }
    }

    public void toggleContainerStatus(@Nonnull final String containerId) {
        logger.debug("Toggling container " + containerId);
        try {
            if (isRunning(containerId)) {
                dockerClient.stopContainerCmd(containerId).exec();
            } else {
                dockerClient.startContainerCmd(containerId).exec();
            }
        } catch (Exception e) {
            logger.error("Error while toggling container " + containerId + ": " + e.getMessage());
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
            e.printStackTrace();
        }

        return logs;
    }

}
