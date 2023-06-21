package online.lucianmusat.Parastas;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


@SpringBootApplication
public class ParastasApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParastasApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {
		DefaultDockerClientConfig config
				= DefaultDockerClientConfig.createDefaultConfigBuilder().build();
		DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
				.dockerHost(config.getDockerHost())
				.sslConfig(config.getSSLConfig())
				.build();

		DockerClient dockerClient = DockerClientBuilder.getInstance(config)
		.withDockerHttpClient(httpClient).build();

		List<Container> containers = dockerClient.listContainersCmd().withStatusFilter(List.of("running")).exec();

		for (Container container : containers) {
			System.out.println("Container ID: " + container.getId());
			System.out.println("Container Name: " + container.getNames()[0]);
		}
	}

}
