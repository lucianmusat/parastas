package online.lucianmusat.Parastas;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.web.servlet.view.InternalResourceViewResolver;
import online.lucianmusat.Parastas.presentation.controllers.SettingsController;
import online.lucianmusat.Parastas.application.services.SettingsService;
import online.lucianmusat.Parastas.application.SettingsFormDTO;

public class SettingsControllerTests {

    @Mock
    private SettingsService settingsService;

    @InjectMocks
    private SettingsController settingsController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(settingsController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    public void testSettingsPage() throws Exception {
        mockMvc.perform(get("/settings"))
            .andExpect(status().isOk())
            .andExpect(view().name("settings"))
            .andExpect(model().attribute("activeTab", "user"));

        verify(settingsService, times(1)).initModels(any());
    }

    @Test
    public void testSaveUserSettings() throws Exception {
        when(settingsService.updateCredentials(any(SettingsFormDTO.class))).thenReturn(true);

        mockMvc.perform(post("/save-user-settings")
                .param("username", "newUser")
                .param("oldPassword", "old")
                .param("newPassword", "new"))
            .andExpect(status().isOk())
            .andExpect(view().name("settings"))
            .andExpect(model().attribute("activeTab", "user"))
            .andExpect(model().attribute("saveSuccess", true));

        verify(settingsService).updateCredentials(any(SettingsFormDTO.class));
    }

    @Test
    public void testSaveNotificationSettings() throws Exception {
        mockMvc.perform(post("/save-notification-settings")
                .param("smtpHost", "smtp.test.com")
                .param("smtpPort", "25"))
            .andExpect(status().isOk())
            .andExpect(view().name("settings"))
            .andExpect(model().attribute("activeTab", "notifications"))
            .andExpect(model().attribute("saveSuccess", true));

        verify(settingsService).updateSMTPSettings(any(SettingsFormDTO.class));
        verify(settingsService).updateStateSettings(any(SettingsFormDTO.class));
    }
}
