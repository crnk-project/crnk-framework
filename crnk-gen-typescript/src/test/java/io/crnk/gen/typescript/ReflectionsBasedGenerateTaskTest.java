package io.crnk.gen.typescript;

import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.repository.ScheduleRepository;

import java.util.Arrays;

public class ReflectionsBasedGenerateTaskTest extends GenerateTypescriptTaskTest {

	@Override()
	protected void configure(TSGeneratorExtension config) {
		config.setResourcePackages(Arrays.asList(Schedule.class.getPackage().getName(), ScheduleRepository.class.getPackage().getName()));
	}
}
