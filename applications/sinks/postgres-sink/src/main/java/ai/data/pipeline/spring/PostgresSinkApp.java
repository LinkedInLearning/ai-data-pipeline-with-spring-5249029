package ai.data.pipeline.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PostgresSinkApp {

	public static void main(String[] args) {
		SpringApplication.run(PostgresSinkApp.class, args);
	}

}
