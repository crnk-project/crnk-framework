package io.crnk.example.springboot;

import io.crnk.spring.jpa.SpringTransactionRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaConfig {

	@Bean
	public SpringTransactionRunner transactionRunner() {
		return new SpringTransactionRunner();
	}
}
