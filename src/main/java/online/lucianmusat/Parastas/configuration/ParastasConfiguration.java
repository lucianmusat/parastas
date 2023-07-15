package online.lucianmusat.Parastas.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import java.util.Properties;


@Configuration
@EnableAsync
public class ParastasConfiguration {
    
    @Value("${SMTP_USERNAME:}")
    private String email;

    @Value("${SMTP_PASSWORD:}")
    private String password;

    private static final Logger logger = LogManager.getLogger(ParastasConfiguration.class);

    @Bean
    DockerClient dockerClient() {
        DefaultDockerClientConfig config
                = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();

        DockerClient dockerClient = DockerClientBuilder.getInstance(config)
                                                       .withDockerHttpClient(httpClient).build();
        return dockerClient;
    }

    @Bean
    public JavaMailSender javaMailSender() {
        if (email.isEmpty() || password.isEmpty()) {
            logger.error("Email or password not set!");
            return null;
        }
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
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

}
