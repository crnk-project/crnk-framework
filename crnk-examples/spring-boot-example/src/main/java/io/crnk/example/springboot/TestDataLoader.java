package io.crnk.example.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.example.springboot.domain.model.ScheduleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.concurrent.Callable;

@Configuration
public class TestDataLoader {

	@Autowired
	private EntityManager em;

	@Autowired
	private TransactionRunner transactionRunner;

	@Autowired
	private ObjectMapper objectMapper;

	@PostConstruct
	public void setup() {
		transactionRunner.doInTransaction(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
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
