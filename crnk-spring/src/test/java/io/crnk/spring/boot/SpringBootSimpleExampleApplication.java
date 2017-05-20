package io.crnk.spring.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
@ComponentScan(value = "io.crnk.spring",
		excludeFilters = @ComponentScan.Filter(classes = {CrnkSpringBootProperties.class},
				type = FilterType.ASSIGNABLE_TYPE))
public class SpringBootSimpleExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootSimpleExampleApplication.class, args);
	}

	@RequestMapping("/api/custom")
	public String customMethod() {
		return "hello";
	}
}
