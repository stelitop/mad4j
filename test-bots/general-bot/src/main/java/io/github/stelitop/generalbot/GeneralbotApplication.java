package io.github.stelitop.generalbot;

import io.github.stelitop.mad4j.Mad4jConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(Mad4jConfig.class)
public class GeneralbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(GeneralbotApplication.class, args);
	}

}
