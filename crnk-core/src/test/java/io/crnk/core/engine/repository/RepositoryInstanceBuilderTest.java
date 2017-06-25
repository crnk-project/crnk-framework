package io.crnk.core.engine.repository;

import io.crnk.core.exception.RepositoryInstanceNotFoundException;
import io.crnk.core.mock.repository.TaskRepository;
import io.crnk.legacy.locator.JsonServiceLocator;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.legacy.registry.RepositoryInstanceBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RepositoryInstanceBuilderTest {

	@Test
	public void onExistingInstanceShouldReturnValue() throws Exception {
		// GIVEN
		RepositoryInstanceBuilder<TaskRepository> sut =
				new RepositoryInstanceBuilder<>(new SampleJsonServiceLocator(), TaskRepository.class);

		// WHEN
		TaskRepository result = sut.buildRepository();

		// THEN
		assertThat(sut.getRepositoryClass()).isEqualTo(TaskRepository.class);
		assertThat(result).isInstanceOf(TaskRepository.class);
	}

	@Test(expected = RepositoryInstanceNotFoundException.class)
	public void onNullInstanceShouldThrowException() throws Exception {
		// GIVEN
		RepositoryInstanceBuilder<TaskRepository> sut =
				new RepositoryInstanceBuilder<>(new JsonServiceLocator() {
					@Override
					public <T> T getInstance(Class<T> clazz) {
						return null;
					}
				}, TaskRepository.class);

		// WHEN
		sut.buildRepository();
	}
}
