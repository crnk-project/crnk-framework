package io.crnk.client.legacy;

import java.io.Serializable;
import java.util.List;

import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.ResourceRepository;

/**
 * Implemented by every {@link ResourceRepository} stub.
 */
public interface ResourceRepositoryStub<T, ID extends Serializable> extends ResourceRepository<T, ID> {

	@Override
	List<T> findAll(QueryParams queryParams);

	@Override
	List<T> findAll(Iterable<ID> ids, QueryParams queryParams);

	/**
	 * Saves the given entity without any of its relationships.
	 *
	 * @param entity resource to be saved
	 * @param <S>    resource type
	 * @return persisted resource
	 */
	<S extends T> S create(S entity);
}
