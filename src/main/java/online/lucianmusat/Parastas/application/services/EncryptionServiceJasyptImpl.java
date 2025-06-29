package online.lucianmusat.Parastas.application.services;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class EncryptionServiceJasyptImpl implements EncryptionService {

    private final StringEncryptor encryptor;

    @Autowired
    public EncryptionServiceJasyptImpl(@Qualifier("stringEncryptor") StringEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    public String encrypt(String value) {
        return encryptor.encrypt(value);
    }

    public String decrypt(String encryptedValue) {
        return encryptor.decrypt(encryptedValue);
    }

}
