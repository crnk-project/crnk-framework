package io.crnk.example.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.example.springboot.domain.model.Project;
import io.crnk.example.springboot.domain.model.ScheduleEntity;
import io.crnk.example.springboot.domain.model.Task;
import io.crnk.example.springboot.domain.repository.ProjectRepository;
import io.crnk.example.springboot.domain.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Configuration
public class TestDataLoader {

	@Autowired
	private EntityManager em;

	@Autowired
	private TransactionRunner transactionRunner;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private TaskRepository taskRepository;

	@PostConstruct
	public void setup() {

		List<String> interests = new ArrayList<>();
		interests.add("coding");
		interests.add("art");

		Project project121 = projectRepository.save(new Project(121L, "Great Project"));
		Project project122 = projectRepository.save(new Project(122L, "Crnk Project"));
		Project project123 = projectRepository.save(new Project(123L, "Some Project"));
		projectRepository.save(new Project(124L, "JSON API Project"));

		Task task = new Task(1L, "Create tasks");
		task.setProject(project121);
		taskRepository.save(task);
		task = new Task(2L, "Make coffee");
		task.setProject(project122);
		taskRepository.save(task);
		task = new Task(3L, "Do things");
		task.setProject(project123);
		taskRepository.save(task);

		transactionRunner.doInTransaction(new Callable<Object>() {
			@Override
			public Object call() {
				for (int i = 0; i < 10; i++) {
					ScheduleEntity scheduleEntity = new ScheduleEntity();
					scheduleEntity.setId((long) i);
					scheduleEntity.setName("schedule" + i);
					em.persist(scheduleEntity);
				}
				em.flush();
				return null;
			}
		});
	}

	@PostConstruct
	public void configureJackson() {
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	}
}
