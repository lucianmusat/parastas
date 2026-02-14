package online.lucianmusat.Parastas;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.context.request.async.DeferredResult;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import online.lucianmusat.Parastas.presentation.controllers.MainController;

import online.lucianmusat.Parastas.application.services.DockerService;
import online.lucianmusat.Parastas.infrastructure.DockerContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.HashMap;


public class MainControllerTests {

    @Mock
    private DockerService dockerService;

    @InjectMocks
    private MainController mainController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(mainController).build();
    }

    @Test
    public void testIndex() throws Exception {
        Map<DockerContainer, Boolean> mockContainers = new HashMap<>();
        when(dockerService.listAllDockerContainers()).thenReturn(mockContainers);

        mockMvc.perform(get("/")).andExpect(status().isOk());

        verify(dockerService, times(1)).listAllDockerContainers();
    }

    @Test
    public void testIndexModel() throws Exception {
        Map<DockerContainer, Boolean> mockContainers = new HashMap<>();
        DockerContainer c1 = new DockerContainer("container1", "image1");
        DockerContainer c2 = new DockerContainer("container2", "image2");
        mockContainers.put(c1, true);
        mockContainers.put(c2, false);
        when(dockerService.listAllDockerContainers()).thenReturn(mockContainers);
        
        Map<String, Boolean> mockWatched = new HashMap<>();
        mockWatched.put("container1", true);
        mockWatched.put("container2", false);
        when(dockerService.getWatchedContainers()).thenReturn(mockWatched);

        Model model = new ConcurrentModel();
        DeferredResult<String> viewName = mainController.index(model);

        viewName.setResultHandler(result -> {
            assertEquals("index", result);
            Map<DockerContainer, Boolean> modelContainers = (Map<DockerContainer, Boolean>) model.getAttribute("containers");
            assertEquals(2, modelContainers.size());
            Map<String, Boolean> modelSelected = (Map<String, Boolean>) model.getAttribute("selectedContainers");
            assertEquals(2, modelSelected.size());
            assertEquals(false, model.getAttribute("allWatched"));
        });
    }

    @Test
    public void testToggleContainer() throws Exception {
        Map<DockerContainer, Boolean> mockContainers = new HashMap<>();
        mockContainers.put(new DockerContainer("containerId1", "image1"), false);
        when(dockerService.listAllDockerContainers()).thenReturn(mockContainers);

        Map<String, Boolean> mockWatched = new HashMap<>();
        mockWatched.put("containerId1", true);
        when(dockerService.getWatchedContainers()).thenReturn(mockWatched);

        mockMvc.perform(get("/container/containerId1/toggleSelect"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        verify(dockerService).toggleWatchedContainer("containerId1");

        mockMvc.perform(get("/containers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == 'containerId1')].watched").value("true"));
    }

}
