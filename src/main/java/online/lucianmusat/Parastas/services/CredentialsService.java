package online.lucianmusat.Parastas.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import online.lucianmusat.Parastas.entities.Credentials;
import online.lucianmusat.Parastas.entities.CredentialsRepository;

@Service
public class CredentialsService {

    private static final Logger logger = LogManager.getLogger(CredentialsService.class);

    @Autowired
    private CredentialsRepository credentialsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Credentials saveOrUpdateCredentials(Credentials credentials) {
        credentials.setPassword(passwordEncoder.encode(credentials.getPassword()));
        return credentialsRepository.save(credentials);
    }

    public Credentials getCredentials() {
        Credentials credentials = credentialsRepository.findById(1L).orElse(new Credentials());
        if (credentials != null) {
            return credentials;
        }
        return new Credentials();
    }

    @PostConstruct
    public void initializeDefaultCredentials() {
        if (credentialsRepository.count() == 0) {
            logger.info("No credentials in the database, creating default");
            Credentials defaultCredentials = new Credentials();
            defaultCredentials.setUsername("admin");
            defaultCredentials.setPassword(passwordEncoder.encode("admin"));
            credentialsRepository.save(defaultCredentials);
        }
    }
}
