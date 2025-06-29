package online.lucianmusat.Parastas.application.services;

public interface EncryptionService {
    String encrypt(String value);
    String decrypt(String encryptedValue);
}

