package online.lucianmusat.Parastas.infrastructure.configuration;

import online.lucianmusat.Parastas.application.services.CredentialsService;
import online.lucianmusat.Parastas.domain.entities.Credentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final List<String> ROLES = List.of("ADMIN", "USER");

    @Autowired
    private CredentialsService credentialsService;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Credentials credentials = credentialsService.getCredentials();
            if (credentials == null) {
                throw new UsernameNotFoundException("User not found");
            }
            return User.builder()
                    .username(credentials.getUsername())
                    .password(credentials.getPassword())
                    .roles(String.valueOf(ROLES))
                    .build();
        };
    }

}
