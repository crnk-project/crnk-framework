package io.crnk.reactive.repository;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;


public interface ReactiveManyRelationshipRepository<T, I, D, J>
		extends ReactiveRelationshipRepository<T, I, D, J> {

	Mono<Void> setRelations(T source, Collection<J> targetIds, ResourceField field);

	Mono<Void> addRelations(T source, Collection<J> targetIds, ResourceField field);

	Mono<Void> removeRelations(T source, Collection<J> targetIds, ResourceField field);

	Mono<Map<I, ResourceList<D>>> findManyTargets(Collection<I> sourceIds, ResourceField field, QuerySpec querySpec);

}
