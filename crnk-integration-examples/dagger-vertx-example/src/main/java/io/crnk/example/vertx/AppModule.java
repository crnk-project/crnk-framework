package io.crnk.example.vertx;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.example.vertx.repository.ProjectRepository;

import javax.inject.Singleton;

@Module
public class AppModule {

	@Provides
	@Singleton
	@IntoSet
	public ResourceRepository projectRepository() {
		return new ProjectRepository();
	}
}