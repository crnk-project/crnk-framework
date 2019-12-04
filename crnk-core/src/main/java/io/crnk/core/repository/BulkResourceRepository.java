package io.crnk.core.repository;

import java.util.Arrays;
import java.util.List;

import io.crnk.core.engine.internal.utils.PreconditionUtil;

/**
 * Bulk-flavor of {@link ResourceRepository} to modify multiple resource simultaneously. Can be used
 * together with {link OperationsModule} to gain performance with bulk imports.
 * <p>
 * This feature is experimental supporting only POST as of yet.
 *
 * @param <T> Type of an entity
 * @param <I> Type of Identifier of an entity
 */
public interface BulkResourceRepository<T, I> extends ResourceRepository<T, I>, Repository {

	default <S extends T> S save(S resource) {
		List<S> results = save(Arrays.asList(resource));
		PreconditionUtil.verifyEquals(1, results.size(), "expected single result");
		return results.get(0);
	}


	default <S extends T> S create(S resource) {
		List<S> results = create(Arrays.asList(resource));
		PreconditionUtil.verifyEquals(1, results.size(), "expected single result");
		return results.get(0);
	}

	default void delete(I id) {
		delete(Arrays.asList(id));
	}

	/**
	 * Bulk saves resources. A Returning resources must include assigned identifier created for the instance of resource.
	 *
	 * @param resources to be saved
	 * @param <S> type of the resource
	 * @return saved resource. Must include set identifier.
	 */
	<S extends T> List<S> save(List<S> resources);

	/**
	 * Bulk creates resources. A Returning resources must include assigned identifier created for the instance of resource.
	 *
	 * @param resources to be saved
	 * @param <S> type of the resource
	 * @return saved resource. Must include set identifier.
	 */
	<S extends T> List<S> create(List<S> resources);

	/**
	 * Bulk removes resources identified by id.
	 *
	 * @param ids identified of the resource to be removed
	 */
	void delete(List<I> ids);

}
