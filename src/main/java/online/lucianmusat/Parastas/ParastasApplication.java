package online.lucianmusat.Parastas;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.Properties;

@SpringBootApplication
public class ParastasApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParastasApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {
		System.out.println("Hello World!");
		Properties properties = new Properties();
		properties.setProperty("registry.email", "info@baeldung.com");
		properties.setProperty("registry.password", "baeldung");
		properties.setProperty("registry.username", "baaldung");
		properties.setProperty("DOCKER_HOST", "tcp://docker.baeldung.com:2376");

		DefaultDockerClientConfig config
				= DefaultDockerClientConfig.createDefaultConfigBuilder()
				.withProperties(properties).build();

		DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
	}

}
