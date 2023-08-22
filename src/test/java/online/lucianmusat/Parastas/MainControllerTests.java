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

import online.lucianmusat.Parastas.controllers.MainController;

import online.lucianmusat.Parastas.services.DockerService;
import online.lucianmusat.Parastas.services.EmailService;
import online.lucianmusat.Parastas.entities.StateSettingsRepository;
import online.lucianmusat.Parastas.utils.DockerContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.HashMap;


public class MainControllerTests {

    @Mock
    private DockerService dockerService;

    @Mock
    private EmailService emailService;

    @Mock
    private StateSettingsRepository stateSettingsRepository;

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
        mockContainers.put(new DockerContainer("container1", "image1"), true);
        mockContainers.put(new DockerContainer("container2", "image2"), false);
        when(dockerService.listAllDockerContainers()).thenReturn(mockContainers);

        Model model = new ConcurrentModel();
        DeferredResult<String> viewName = mainController.index(model);

        viewName.setResultHandler(result -> {
            assertEquals("index", result);
            assertEquals(2, model.getAttribute("containers"));
            assertEquals(1, model.getAttribute("selectedContainers"));
            assertEquals(false, model.getAttribute("allWatched"));
        });
    }

}
