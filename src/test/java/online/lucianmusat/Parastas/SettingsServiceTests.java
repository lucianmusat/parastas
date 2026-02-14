package online.lucianmusat.Parastas;

import online.lucianmusat.Parastas.application.SettingsFormDTO;
import online.lucianmusat.Parastas.application.services.DockerService;
import online.lucianmusat.Parastas.application.services.EncryptionService;
import online.lucianmusat.Parastas.application.services.SettingsService;
import online.lucianmusat.Parastas.domain.entities.Credentials;
import online.lucianmusat.Parastas.domain.entities.SmtpSettings;
import online.lucianmusat.Parastas.domain.entities.StateSettings;
import online.lucianmusat.Parastas.domain.repositories.CredentialsRepository;
import online.lucianmusat.Parastas.domain.repositories.SmtpSettingsRepository;
import online.lucianmusat.Parastas.domain.repositories.StateSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SettingsServiceTests {

    @Mock
    private SmtpSettingsRepository smtpSettingsRepository;
    @Mock
    private StateSettingsRepository stateSettingsRepository;
    @Mock
    private CredentialsRepository credentialsRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EncryptionService encryptionService;
    @Mock
    private DockerService dockerService;
    @Mock
    private Model model;

    @InjectMocks
    private SettingsService settingsService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInitModels() {
        when(smtpSettingsRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(new SmtpSettings()));
        when(stateSettingsRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(new StateSettings()));
        when(credentialsRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(new Credentials()));

        settingsService.initModels(model);

        verify(model).addAttribute(eq("smtpSettings"), any(SmtpSettings.class));
        verify(model).addAttribute(eq("stateSettings"), any(StateSettings.class));
        verify(model).addAttribute(eq("credentials"), any(Credentials.class));
        verify(model).addAttribute("passwordMatch", true);
        verify(model).addAttribute("saveSuccess", false);
    }

    @Test
    public void testUpdateSMTPSettings() {
        SmtpSettings smtpSettings = new SmtpSettings();
        when(smtpSettingsRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(smtpSettings));
        when(encryptionService.encrypt("password")).thenReturn("encryptedPassword");

        SettingsFormDTO form = new SettingsFormDTO();
        form.setSmtpHost("host");
        form.setSmtpPort("123");
        form.setSmtpUsername("user");
        form.setSmtpPassword("password");
        form.setRecipientEmailList("a@b.com");

        settingsService.updateSMTPSettings(form);

        assertEquals("host", smtpSettings.getSmtpHost());
        assertEquals(123, smtpSettings.getSmtpPort());
        assertEquals("user", smtpSettings.getSmtpUsername());
        assertEquals("encryptedPassword", smtpSettings.getSmtpPassword());
        assertEquals("a@b.com", smtpSettings.getRecipients());
        verify(smtpSettingsRepository).save(smtpSettings);
    }

    @Test
    public void testUpdateCredentialsSuccess() {
        Credentials credentials = new Credentials();
        credentials.setUsername("admin");
        credentials.setPassword("encodedOld");
        when(credentialsRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(credentials));
        when(passwordEncoder.matches("old", "encodedOld")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("encodedNew");

        SettingsFormDTO form = new SettingsFormDTO();
        form.setUsername("newAdmin");
        form.setOldPassword("old");
        form.setNewPassword("new");

        boolean result = settingsService.updateCredentials(form);

        assertTrue(result);
        assertEquals("newAdmin", credentials.getUsername());
        assertEquals("encodedNew", credentials.getPassword());
        verify(credentialsRepository).save(credentials);
    }

    @Test
    public void testUpdateCredentialsWrongOldPassword() {
        Credentials credentials = new Credentials();
        credentials.setPassword("encodedOld");
        when(credentialsRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(credentials));
        when(passwordEncoder.matches("wrong", "encodedOld")).thenReturn(false);

        SettingsFormDTO form = new SettingsFormDTO();
        form.setOldPassword("wrong");

        boolean result = settingsService.updateCredentials(form);

        assertFalse(result);
        verify(credentialsRepository, never()).save(any());
    }

    @Test
    public void testUpdateStateSettings() {
        StateSettings stateSettings = new StateSettings();
        when(stateSettingsRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(stateSettings));

        SettingsFormDTO form = new SettingsFormDTO();
        form.setRefreshPeriod("60");

        settingsService.updateStateSettings(form);

        assertEquals(60, stateSettings.getRefreshPeriodSeconds());
        verify(stateSettingsRepository).save(stateSettings);
        verify(dockerService).updateExecutorSettings();
    }
}
