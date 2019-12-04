package io.crnk.core.engine.internal.repository;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.result.Result;
import io.crnk.core.repository.response.JsonApiResponse;

import java.util.Collection;
import java.util.Map;

/**
 * A repository adapter for relationship repository.
 */
public interface RelationshipRepositoryAdapter {

    Result<JsonApiResponse> setRelation(Object source, Object targetId, ResourceField field, QueryAdapter queryAdapter);

    Result<JsonApiResponse> setRelations(Object source, Collection targetIds, ResourceField field, QueryAdapter queryAdapter);

    Result<JsonApiResponse> addRelations(Object source, Collection targetIds, ResourceField field, QueryAdapter queryAdapter);

    Result<JsonApiResponse> removeRelations(Object source, Collection targetIds, ResourceField field, QueryAdapter queryAdapter);

    Result<JsonApiResponse> findOneRelations(Object sourceId, ResourceField field, QueryAdapter queryAdapter);


    Result<JsonApiResponse> findManyRelations(Object sourceId, ResourceField field, QueryAdapter queryAdapter);

    Result<Map<Object, JsonApiResponse>> findBulkManyTargets(Collection sourceIds, ResourceField field, QueryAdapter
            queryAdapter);

    Result<Map<Object, JsonApiResponse>> findBulkOneTargets(Collection sourceIds, ResourceField field, QueryAdapter
            queryAdapter);

    Object getImplementation();

    ResourceField getResourceField();
}
