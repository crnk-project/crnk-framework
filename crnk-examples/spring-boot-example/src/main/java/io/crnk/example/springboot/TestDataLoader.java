package io.crnk.example.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.example.springboot.domain.model.ScheduleEntity;
import io.crnk.jpa.JpaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

@Configuration
public class TestDataLoader {

	private Logger logger = LoggerFactory.getLogger(TestDataLoader.class);

	@Autowired
	private EntityManager em;

	@Autowired
	private TransactionRunner transactionRunner;

	@Autowired
	private ObjectMapper objectMapper;

	@PostConstruct
	public void setup() {
		try {
			transactionRunner.doInTransaction(() -> {
                for (int i = 0; i < 10; i++) {
                    ScheduleEntity scheduleEntity = new ScheduleEntity();
                    scheduleEntity.setId((long) i);
                    scheduleEntity.setName("schedule" + i);
                    em.persist(scheduleEntity);
                }
                em.flush();
                return null;
            });
		} catch (Exception e) {
			logger.error("failed to execute operation", e);
		}
	}

	@PostConstruct
	public void configureJackson() {
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	}
}
