package io.crnk.core.engine.internal.document.mapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.InternalServerErrorException;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.annotations.JsonApiLookupIncludeAutomatically;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.utils.Nullable;
import io.crnk.legacy.internal.QueryParamsAdapter;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncludeLookupSetter {

	private static final Logger LOGGER = LoggerFactory.getLogger(IncludeLookupSetter.class);

	private final ResourceRegistry resourceRegistry;

	private final LookupIncludeBehavior globalLookupIncludeBehavior;

	private ResourceMapper resourceMapper;

	private IncludeLookupUtil util;

	// currently
	private boolean allowPagination = false;

	public IncludeLookupSetter(ResourceRegistry resourceRegistry, ResourceMapper resourceMapper,
			PropertiesProvider propertiesProvider) {
		this.resourceMapper = resourceMapper;
		this.resourceRegistry = resourceRegistry;

		this.globalLookupIncludeBehavior = IncludeLookupUtil.getDefaultLookupIncludeBehavior(propertiesProvider);
		IncludeBehavior includeBehavior = IncludeLookupUtil.getIncludeBehavior(propertiesProvider);
		this.util = new IncludeLookupUtil(resourceRegistry, includeBehavior);
		this.allowPagination = propertiesProvider != null && Boolean.parseBoolean(propertiesProvider.getProperty(CrnkProperties
				.INCLUDE_PAGING_ENABLED));
	}

	public void setIncludedElements(Document document, Object entity, QueryAdapter queryAdapter,
			RepositoryMethodParameterProvider parameterProvider, Set<String> additionalEagerLoadedRelations) {

		QueryAdapter inclusionQueryAdapter = queryAdapter;
		if(!allowPagination && !(queryAdapter instanceof QueryParamsAdapter) && queryAdapter != null) {
			// offset/limit cannot properly work for nested inclusions if becomes cyclic
			inclusionQueryAdapter = queryAdapter.duplicate();
			inclusionQueryAdapter.setOffset(0);
			inclusionQueryAdapter.setLimit(null);
		}

		List<Object> entityList = DocumentMapperUtil.toList(entity);
		List<Resource> dataList = DocumentMapperUtil.toList(document.getData().get());
		Map<ResourceIdentifier, Resource> dataMap = new HashMap<>();
		Map<ResourceIdentifier, Object> entityMap = new HashMap<>();
		for (int i = 0; i < dataList.size(); i++) {
			Resource dataElement = dataList.get(i);
			ResourceIdentifier id = dataElement.toIdentifier();
			entityMap.put(id, entityList.get(i));
			dataMap.put(id, dataElement);
		}

		Map<ResourceIdentifier, Resource> resourceMap = new HashMap<>();
		resourceMap.putAll(dataMap);

		Set<ResourceIdentifier> inclusions = new HashSet<>();
		PopulatedCache populatedCache = new PopulatedCache();

		ArrayList<ResourceField> stack = new ArrayList<>();
		populate(dataList, inclusions, resourceMap, entityMap, stack, inclusionQueryAdapter, parameterProvider,
				additionalEagerLoadedRelations, populatedCache);

		// no need to include resources included in the data section
		inclusions.removeAll(dataMap.keySet());

		// setup included section
		ArrayList<Resource> included = new ArrayList<>();
		for (ResourceIdentifier inclusionId : inclusions) {
			Resource includedResource = resourceMap.get(inclusionId);
			PreconditionUtil.assertNotNull("resource not found", includedResource);
			included.add(includedResource);
		}
		Collections.sort(included);
		LOGGER.debug("Extracted included resources {}", included.toString());
		document.setIncluded(included);
	}

	private void populate(Collection<Resource> resourceList, Set<ResourceIdentifier> inclusions,
			Map<ResourceIdentifier, Resource> resourceMap, Map<ResourceIdentifier, Object> entityMap,
			List<ResourceField> fieldPath,
			QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider,
			Set<String> additionalEagerLoadedRootRelations, PopulatedCache populatedCache) {

		if (resourceList.isEmpty()) {
			return; // nothing to do
		}

		checkNoRecursion(fieldPath);

		Set<ResourceField> relationshipFields = util.getRelationshipFields(resourceList);
		for (ResourceField resourceField : relationshipFields) {
			fieldPath.add(resourceField);
			populateField(resourceField, queryAdapter, fieldPath, populatedCache, resourceList,
					additionalEagerLoadedRootRelations, parameterProvider, resourceMap, entityMap, inclusions);
			fieldPath.remove(fieldPath.size() - 1);
		}
	}

	private void populateField(ResourceField resourceField, QueryAdapter queryAdapter, List<ResourceField> fieldPath,
			PopulatedCache populatedCache, Collection<Resource> resourceList, Set<String> additionalEagerLoadedRootRelations,
			RepositoryMethodParameterProvider parameterProvider, Map<ResourceIdentifier, Resource> resourceMap,
			Map<ResourceIdentifier, Object> entityMap, Set<ResourceIdentifier> inclusions) {
		ResourceInformation resourceInformation = resourceField.getParentResourceInformation();

		boolean includeRequested = util.isInclusionRequested(queryAdapter, fieldPath);

		boolean includeResources = includeRequested || resourceField.getIncludeByDefault();
		boolean includeRelationshipData = !resourceField.isLazy() || includeResources || additionalEagerLoadedRootRelations
				.contains(resourceField.getJsonName());

		if (includeRelationshipData) {

			Collection<Resource> unpopulatedResourceList = populatedCache.filterProcessed(resourceList, resourceField);
			if (!unpopulatedResourceList.isEmpty()) {

				// only handle resources from the proper subtype where the
				// relationship is desired to be loaded
				List<Resource> resourcesByType = util.filterByType(unpopulatedResourceList, resourceInformation);
				List<Resource> resourcesWithField = util.filterByLoadedRelationship(resourcesByType, resourceField);

				// lookup resources by inspecting the POJOs in entityMap
				LookupIncludeBehavior fieldLookupIncludeBehavior = resourceField.getLookupIncludeAutomatically();

				Set<Resource> populatedResources;
				if (fieldLookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_ALWAYS
						|| globalLookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_ALWAYS) {
					// lookup resources by making repository calls
					populatedResources =
							lookupRelationshipField(resourcesWithField, resourceField, queryAdapter, parameterProvider,
									resourceMap, entityMap);
				}
				else if (fieldLookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL
						|| globalLookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL) {
					// try to populate from entities
					Set<Resource> extractedResources =
							extractRelationshipField(resourcesWithField, resourceField, queryAdapter, resourceMap, entityMap,
									true);

					// do lookups where relationship data is null
					Collection<Resource> resourcesForLookup =
							util.findResourcesWithoutRelationshipData(resourcesWithField, resourceField);
					Collection<Resource> lookedupResources =
							lookupRelationshipField(resourcesForLookup, resourceField, queryAdapter, parameterProvider,
									resourceMap, entityMap);

					populatedResources = util.union(lookedupResources, extractedResources);
				}
				else {
					// do not do any lookups
					populatedResources =
							extractRelationshipField(resourcesWithField, resourceField, queryAdapter, resourceMap, entityMap,
									false);

					// set relationship data to null for single-valued relation.
					// extractRelationshipField cannot differentiate between
					// null and not loaded.
					// It assume it is null and loaded. Otherwise an application
					// can reconfigure the includeBehavior to make a lookup
					// and be sure.
					if (!Iterable.class.isAssignableFrom(resourceField.getType())) {
						Nullable<Object> emptyData = Nullable.nullValue();
						for (Resource resourceWithField : resourcesWithField) {
							Relationship relationship = resourceWithField.getRelationships().get(resourceField.getJsonName
									());
							if (!relationship.getData().isPresent()) {
								relationship.setData(emptyData);
							}
						}
					}
				}

				// add inclusions and do nested population if requested as
				// such
				if (includeResources && !populatedResources.isEmpty()) {
					inclusions.addAll(util.toIds(populatedResources));
					Set<String> additionalEagerLoadedNestedRelations = Collections.emptySet();
					populate(populatedResources, inclusions, resourceMap, entityMap, fieldPath, queryAdapter,
							parameterProvider, additionalEagerLoadedNestedRelations, populatedCache);
				}
			}
		}
	}

	private void checkNoRecursion(List<ResourceField> fieldPath) {
		int index = fieldPath.size();
		if (index >= 42) {
			throw new IllegalStateException("42 nested inclusions reached, aborting");
		}
	}

	/**
	 * No lookup specified for the field. Attempt to load relationship from
	 * original POJOs. Throw an InternalServerErrorException if the field is an
	 * Iterable and null.
	 */
	private Set<Resource> extractRelationshipField(List<Resource> sourceResources, ResourceField relationshipField,
			QueryAdapter queryAdapter, Map<ResourceIdentifier, Resource> resourceMap,
			Map<ResourceIdentifier, Object> entityMap, boolean lookUp) {
		Set<Resource> loadedEntities = new HashSet<>();
		for (Resource sourceResource : sourceResources) {
			ResourceIdentifier id = sourceResource.toIdentifier();

			Object source = entityMap.get(id);
			if (source != null && !(source instanceof Resource)) {
				Object targetEntity = relationshipField.getAccessor().getValue(source);

				if (!lookUp && Iterable.class.isAssignableFrom(relationshipField.getType()) && targetEntity == null) {
					throw new InternalServerErrorException(
							id + " relationship field collection '" + relationshipField.getJsonName()
									+ "' can not be null. Either set the relationship as an empty "
									+ Iterable.class.getCanonicalName() + " or add annotation @"
									+ JsonApiLookupIncludeAutomatically.class.getCanonicalName());
				}
				if (targetEntity == null) {
					continue;
				}
				List<Resource> targetIds =
						setupRelation(sourceResource, relationshipField, targetEntity, queryAdapter, resourceMap, entityMap);
				loadedEntities.addAll(targetIds);
			}
		}
		return loadedEntities;
	}

	/**
	 * Loads all related resources for the given resources and relationship
	 * field. It updates the relationship data of the source resources
	 * accordingly and returns the loaded resources for potential inclusion in
	 * the result resource.
	 */
	@SuppressWarnings("unchecked")
	private Set<Resource> lookupRelationshipField(Collection<Resource> sourceResources, ResourceField relationshipField,
			QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider,
			Map<ResourceIdentifier, Resource> resourceMap, Map<ResourceIdentifier, Object> entityMap) {
		if (sourceResources.isEmpty()) {
			return Collections.emptySet();
		}

		ResourceInformation resourceInformation = relationshipField.getParentResourceInformation();
		RegistryEntry registyEntry = resourceRegistry.getEntry(resourceInformation.getResourceType());

		List<Serializable> resourceIds = getIds(sourceResources, resourceInformation);

		boolean isMany = Iterable.class.isAssignableFrom(relationshipField.getType());

		Set<Resource> loadedTargets = new HashSet<>();

		@SuppressWarnings("rawtypes")
		RelationshipRepositoryAdapter relationshipRepository =
				registyEntry.getRelationshipRepositoryForType(relationshipField.getOppositeResourceType(), parameterProvider);
		if (relationshipRepository != null) {
			Map<Object, JsonApiResponse> responseMap;
			if (isMany) {
				responseMap = relationshipRepository.findBulkManyTargets(resourceIds, relationshipField, queryAdapter);
			}
			else {
				responseMap = relationshipRepository.findBulkOneTargets(resourceIds, relationshipField, queryAdapter);
			}

			for (Resource sourceResource : sourceResources) {
				Serializable sourceId = resourceInformation.parseIdString(sourceResource.getId());
				JsonApiResponse targetResponse = responseMap.get(sourceId);
				if (targetResponse != null && targetResponse.getEntity() != null) {
					Object targetEntity = targetResponse.getEntity();

					List<Resource> targets =
							setupRelation(sourceResource, relationshipField, targetEntity, queryAdapter, resourceMap, entityMap);
					loadedTargets.addAll(targets);
				}
				else {
					Nullable<Object> emptyData = (Nullable) Nullable
							.of(Iterable.class.isAssignableFrom(relationshipField.getType()) ? Collections.emptyList() : null);
					Relationship relationship = sourceResource.getRelationships().get(relationshipField.getJsonName());
					relationship.setData(emptyData);
				}
			}
		}

		return loadedTargets;
	}

	private List<Resource> setupRelation(Resource sourceResource, ResourceField relationshipField, Object targetEntity,
			QueryAdapter queryAdapter, Map<ResourceIdentifier, Resource> resourceMap,
			Map<ResourceIdentifier, Object> entityMap) {
		// set the relation
		String relationshipName = relationshipField.getJsonName();
		Map<String, Relationship> relationships = sourceResource.getRelationships();
		Relationship relationship = relationships.get(relationshipName);
		if (targetEntity instanceof Iterable) {
			List<Resource> targets = new ArrayList<>();
			for (Object targetElement : (Iterable<?>) targetEntity) {
				Resource targetResource = mergeResource(targetElement, queryAdapter, resourceMap, entityMap);
				targets.add(targetResource);
			}
			relationship.setData(Nullable.of((Object) util.toIds(targets)));
			return targets;
		}
		else {
			Resource targetResource = mergeResource(targetEntity, queryAdapter, resourceMap, entityMap);
			relationship.setData(Nullable.of((Object) targetResource.toIdentifier()));
			return Collections.singletonList(targetResource);
		}
	}

	private Resource mergeResource(Object targetEntity, QueryAdapter queryAdapter, Map<ResourceIdentifier, Resource> resourceMap,
			Map<ResourceIdentifier, Object> entityMap) {
		Resource targetResource = resourceMapper.toData(targetEntity, queryAdapter);
		ResourceIdentifier targetId = targetResource.toIdentifier();
		if (!resourceMap.containsKey(targetId)) {
			resourceMap.put(targetId, targetResource);
		}
		else {
			// TODO consider merging
			targetResource = resourceMap.get(targetId);
		}
		if (!(targetEntity instanceof Resource)) {
			entityMap.put(targetId, targetEntity);
		}
		return targetResource;
	}

	private List<Serializable> getIds(Collection<Resource> resources, ResourceInformation resourceInformation) {
		List<Serializable> ids = new ArrayList<>();
		for (Resource resource : resources) {
			Serializable id = resourceInformation.parseIdString(resource.getId());
			ids.add(id);
		}
		return ids;
	}

	/**
	 * Cache resource/field pairs already populated to avoid loops
	 */
	class PopulatedCache {

		private HashSet<String> processed = new HashSet<>();

		public void markProcessed(Resource resource, ResourceField field) {
			String key = getKey(resource, field);
			processed.add(key);
		}

		public Collection<Resource> filterProcessed(Collection<Resource> resources, ResourceField field) {
			Collection<Resource> result = new ArrayList<>();
			for (Resource resource : resources) {
				if (!wasProcessed(resource, field)) {
					result.add(resource);
					markProcessed(resource, field);
				}
			}
			return result;
		}

		public boolean wasProcessed(Resource resource, ResourceField field) {
			String key = getKey(resource, field);
			return processed.contains(key);
		}

		private String getKey(Resource resource, ResourceField field) {
			return resourceRegistry.getBaseResourceInformation(resource.getType()).getResourceType() + "@" + resource.getId()
					+ "@" + field.getUnderlyingName();
		}
	}

}
