package io.crnk.example.springboot.simple;

import java.util.HashMap;
import java.util.Map;

import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.spring.boot.v3.CrnkConfigV3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@RestController
@SpringBootApplication
@Import({CrnkConfigV3.class, JpaConfig.class, ModuleConfig.class, CorsConfig.class})
public class SpringBootSimpleExampleApplication {

	@Autowired
	private ResourceRegistry resourceRegistry;

	public static void main(String[] args) {
		SpringApplication.run(SpringBootSimpleExampleApplication.class, args);
		System.out.println("visit http://127.0.0.1:8080/ in your browser");
	}

	@RequestMapping("/resourcesInfo")
	public Map<?, ?> getResources() {
		Map<String, String> result = new HashMap<>();
		// Add all resources (i.e. Project and Task)
		for (RegistryEntry entry : resourceRegistry.getResources()) {
			result.put(entry.getResourceInformation().getResourceType(),
					resourceRegistry.getResourceUrl(entry.getResourceInformation()));
		}
		return result;
	}
}
