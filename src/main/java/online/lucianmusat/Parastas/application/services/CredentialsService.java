package online.lucianmusat.Parastas.application.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.annotation.PostConstruct;
import online.lucianmusat.Parastas.domain.Credentials;
import online.lucianmusat.Parastas.domain.repositories.CredentialsRepository;

@Service
public class CredentialsService {

    private static final Logger logger = LogManager.getLogger(CredentialsService.class);

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private CredentialsRepository credentialsRepository;

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
