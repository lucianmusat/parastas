package online.lucianmusat.Parastas;

import online.lucianmusat.Parastas.application.services.CredentialsService;
import online.lucianmusat.Parastas.domain.entities.Credentials;
import online.lucianmusat.Parastas.domain.repositories.CredentialsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CredentialsServiceTests {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CredentialsRepository credentialsRepository;

    @InjectMocks
    private CredentialsService credentialsService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSaveOrUpdateCredentials() {
        Credentials credentials = new Credentials();
        credentials.setPassword("plain");
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(credentialsRepository.save(credentials)).thenReturn(credentials);

        Credentials result = credentialsService.saveOrUpdateCredentials(credentials);

        assertEquals("encoded", result.getPassword());
        verify(credentialsRepository).save(credentials);
    }

    @Test
    public void testGetCredentials() {
        Credentials credentials = new Credentials();
        when(credentialsRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(credentials));

        Credentials result = credentialsService.getCredentials();

        assertSame(credentials, result);
    }

    @Test
    public void testInitializeDefaultCredentials() {
        when(credentialsRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("admin")).thenReturn("encodedAdmin");

        credentialsService.initializeDefaultCredentials();

        verify(credentialsRepository).save(any(Credentials.class));
    }

    @Test
    public void testInitializeDefaultCredentialsAlreadyExists() {
        when(credentialsRepository.count()).thenReturn(1L);

        credentialsService.initializeDefaultCredentials();

        verify(credentialsRepository, never()).save(any(Credentials.class));
    }
}
