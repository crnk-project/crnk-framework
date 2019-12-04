package io.crnk.example.springboot.proxied.microservice;

import io.crnk.example.springboot.proxied.microservice.project.ProjectApplication;
import io.crnk.example.springboot.proxied.microservice.task.TaskApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Launches both micro services within the same JVM for demonstration purposes.
 */
public class ProxiedMicroServiceApplication {

	public static final Integer TASK_PORT = 12011;

	public static final Integer PROJECT_PORT = 12012;

	public static ConfigurableApplicationContext startTaskApplication() {
		Map<String, Object> taskProperties = new HashMap<>();
		taskProperties.put("server.port", Integer.toString(TASK_PORT));
		SpringApplication taskApp = new SpringApplication(TaskApplication.class);
		taskApp.setDefaultProperties(taskProperties);
		return taskApp.run();
	}

	public static ConfigurableApplicationContext startProjectApplication() {
		Map<String, Object> projectProperties = new HashMap<>();
		projectProperties.put("server.port", Integer.toString(PROJECT_PORT));
		SpringApplication projectApp = new SpringApplication(ProjectApplication.class);
		projectApp.setDefaultProperties(projectProperties);
		return projectApp.run();
	}

	public static void main(String[] args) {
		startTaskApplication();
		startProjectApplication();
		System.out.println("visit http://127.0.0.1:12011/ and  http://127.0.0.1:12012/ in your browser");
	}
}
