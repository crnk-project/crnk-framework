package io.crnk.example.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@RestController
@SpringBootApplication
@Import({ExampleJpaModuleConfigurer.class, CorsConfig.class, TestDataLoader.class})
public class SpringBootExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootExampleApplication.class, args);
		System.out.println("visit http://127.0.0.1:8080/api/ resp. http://127.0.0.1:8080/browse/ in your browser");
	}
}
