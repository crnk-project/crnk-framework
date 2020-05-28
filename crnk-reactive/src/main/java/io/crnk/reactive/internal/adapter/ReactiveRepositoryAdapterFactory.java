package io.crnk.reactive.internal.adapter;

import io.crnk.core.engine.information.repository.RelationshipRepositoryInformation;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.RepositoryAdapterFactory;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.reactive.repository.ImmediateRepository;
import io.crnk.reactive.repository.ReactiveManyRelationshipRepository;
import io.crnk.reactive.repository.ReactiveOneRelationshipRepository;
import io.crnk.reactive.repository.ReactiveRelationshipRepository;
import io.crnk.reactive.repository.ReactiveResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Collection;

public class ReactiveRepositoryAdapterFactory implements RepositoryAdapterFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveRepositoryAdapterFactory.class);

	private final ModuleRegistry moduleRegistry;

	private Scheduler workerScheduler;

	public ReactiveRepositoryAdapterFactory(ModuleRegistry moduleRegistry, Scheduler workerScheduler) {
		this.moduleRegistry = moduleRegistry;
		this.workerScheduler = workerScheduler;
	}

	public void setWorkerScheduler(Scheduler scheduler) {
		this.workerScheduler = scheduler;
	}

	@Override
	public boolean accepts(Object repository) {
		return repository instanceof ReactiveResourceRepository || repository instanceof ReactiveRelationshipRepository;
	}

	@Override
	public ResourceRepositoryAdapter createResourceRepositoryAdapter(ResourceRepositoryInformation information, Object repository) {
		return new ReactiveResourceRepositoryAdapter(information, moduleRegistry, (ReactiveResourceRepository) repository);
	}

	@Override
	public RelationshipRepositoryAdapter createRelationshipRepositoryAdapter(ResourceField field, RelationshipRepositoryInformation information, Object repository) {
		if (Collection.class.isAssignableFrom(field.getType())) {
			return new ReactiveManyRelationshipRepositoryAdapter(field, information, moduleRegistry, (ReactiveManyRelationshipRepository) repository);
		}
		return new ReactiveOneRelationshipRepositoryAdapter(field, information, moduleRegistry, (ReactiveOneRelationshipRepository) repository);
	}

	@Override
	public ResourceRepositoryAdapter decorate(ResourceRepositoryAdapter adapter) {
		Object implementation = adapter.getImplementation();
		if (implementation instanceof ReactiveResourceRepository) {
			return adapter;
		}

		boolean immediate = implementation.getClass().getAnnotation(ImmediateRepository.class) != null;
		LOGGER.debug("wrapping non-reactive repository {}, immediate={}", implementation, immediate);
		Scheduler scheduler = immediate ? Schedulers.immediate() : Schedulers.elastic();
		return new WorkerResourceRepositoryAdapter(adapter, scheduler, moduleRegistry.getHttpRequestContextProvider());
	}

	@Override
	public RelationshipRepositoryAdapter decorate(RelationshipRepositoryAdapter adapter) {
		Object implementation = adapter.getImplementation();
		if (implementation instanceof ReactiveRelationshipRepository) {
			return adapter;
		}

		boolean immediate = implementation.getClass().getAnnotation(ImmediateRepository.class) != null;
		LOGGER.debug("wrapping non-reactive repository {}, immediate={}", implementation, immediate);
		Scheduler scheduler = immediate ? Schedulers.immediate() : workerScheduler;
		return new WorkerRelationshipRepositoryAdapter(adapter, scheduler, moduleRegistry.getHttpRequestContextProvider());
	}
}
