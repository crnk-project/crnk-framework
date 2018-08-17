package io.crnk.core.engine.internal.document.mapper;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.result.ResultFactory;
import io.crnk.core.exception.InternalServerErrorException;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.core.resource.annotations.JsonApiLookupIncludeAutomatically;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.utils.Nullable;
import io.crnk.legacy.internal.QueryParamsAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IncludeLookupSetter {

	private static final Logger LOGGER = LoggerFactory.getLogger(IncludeLookupSetter.class);

	private final ResourceRegistry resourceRegistry;

	private final ResultFactory resultFactory;

	private ResourceMapper resourceMapper;

	private IncludeLookupUtil util;

	private IncludeRelationshipLoader relationshipLoader;

	// currently
	private boolean allowPagination = false;

	public IncludeLookupSetter(ResourceRegistry resourceRegistry, ResourceMapper resourceMapper,
							   PropertiesProvider propertiesProvider, ResultFactory resultFactory) {
		this.resourceMapper = resourceMapper;
		this.resourceRegistry = resourceRegistry;
		this.resultFactory = resultFactory;

		this.relationshipLoader = new IncludeRelationshipLoader(resourceRegistry, resultFactory, propertiesProvider);

		IncludeBehavior includeBehavior = IncludeLookupUtil.getIncludeBehavior(propertiesProvider);
		this.util = new IncludeLookupUtil(resourceRegistry, includeBehavior);
		this.allowPagination = propertiesProvider != null && Boolean.parseBoolean(propertiesProvider.getProperty(CrnkProperties
				.INCLUDE_PAGING_ENABLED));
	}

	public Result<Document> processInclusions(Document document, Object entity, QueryAdapter queryAdapter, DocumentMappingConfig
			mappingConfig) {

		QueryAdapter inclusionQueryAdapter = queryAdapter;
		if (!allowPagination && !(queryAdapter instanceof QueryParamsAdapter) && !queryAdapter.isEmpty()) {
			// offset/limit cannot properly work for nested inclusions if becomes cyclic
			inclusionQueryAdapter = queryAdapter.duplicate();

			RegistryEntry entry = resourceRegistry.getEntry(queryAdapter.getResourceInformation().getResourceType());
			PagingBehavior pagingBehavior = entry.getPagingBehavior();
			if (pagingBehavior != null) {
				inclusionQueryAdapter
						.setPagingSpec(pagingBehavior.createEmptyPagingSpec());
			}
		}

		IncludeRequest request = new IncludeRequest(entity, document, resourceRegistry, mappingConfig,
				inclusionQueryAdapter, util, resourceMapper);

		ArrayList<ResourceField> stack = new ArrayList<>();

		Result result = populate(request, request.getDataList(), stack);
		return result.map(it -> {
			request.removeDataFromIncluded();

			List<Resource> included = request.getIncluded();
			LOGGER.debug("adding {} inclusions", included.size());
			document.setIncluded(included);
			return document;
		});
	}

	private Result populate(IncludeRequest request, Collection<Resource> resourceList,
							List<ResourceField> fieldPath) {
		Result result = resultFactory.just(request);
		if (!resourceList.isEmpty()) {
			checkNoRecursion(fieldPath);
			Set<ResourceField> relationshipFields = util.getRelationshipFields(resourceList);
			for (ResourceField resourceField : relationshipFields) {
				List<ResourceField> nextFieldPath = new ArrayList(fieldPath);
				nextFieldPath.add(resourceField);
				result = result.merge(it -> populateField(request, resourceList, resourceField, nextFieldPath));
			}
		}
		return result;
	}

	private Result populateField(IncludeRequest request, Collection<Resource> resourceList,
								 ResourceField resourceField,
								 List<ResourceField> fieldPath) {
		ResourceInformation resourceInformation = resourceField.getParentResourceInformation();

		boolean includeRelationship = request.isInclusionRequest(fieldPath, resourceField);
		boolean serializeRelationId = request.isRelationIdSerialized(fieldPath);
		boolean requiresRelationData = serializeRelationId || includeRelationship;
		LOGGER.debug("populating field={} included={} serializeId={} ", resourceField.getUnderlyingName(), includeRelationship, serializeRelationId);

		if (requiresRelationData) {

			Collection<Resource> unpopulatedResourceList = request.filterProcessed(resourceList, resourceField);
			if (!unpopulatedResourceList.isEmpty()) {

				// only handle resources from the proper subtype where the
				// relationship is desired to be loaded
				List<Resource> resourcesByType = util.filterByType(unpopulatedResourceList, resourceInformation);
				List<Resource> resourcesWithField = util.filterByLoadedRelationship(resourcesByType, resourceField);

				// lookup resources by inspecting the POJOs in entityMap
				LookupIncludeBehavior fieldLookupIncludeBehavior = resourceField.getLookupIncludeAutomatically();

				Result<Set<Resource>> populatedResult;
				if (!includeRelationship && resourceField.hasIdField()) {
					// fill in @JsonApiRelationId into Relationship where possible
					fetchRelationFromEntity(request, resourcesWithField, resourceField,
							false, false, includeRelationship);

					// only ID is required and no lookup must take place
					// nothing to do
					populatedResult = resultFactory.just(Collections.emptySet());
				} else if (fieldLookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_ALWAYS) {
					// fill in @JsonApiRelationId into Relationship where possible
					fetchRelationFromEntity(request, resourcesWithField, resourceField,
							false, false, includeRelationship);

					// lookup resources by making repository calls
					populatedResult = relationshipLoader.lookupRelatedResource(request, resourcesWithField, resourceField);
				} else if (fieldLookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL) {
					// try to populate from entities
					Set<Resource> extractedResources =
							fetchRelationFromEntity(request, resourcesWithField, resourceField, true, true, includeRelationship);

					// do lookups where relationship data is null
					Collection<Resource> resourcesForLookup =
							util.findResourcesWithoutRelationshipToLoad(resourcesWithField, resourceField, request);

					populatedResult = relationshipLoader.lookupRelatedResource(request, resourcesForLookup, resourceField)
							.map(lookedupResources -> util.union(lookedupResources, extractedResources));
				} else {
					// do not do any lookups
					populatedResult = resultFactory.just(fetchRelationFromEntity(request, resourcesWithField, resourceField,
							false, true, includeRelationship));

					// set relationship data to null for single-valued relation.
					// fetchRelationFromEntity cannot differentiate between
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

				return populatedResult.merge(populated -> {
					// add inclusions and do nested population if requested as such
					if (includeRelationship && !populated.isEmpty()) {
						request.markForInclusion(populated);
						return populate(request, populated, fieldPath);
					}
					return resultFactory.just(request);
				});
			}
		}
		return resultFactory.just(request);
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
	private Set<Resource> fetchRelationFromEntity(IncludeRequest request, List<Resource> sourceResources, ResourceField
			relationshipField, boolean allowLookup, boolean fetchRelatedEntity, boolean mustInclude) {
		Set<Resource> loadedResources = new HashSet<>();
		for (Resource sourceResource : sourceResources) {
			ResourceIdentifier id = sourceResource.toIdentifier();

			Object sourceEntity = request.getEntity(id);
			if (sourceEntity != null && !(sourceEntity instanceof Resource)) {

				Object relatedEntity = null;
				if (fetchRelatedEntity) {
					relatedEntity = relationshipField.getAccessor().getValue(sourceEntity);
					if (!allowLookup && Iterable.class.isAssignableFrom(relationshipField.getType()) && relatedEntity == null) {
						// note that single-valued relations are allowed to be null
						throw new InternalServerErrorException(
								id + " relationship field collection '" + relationshipField.getJsonName()
										+ "' can not be null. Either set the relationship as an empty "
										+ Iterable.class.getCanonicalName() + " or add annotation @"
										+ JsonApiLookupIncludeAutomatically.class.getCanonicalName());
					}
				}

				// attempt to work with full relationship and fallback to relationshipId where possible
				if (relatedEntity != null) {
					List<Resource> relatedResources =
							request.setupRelation(sourceResource, relationshipField, relatedEntity);
					loadedResources.addAll(relatedResources);
				} else if (relationshipField.hasIdField()) {
					Object relatedEntityID = relationshipField.getIdAccessor().getValue(sourceEntity);
					request.setupRelationId(sourceResource, relationshipField, relatedEntityID);
					if (fetchRelatedEntity && relatedEntityID != null && !allowLookup && mustInclude) {
						throw new IllegalStateException("inconsistent relationship '" + relationshipField.getUnderlyingName()
								+ "' for " + id + ", id "
								+ "set to " + relatedEntityID + ", but related object is null and lookup disabled");
					}
				}
			}
		}
		return loadedResources;
	}


}
