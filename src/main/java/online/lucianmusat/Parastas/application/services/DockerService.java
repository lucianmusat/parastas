package online.lucianmusat.Parastas.application.services;

import online.lucianmusat.Parastas.infrastructure.DockerContainer;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public interface DockerService {
    Map<DockerContainer, Boolean> listAllDockerContainers();
    Boolean isRunning(@Nonnull String containerId);
    String getContainerName(@Nonnull String containerId);
    void toggleContainerStatus(@Nonnull String containerId);
    List<String> getContainerLogs(@Nonnull String containerId, int numberOfLines);
    
    Map<String, Boolean> getWatchedContainers();
    void toggleWatchedContainer(@Nonnull String containerId);
    void toggleAllWatchedContainers();
    void updateExecutorSettings();
}
