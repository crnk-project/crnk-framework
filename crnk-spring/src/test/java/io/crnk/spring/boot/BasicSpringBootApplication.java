package io.crnk.spring.boot;

import io.crnk.spring.boot.autoconfigure.CrnkJpaAutoConfiguration;
import io.crnk.spring.boot.v3.CrnkConfigV3;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@RestController
@SpringBootApplication
@Import({CrnkConfigV3.class, CrnkJpaAutoConfiguration.class, ModuleConfig.class})
public class BasicSpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(BasicSpringBootApplication.class, args);
	}

	@RequestMapping("/api/custom")
	public String customMethod() {
		return "hello";
	}
}
