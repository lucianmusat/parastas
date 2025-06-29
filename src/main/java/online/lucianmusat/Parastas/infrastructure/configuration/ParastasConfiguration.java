package online.lucianmusat.Parastas.infrastructure.configuration;

import online.lucianmusat.Parastas.application.services.EncryptionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Scope;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import online.lucianmusat.Parastas.domain.entities.SmtpSettings;
import online.lucianmusat.Parastas.domain.repositories.SmtpSettingsRepository;

import java.util.Properties;

import com.google.common.base.Strings;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


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
    public JavaMailSender javaMailSender(SmtpSettingsRepository smtpSettingsRepository, EncryptionService encryptionService) {
        SmtpSettings smtpSettings = smtpSettingsRepository.findTopByOrderByIdAsc().orElseGet(SmtpSettings::new);

        String email = smtpSettings.getSmtpUsername();
        String password = encryptionService.decrypt(smtpSettings.getSmtpPassword());
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

    @Bean
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword("super-secret-key"); // TODO: Use a secure, environment-specific key, env-var
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
