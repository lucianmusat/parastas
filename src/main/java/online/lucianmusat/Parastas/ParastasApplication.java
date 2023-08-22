package online.lucianmusat.Parastas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import online.lucianmusat.Parastas.utils.PBanner;

@SpringBootApplication
public class ParastasApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(ParastasApplication.class);
		application.setBanner(new PBanner());
		application.run(args);
	}
}
