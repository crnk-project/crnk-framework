package io.crnk.core.engine.internal.document.mapper;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.result.ResultFactory;
import io.crnk.core.exception.InvalidResourceException;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.utils.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IncludeRelationshipLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(IncludeRelationshipLoader.class);

	private final ResultFactory resultFactory;

	private ResourceRegistry resourceRegistry;

	private PropertiesProvider propertiesProvider;

	private boolean exceptionOnMissingRelatedResource = true;

	public IncludeRelationshipLoader(ResourceRegistry resourceRegistry, ResultFactory resultFactory, PropertiesProvider propertiesProvider) {
		this.resourceRegistry = resourceRegistry;
		this.resultFactory = resultFactory;
		this.propertiesProvider = propertiesProvider;

		if (propertiesProvider != null && propertiesProvider.getProperty(CrnkProperties.EXCEPTION_ON_MISSING_RELATED_RESOURCE) != null) {
			exceptionOnMissingRelatedResource = Boolean.parseBoolean(propertiesProvider.getProperty(CrnkProperties.EXCEPTION_ON_MISSING_RELATED_RESOURCE));
		}

	}

	/**
	 * Loads all related resources for the given resources and relationship
	 * field. It updates the relationship data of the source resources
	 * accordingly and returns the loaded resources for potential inclusion in
	 * the result resource.
	 */
	@SuppressWarnings("unchecked")
	public Result<Set<Resource>> lookupRelatedResource(IncludeRequest request, Collection<Resource> sourceResources,
													   ResourceField relationshipField) {

		LOGGER.debug("looking up {} for {}", relationshipField, sourceResources);
		if (sourceResources.isEmpty()) {
			return resultFactory.just(Collections.emptySet());
		}

		// directly load where relationship data is available
		Collection<Resource> sourceResourcesWithData = new ArrayList<>();
		Collection<Resource> sourceResourcesWithoutData = new ArrayList<>();

		for (Resource sourceResource : sourceResources) {
			boolean present = sourceResource.getRelationships().get(relationshipField.getJsonName()).getData().isPresent();
			if (present) {
				sourceResourcesWithData.add(sourceResource);
			} else {
				sourceResourcesWithoutData.add(sourceResource);
			}

		}

		Set<Resource> relatedResources = new HashSet<>();


		Result<Set<Resource>> result = resultFactory.just(relatedResources);
		if (!sourceResourcesWithData.isEmpty()) {
			LOGGER.debug("fetching {} by relationship id", sourceResourcesWithData);
			Result<Set<Resource>> lookupWithId = lookupRelatedResourcesWithId(request, sourceResourcesWithData,
					relationshipField);
			result = result.zipWith(lookupWithId, this::mergeList);
		}
		if (!sourceResourcesWithoutData.isEmpty()) {
			LOGGER.debug("looking up relationship for {}", sourceResourcesWithData);
			Result<Set<Resource>> lookupWithoutData = lookupRelatedResourceWithRelationship(request, sourceResourcesWithoutData,
					relationshipField);
			result = result.zipWith(lookupWithoutData, this::mergeList);
		}
		return result;
	}

	private Set<Resource> mergeList(Set<Resource> set1, Set<Resource> set2) {
		Set<Resource> set = new HashSet<>();
		set.addAll(set1);
		set.addAll(set2);
		return set;
	}

	public Result<Set<Resource>> lookupRelatedResourcesWithId(IncludeRequest request, Collection<Resource> sourceResources,
															  ResourceField relationshipField) {

		String oppositeResourceType = relationshipField.getOppositeResourceType();
		RegistryEntry oppositeEntry = resourceRegistry.getEntry(oppositeResourceType);
		if (oppositeEntry == null) {
			throw new RepositoryNotFoundException("no resource with type " + oppositeResourceType + " found");
		}
		ResourceInformation oppositeResourceInformation = oppositeEntry.getResourceInformation();
		ResourceRepositoryAdapter oppositeResourceRepository = oppositeEntry.getResourceRepository();
		if (oppositeResourceRepository == null) {
			throw new RepositoryNotFoundException(
					"no relationship repository found for " + oppositeResourceInformation.getResourceType());
		}

		Set<Resource> related = new HashSet<>();

		Set<Object> relatedIdsToLoad = new HashSet<>();
		Map<Object, List<ResourceIdentifier>> mapRelatedIdsToLoadToResourceIdentifier = new HashMap<>();
		for (Resource sourceResource : sourceResources) {
			Relationship relationship = sourceResource.getRelationships().get(relationshipField.getJsonName());
			PreconditionUtil.verify(relationship.getData().isPresent(), "expected relationship data to be loaded for @JsonApiResourceId annotated field, sourceType=%d sourceId=%d, relationshipName=%s", sourceResource.getType(), sourceResource.getId(), relationshipField.getJsonName());

			if (relationship.getData().get() != null) {
				for (ResourceIdentifier id : relationship.getCollectionData().get()) {
					if (request.containsResource(id)) {
						// load from cache
						related.add(request.getResource(id));
					} else {
						relatedIdsToLoad.add(oppositeResourceInformation.parseIdString(id.getId()));
						// ResourceIdentifier may have the wrong type, e.g. when resource is a subtype of declared type in source resource,
						// so we store the resource identifier to be able to set the correct type later
						mapRelatedIdsToLoadToResourceIdentifier.computeIfAbsent(id.getId(), k -> new ArrayList<>()).add(id);
					}
				}
			}
		}

		LOGGER.debug("fetching {}", relatedIdsToLoad);

		if (!relatedIdsToLoad.isEmpty()) {
			QueryAdapter queryAdapter = request.getQueryAdapter();
			Result<JsonApiResponse> responseResult = oppositeResourceRepository.findAll(relatedIdsToLoad, queryAdapter);
			return responseResult.map(response -> {
				LOGGER.debug("got {}", response);
				Collection responseList = (Collection) response.getEntity();
				for (Object responseEntity : responseList) {
					Resource relatedResource = request.merge(responseEntity);
					// ResourceIdentifier may have the wrong type, e.g. when resource is a subtype of declared type in source resource,
					// so we set the correct type here
					List<ResourceIdentifier> resourceIdentifiers = mapRelatedIdsToLoadToResourceIdentifier.get(relatedResource.getId());
					if (resourceIdentifiers != null) {
						for(ResourceIdentifier resourceIdentifier : resourceIdentifiers) {
							resourceIdentifier.setType(relatedResource.getType());
						}
					} else {
						throw new InvalidResourceException("type=" + relationshipField.getOppositeResourceType() + ", "
								+ "id=" + relatedResource.getId() + " : There must be an issue with serializing this id.");
					}
					related.add(relatedResource);
					Object responseEntityId = oppositeResourceInformation.getId(responseEntity);
					relatedIdsToLoad.remove(responseEntityId);
				}
				if (!relatedIdsToLoad.isEmpty() && exceptionOnMissingRelatedResource) {
					throw new ResourceNotFoundException("type=" + relationshipField.getOppositeResourceType() + ", "
							+ "ids=" + relatedIdsToLoad);
				}
				return related;
			});
		}

		return resultFactory.just(related);
	}

	private Result<Set<Resource>> lookupRelatedResourceWithRelationship(IncludeRequest request, Collection<Resource>
			sourceResources, ResourceField relationshipField) {

		ResourceInformation resourceInformation = relationshipField.getResourceInformation();
		RegistryEntry registyEntry = resourceRegistry.getEntry(resourceInformation.getResourceType());
		List<Serializable> resourceIds = IncludeLookupUtil.getIds(sourceResources, resourceInformation);
		boolean isMany = Iterable.class.isAssignableFrom(relationshipField.getType());

		QueryAdapter queryAdapter = request.getQueryAdapter();
		RelationshipRepositoryAdapter relationshipRepository =
				registyEntry.getRelationshipRepository(relationshipField);
		if (relationshipRepository == null) {
			throw new RepositoryNotFoundException("no relationship repository found for " + resourceInformation.getResourceType
					() + "." + relationshipField.getUnderlyingName());
		}

		Result<Map<Object, JsonApiResponse>> responseMapResult;
		if (isMany) {
			responseMapResult = relationshipRepository.findBulkManyTargets(resourceIds, relationshipField, queryAdapter);
		} else {
			responseMapResult = relationshipRepository.findBulkOneTargets(resourceIds, relationshipField, queryAdapter);
		}
		return responseMapResult.map(responseMap -> {
			LOGGER.debug("got {}", responseMap);

			Set<Resource> loadedTargets = new HashSet<>();
			for (Resource sourceResource : sourceResources) {
				Serializable sourceId = resourceInformation.parseIdString(sourceResource.getId());
				JsonApiResponse targetResponse = responseMap.get(sourceId);
				if (targetResponse != null && targetResponse.getEntity() != null) {
					Object targetEntity = targetResponse.getEntity();

					List<Resource> targets = request.setupRelation(sourceResource, relationshipField, targetEntity);
					loadedTargets.addAll(targets);
				} else {
					Nullable<Object> emptyData = Nullable.of(
							Iterable.class.isAssignableFrom(relationshipField.getType()) ? Collections.emptyList() : null);
					Relationship relationship = sourceResource.getRelationships().get(relationshipField.getJsonName());
					relationship.setData(emptyData);
				}
			}
			return loadedTargets;
		});
	}
}
