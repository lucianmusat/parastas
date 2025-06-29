package online.lucianmusat.Parastas.application.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import online.lucianmusat.Parastas.domain.entities.Credentials;
import online.lucianmusat.Parastas.domain.repositories.CredentialsRepository;

@Service
public class CredentialsService {

    private static final Logger logger = LogManager.getLogger(CredentialsService.class);

    private final PasswordEncoder passwordEncoder;
    private final CredentialsRepository credentialsRepository;

    @Autowired
    public CredentialsService(PasswordEncoder passwordEncoder, CredentialsRepository credentialsRepository) {
        this.passwordEncoder = passwordEncoder;
        this.credentialsRepository = credentialsRepository;
    }

    public Credentials saveOrUpdateCredentials(Credentials credentials) {
        credentials.setPassword(passwordEncoder.encode(credentials.getPassword()));
        return credentialsRepository.save(credentials);
    }

    public Credentials getCredentials() {
        return credentialsRepository.findTopByOrderByIdAsc().orElse(new Credentials());
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
