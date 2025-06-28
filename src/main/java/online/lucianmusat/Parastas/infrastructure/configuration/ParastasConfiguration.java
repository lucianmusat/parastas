package online.lucianmusat.Parastas.infrastructure.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Scope;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.User;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import online.lucianmusat.Parastas.domain.entities.SmtpSettings;
import online.lucianmusat.Parastas.domain.repositories.SmtpSettingsRepository;
import online.lucianmusat.Parastas.application.services.CredentialsService;
import online.lucianmusat.Parastas.domain.entities.Credentials;

import java.util.List;
import java.util.Properties;

import com.google.common.base.Strings;


@Configuration
@EnableAsync // for @Async annotation
public class ParastasConfiguration {

    private static final Logger logger = LogManager.getLogger(ParastasConfiguration.class);

    @Bean
    DockerClient dockerClient() {
        DefaultDockerClientConfig config
                = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();

        return DockerClientBuilder.getInstance(config)
                                   .withDockerHttpClient(httpClient).build();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE) // Recreate bean for each request because settings might change in the meantime
    public JavaMailSender javaMailSender(SmtpSettingsRepository smtpSettingsRepository) {
        SmtpSettings smtpSettings = smtpSettingsRepository.findById(1L).orElseGet(SmtpSettings::new);

        String email = smtpSettings.getSmtpUsername();
        String password = smtpSettings.getSmtpPassword();
        String host = smtpSettings.getSmtpHost();
        int port = smtpSettings.getSmtpPort();

        if (Strings.isNullOrEmpty(email) || Strings.isNullOrEmpty(password)) {
            logger.error("Email or password not set!");
            email = "";
            password = "";
        }

        if (Strings.isNullOrEmpty(host) || port == 0) {
            logger.error("Host or port not set!");
            host = "";
            port = 0;
        }

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(email);
        mailSender.setPassword(password);

        Properties properties = new Properties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", true);
        properties.put("mail.smtp.ssl.trust", "*");
        properties.put("mail.debug", "false");

        mailSender.setJavaMailProperties(properties);
        return mailSender;
    }


    @Configuration
    @EnableWebSecurity
    public static class AuthorizeUrlsSecurityConfig {

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

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }


}
