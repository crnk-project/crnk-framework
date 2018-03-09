package io.crnk.core.engine.internal.repository;

import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.result.Result;
import io.crnk.core.repository.response.JsonApiResponse;

/**
 * A repository adapter for resource repository
 */
public interface ResourceRepositoryAdapter {


	Result<JsonApiResponse> findOne(Object id, QueryAdapter queryAdapter);

	Result<JsonApiResponse> findAll(QueryAdapter queryAdapter);

	Result<JsonApiResponse> findAll(Iterable ids, QueryAdapter queryAdapter);

	Result<JsonApiResponse> update(Object entity, QueryAdapter queryAdapter);

	Result<JsonApiResponse> create(Object entity, QueryAdapter queryAdapter);

	Result<JsonApiResponse> delete(Object id, QueryAdapter queryAdapter);

	@Deprecated
	Object getResourceRepository();

	ResourceRepositoryInformation getRepositoryInformation();
}
