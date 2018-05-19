package io.crnk.example.vertx;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.example.vertx.repository.ProjectRepository;

import javax.inject.Singleton;

@Module
public class AppModule {

	@Provides
	@Singleton
	@IntoSet
	public ResourceRepositoryV2 projectRepository() {
		return new ProjectRepository();
	}
}