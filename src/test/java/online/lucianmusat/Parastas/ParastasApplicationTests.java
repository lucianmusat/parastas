package online.lucianmusat.Parastas;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.BeforeEach;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.model.Container;

import online.lucianmusat.Parastas.services.DockerService;
import online.lucianmusat.Parastas.utils.DockerContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@SpringBootTest
class ParastasApplicationTests {

    private static final Logger logger = LogManager.getLogger(ParastasApplicationTests.class);

    @Mock
    private DockerClient dockerClient;

    @Mock
    private Container container1;
    @Mock
    private Container container2;


    @BeforeEach
    public void setUp() {
        when(container1.getId()).thenReturn("container1");
        when(container1.getImage()).thenReturn("image1");
        when(container1.getState()).thenReturn("running");
        when(container2.getId()).thenReturn("container2");
        when(container2.getImage()).thenReturn("image2");
        when(container2.getState()).thenReturn("running");
    }

    @Test
    public void testListAllDockerContainers() {

        ListContainersCmd listContainersCmdMock = mock(ListContainersCmd.class);
        when(listContainersCmdMock.withShowAll(true)).thenReturn(listContainersCmdMock);
        when(listContainersCmdMock.exec()).thenReturn(Arrays.asList(container1, container2));
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmdMock);

        DockerService dockerService = new DockerService(dockerClient);
        Map<DockerContainer, Boolean> containers = dockerService.listAllDockerContainers();

        assertEquals(2, containers.size());

        Map<String, Boolean> expectedStatusMap = new HashMap<>();
        expectedStatusMap.put("container1", true);
        expectedStatusMap.put("container2", true);

        for (Map.Entry<DockerContainer, Boolean> entry : containers.entrySet()) {
            DockerContainer container = entry.getKey();
            String containerId = container.id();
            Boolean actualStatus = entry.getValue();
            Boolean expectedStatus = expectedStatusMap.get(containerId);
            assertNotNull(expectedStatus);
            assertEquals(expectedStatus, actualStatus);
        }
    }

    @Test
    public void testListNoDockerContainers() {

        ListContainersCmd listContainersCmdMock = mock(ListContainersCmd.class);
        when(listContainersCmdMock.withShowAll(true)).thenReturn(listContainersCmdMock);
        when(listContainersCmdMock.exec()).thenReturn(Arrays.asList());
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmdMock);

        DockerService dockerService = new DockerService(dockerClient);
        Map<DockerContainer, Boolean> containers = dockerService.listAllDockerContainers();

        assertEquals(0, containers.size());
    }

    @Test
    public void testOneContainerisDown() {

        when(container2.getState()).thenReturn("exited");

        ListContainersCmd listContainersCmdMock = mock(ListContainersCmd.class);
        when(listContainersCmdMock.withShowAll(true)).thenReturn(listContainersCmdMock);
        when(listContainersCmdMock.exec()).thenReturn(Arrays.asList(container1, container2));
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmdMock);

        DockerService dockerService = new DockerService(dockerClient);
        Map<DockerContainer, Boolean> containers = dockerService.listAllDockerContainers();

        assertEquals(2, containers.size());

        Map<String, Boolean> expectedStatusMap = new HashMap<>();
        expectedStatusMap.put("container1", true);
        expectedStatusMap.put("container2", false);

        for (Map.Entry<DockerContainer, Boolean> entry : containers.entrySet()) {
            DockerContainer container = entry.getKey();
            String containerId = container.id();
            Boolean actualStatus = entry.getValue();

            Boolean expectedStatus = expectedStatusMap.get(containerId);
            assertNotNull(expectedStatus);

            assertEquals(expectedStatus, actualStatus);
        }
    }

}
