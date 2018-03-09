package io.crnk.reactive.repository.decorator;

import io.crnk.reactive.repository.ReactiveRelationshipRepository;
import io.crnk.reactive.repository.ReactiveResourceRepository;

public interface ReactiveRepositoryDecoratorFactory {

	<T, I> ReactiveResourceRepository<T, I> decorateRepository(ReactiveResourceRepository<T, I> repository);

	<T, I, D, J> ReactiveRelationshipRepository<T, I, D, J> decorateRepository(
			ReactiveRelationshipRepository<T, I, D, J> repository);

}
