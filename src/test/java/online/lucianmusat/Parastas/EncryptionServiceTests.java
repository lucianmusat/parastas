package online.lucianmusat.Parastas;

import online.lucianmusat.Parastas.application.services.EncryptionServiceJasyptImpl;
import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EncryptionServiceTests {

    @Mock
    private StringEncryptor stringEncryptor;

    @InjectMocks
    private EncryptionServiceJasyptImpl encryptionService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testEncrypt() {
        when(stringEncryptor.encrypt("plain")).thenReturn("encrypted");
        String result = encryptionService.encrypt("plain");
        assertEquals("encrypted", result);
        verify(stringEncryptor).encrypt("plain");
    }

    @Test
    public void testDecrypt() {
        when(stringEncryptor.decrypt("encrypted")).thenReturn("plain");
        String result = encryptionService.decrypt("encrypted");
        assertEquals("plain", result);
        verify(stringEncryptor).decrypt("encrypted");
    }
}
