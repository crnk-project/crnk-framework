package io.crnk.reactive;

import io.crnk.core.engine.internal.repository.RepositoryAdapterFactory;
import io.crnk.core.module.Module;
import io.crnk.legacy.repository.information.DefaultRelationshipRepositoryInformationProvider;
import io.crnk.legacy.repository.information.DefaultResourceRepositoryInformationProvider;
import io.crnk.reactive.internal.adapter.ReactiveRepositoryAdapterFactory;
import io.crnk.reactive.repository.ReactiveRelationshipRepository;
import io.crnk.reactive.repository.ReactiveResourceRepository;
import net.jodah.typetools.TypeResolver;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class ReactiveModule implements Module {

	private Scheduler workerScheduler = Schedulers.elastic();


	@Override
	public String getModuleName() {
		return "reactive";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addRepositoryInformationBuilder(new ReactiveResourceRepositoryInformationProvider());
		context.addRepositoryInformationBuilder(new ReactiveRelationshipRepositoryInformationProvider());
		context.addRepositoryAdapterFactory(new ReactiveRepositoryAdapterFactory(context.getModuleRegistry(), workerScheduler));
	}

	/**
	 * @param scheduler to use to access traditional, non-reactive repositories.
	 */
	public void setWorkerScheduler(Scheduler scheduler) {
		workerScheduler = scheduler;
	}

	class ReactiveResourceRepositoryInformationProvider extends DefaultResourceRepositoryInformationProvider {

		@Override
		public boolean accept(Class<?> repositoryClass) {
			return ReactiveResourceRepository.class.isAssignableFrom(repositoryClass);
		}

		protected Class<?> getResourceClass(Object repository, Class<?> repositoryClass) {
			if (repository != null) {
				ReactiveResourceRepository querySpecRepo = (ReactiveResourceRepository) repository;
				return querySpecRepo.getResourceClass();
			} else {
				Class<?>[] typeArgs = TypeResolver.resolveRawArguments(ReactiveRelationshipRepository.class, repositoryClass);
				return typeArgs[0];
			}
		}
	}

	class ReactiveRelationshipRepositoryInformationProvider extends DefaultRelationshipRepositoryInformationProvider {

		@Override
		public boolean accept(Class<?> repositoryClass) {
			return ReactiveRelationshipRepository.class.isAssignableFrom(repositoryClass);
		}
	}
}