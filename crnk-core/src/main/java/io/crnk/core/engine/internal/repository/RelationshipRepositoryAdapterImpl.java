package io.crnk.core.engine.internal.repository;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.filter.RepositoryFilterContext;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.result.ImmediateResult;
import io.crnk.core.engine.result.Result;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ManyRelationshipRepository;
import io.crnk.core.repository.OneRelationshipRepository;
import io.crnk.core.repository.response.JsonApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A repository adapter for relationship repository.
 */
@SuppressWarnings("unchecked")
public class RelationshipRepositoryAdapterImpl extends ResponseRepositoryAdapter implements RelationshipRepositoryAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RelationshipRepositoryAdapterImpl.class);

    private final Object relationshipRepository;

    private final ResourceField field;

    public RelationshipRepositoryAdapterImpl(ResourceField field, ModuleRegistry moduleRegistry,
                                             Object relationshipRepository) {
        super(moduleRegistry);
        this.field = field;
        this.relationshipRepository = relationshipRepository;
    }

    @SuppressWarnings("rawtypes")
    public Result<JsonApiResponse> setRelation(Object source, Object targetId, ResourceField field, QueryAdapter queryAdapter) {
        RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

            @Override
            protected JsonApiResponse invoke(RepositoryFilterContext context) {
                RepositoryRequestSpec request = context.getRequest();
                Object source = request.getEntity();
                Serializable targetId = request.getId();
                ResourceField field = request.getRelationshipField();
                LOGGER.debug("setRelation {} on {} with {}", targetId, field, relationshipRepository);
                ((OneRelationshipRepository) relationshipRepository).setRelation(source, targetId, field.getUnderlyingName());
                return new JsonApiResponse();
            }
        };
        RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl
                .forRelation(moduleRegistry, HttpMethod.PATCH, source, queryAdapter, Arrays.asList(targetId), field);
        return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
    }

    @SuppressWarnings("rawtypes")
    public Result<JsonApiResponse> setRelations(Object source, Collection targetIds, ResourceField field, QueryAdapter
            queryAdapter) {
        RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

            @Override
            protected JsonApiResponse invoke(RepositoryFilterContext context) {
                RepositoryRequestSpec request = context.getRequest();
                Object source = request.getEntity();
                Collection<?> targetIds = request.getIds();
                ResourceField field = request.getRelationshipField();
                LOGGER.debug("setRelations {} on {} with {}", targetIds, field, relationshipRepository);
                ((ManyRelationshipRepository) relationshipRepository)
                        .setRelations(source, targetIds, field.getUnderlyingName());
                return new JsonApiResponse();
            }
        };
        RepositoryRequestSpec requestSpec =
                RepositoryRequestSpecImpl.forRelation(moduleRegistry, HttpMethod.PATCH, source, queryAdapter, targetIds, field);
        return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
    }

    @SuppressWarnings("rawtypes")
    public Result<JsonApiResponse> addRelations(Object source, Collection targetIds, ResourceField field, QueryAdapter
            queryAdapter) {
        RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

            @Override
            protected JsonApiResponse invoke(RepositoryFilterContext context) {
                RepositoryRequestSpec request = context.getRequest();
                Object source = request.getEntity();
                Collection<?> targetIds = request.getIds();
                ResourceField field = request.getRelationshipField();
                LOGGER.debug("addRelation {} on {} with {}", targetIds, field, relationshipRepository);
                ((ManyRelationshipRepository) relationshipRepository)
                        .addRelations(source, targetIds, field.getUnderlyingName());
                return new JsonApiResponse();
            }
        };
        RepositoryRequestSpec requestSpec =
                RepositoryRequestSpecImpl.forRelation(moduleRegistry, HttpMethod.POST, source, queryAdapter, targetIds, field);
        return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
    }

    @SuppressWarnings("rawtypes")
    public Result<JsonApiResponse> removeRelations(Object source, Collection targetIds, ResourceField field,
                                                   QueryAdapter queryAdapter) {
        RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

            @Override
            protected JsonApiResponse invoke(RepositoryFilterContext context) {
                RepositoryRequestSpec request = context.getRequest();
                Object source = request.getEntity();
                Collection<?> targetIds = request.getIds();
                ResourceField field = request.getRelationshipField();
                LOGGER.debug("removeRelations {} on {} with {}", targetIds, field, relationshipRepository);
                ((ManyRelationshipRepository) relationshipRepository)
                        .removeRelations(source, targetIds, field.getUnderlyingName());
                return new JsonApiResponse();
            }
        };
        RepositoryRequestSpec requestSpec =
                RepositoryRequestSpecImpl.forRelation(moduleRegistry, HttpMethod.DELETE, source, queryAdapter, targetIds, field);
        return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
    }

    @SuppressWarnings("rawtypes")
    public Result<JsonApiResponse> findOneRelations(Object sourceId, ResourceField field, QueryAdapter queryAdapter) {
        RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

            @Override
            protected JsonApiResponse invoke(RepositoryFilterContext context) {
                RepositoryRequestSpec request = context.getRequest();
                Serializable sourceId = request.getId();
                ResourceField field = request.getRelationshipField();

                LOGGER.debug("findOneRelations for sourceId={} on {} with {}", sourceId, field, relationshipRepository);
                Object resource;
                OneRelationshipRepository querySpecRepository = (OneRelationshipRepository) relationshipRepository;
                ResourceInformation targetResourceInformation =
                        moduleRegistry.getResourceRegistry().getEntry(field.getOppositeResourceType())
                                .getResourceInformation();
                Map map = querySpecRepository
                        .findOneRelations(Arrays.asList(sourceId), field.getUnderlyingName(), request.getQuerySpec(targetResourceInformation));
                resource = map.get(sourceId);
                return getResponse(relationshipRepository, resource, request);
            }
        };
        RepositoryRequestSpec requestSpec =
                RepositoryRequestSpecImpl.forFindTarget(moduleRegistry, queryAdapter, Arrays.asList(sourceId), field);
        return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
    }

    public Result<JsonApiResponse> findManyRelations(Object sourceId, ResourceField field, QueryAdapter queryAdapter) {
        RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

            @Override
            protected JsonApiResponse invoke(RepositoryFilterContext context) {
                RepositoryRequestSpec request = context.getRequest();
                Serializable sourceId = request.getId();
                ResourceField field = request.getRelationshipField();

                LOGGER.debug("findManyRelations for sourceId={} on {} with {}", sourceId, field, relationshipRepository);
                Object resources;
                ManyRelationshipRepository querySpecRepository = (ManyRelationshipRepository) relationshipRepository;
                ResourceInformation targetResourceInformation =
                        moduleRegistry.getResourceRegistry().getEntry(field.getOppositeResourceType())
                                .getResourceInformation();
                Map map = querySpecRepository.findManyRelations(Arrays.asList(sourceId), field.getUnderlyingName(),
                        request.getQuerySpec(targetResourceInformation));
                resources = map.get(sourceId);
                return getResponse(relationshipRepository, resources, request);
            }
        };
        RepositoryRequestSpec requestSpec =
                RepositoryRequestSpecImpl.forFindTarget(moduleRegistry, queryAdapter, Arrays.asList(sourceId), field);
        return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
    }

    @SuppressWarnings("rawtypes")
    public Result<Map<Object, JsonApiResponse>> findBulkManyTargets(Collection sourceIds, ResourceField field,
                                                                    QueryAdapter queryAdapter) {
        if (relationshipRepository instanceof ManyRelationshipRepository) {
            RepositoryBulkRequestFilterChainImpl chain = new RepositoryBulkRequestFilterChainImpl() {

                @Override
                protected Map<Object, JsonApiResponse> invoke(RepositoryFilterContext context) {
                    RepositoryRequestSpec request = context.getRequest();
                    Collection sourceIds = request.getIds();
                    ResourceField field = request.getRelationshipField();
                    QueryAdapter queryAdapter = request.getQueryAdapter();

                    LOGGER.debug("findManyTargets for sourceIds={} on {} with {}", sourceIds, field, relationshipRepository);
                    ManyRelationshipRepository bulkRepository = (ManyRelationshipRepository) relationshipRepository;
                    ResourceInformation targetResourceInformation =
                            moduleRegistry.getResourceRegistry().getEntry(field.getOppositeResourceType()).getResourceInformation();
                    QuerySpec querySpec = request.getQuerySpec(targetResourceInformation);
                    Map targetsMap = bulkRepository.findManyRelations(sourceIds, field.getUnderlyingName(), querySpec);
                    return toResponses(targetsMap, true, queryAdapter, field, HttpMethod.GET);
                }
            };
            RepositoryRequestSpec requestSpec =
                    RepositoryRequestSpecImpl.forFindTarget(moduleRegistry, queryAdapter, new ArrayList<>(sourceIds), field);
            return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
        } else {
            // fallback to non-bulk operation
            Map<Object, JsonApiResponse> responseMap = new HashMap<>();
            for (Object sourceId : sourceIds) {
                JsonApiResponse response = findManyRelations(sourceId, field, queryAdapter).get();
                responseMap.put(sourceId, response);
            }
            return new ImmediateResult<>(responseMap);
        }
    }

    @SuppressWarnings("rawtypes")
    public Result<Map<Object, JsonApiResponse>> findBulkOneTargets(Collection sourceIds, ResourceField field, QueryAdapter
            queryAdapter) {

        if (relationshipRepository instanceof OneRelationshipRepository) {

            RepositoryBulkRequestFilterChainImpl chain = new RepositoryBulkRequestFilterChainImpl() {

                @Override
                protected Map<Object, JsonApiResponse> invoke(RepositoryFilterContext context) {
                    RepositoryRequestSpec request = context.getRequest();
                    Collection<?> sourceIds = request.getIds();
                    ResourceField field = request.getRelationshipField();
                    QueryAdapter queryAdapter = request.getQueryAdapter();

                    OneRelationshipRepository bulkRepository = (OneRelationshipRepository) relationshipRepository;
                    ResourceInformation targetResourceInformation =
                            moduleRegistry.getResourceRegistry().getEntry(field.getOppositeResourceType())
                                    .getResourceInformation();

                    LOGGER.debug("findOneRelations for sourceId={} on {} with {}", sourceIds, field, relationshipRepository);

                    Map targetsMap = bulkRepository
                            .findOneRelations(sourceIds, field.getUnderlyingName(), request.getQuerySpec(targetResourceInformation));
                    return toResponses(targetsMap, false, queryAdapter, field, HttpMethod.GET);
                }
            };
            RepositoryRequestSpec requestSpec =
                    RepositoryRequestSpecImpl.forFindTarget(moduleRegistry, queryAdapter, new ArrayList<>(sourceIds), field);
            return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
        } else {
            // fallback to non-bulk operation
            Map<Object, JsonApiResponse> responseMap = new HashMap<>();
            for (Object sourceId : sourceIds) {
                JsonApiResponse response = findOneRelations(sourceId, field, queryAdapter).get();
                responseMap.put(sourceId, response);
            }
            return new ImmediateResult<>(responseMap);
        }
    }


    private Map<Object, JsonApiResponse> toResponses(Map targetsMap, boolean isMany, QueryAdapter queryAdapter,
                                                     ResourceField field, HttpMethod method) {
        Map<Object, JsonApiResponse> responseMap = new HashMap<>();
        for (Object sourceId : targetsMap.keySet()) {
            Object targets = targetsMap.get(sourceId);
            RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl
                    .forRelation(moduleRegistry, method, null, queryAdapter, Collections.singleton(sourceId), field);
            JsonApiResponse response = getResponse(relationshipRepository, targets, requestSpec);
            responseMap.put(sourceId, response);
        }
        return responseMap;
    }

    public Object getImplementation() {
        return relationshipRepository;
    }

    @Override
    public ResourceField getResourceField() {
        return field;
    }
}
