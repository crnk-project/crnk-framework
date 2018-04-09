package io.crnk.reactive.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.Repository;
import io.crnk.core.resource.list.ResourceList;

import java.util.Collection;

import reactor.core.publisher.Mono;


public interface ReactiveResourceRepository<T, I> extends Repository {

	Class<T> getResourceClass();

	Mono<T> findOne(I id, QuerySpec querySpec);

	Mono<ResourceList<T>> findAll(QuerySpec querySpec);

	Mono<ResourceList<T>> findAll(Collection<I> ids, QuerySpec querySpec);

	Mono<T> save(T resource);

	Mono<T> create(T resource);

	Mono<Boolean> delete(I id);

}
