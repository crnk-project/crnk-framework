package io.crnk.core.engine.internal.document.mapper;

import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.core.utils.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class IncludeRequest {


	private static final Logger LOGGER = LoggerFactory.getLogger(IncludeLookupSetter.class);

	private final IncludePopulatedCache populatedCache;

	private final HashSet<ResourceIdentifier> inclusions;

	private final List<Resource> dataList;

	private final QueryAdapter queryAdapter;

	private final IncludeLookupUtil util;

	private final DocumentMappingConfig mappingConfig;

	private final ResourceRegistry resourceRegistry;

	private final ResourceMapper resourceMapper;

	private Map<ResourceIdentifier, Resource> dataMap;

	private Map<ResourceIdentifier, Object> entityMap;

	private Map<ResourceIdentifier, Resource> resourceMap;

	public IncludeRequest(Object entity, Document document, ResourceRegistry resourceRegistry,
						  DocumentMappingConfig mappingConfig, QueryAdapter queryAdapter,
						  IncludeLookupUtil util, ResourceMapper resourceMapper) {
		this.resourceMapper = resourceMapper;
		this.resourceRegistry = resourceRegistry;
		this.mappingConfig = mappingConfig;
		this.queryAdapter = queryAdapter;
		this.util = util;

		List<Object> entityList = DocumentMapperUtil.toList(entity);
		dataList = DocumentMapperUtil.toList(document.getData().get());
		dataMap = new HashMap<>();
		entityMap = new HashMap<>();
		for (int i = 0; i < dataList.size(); i++) {
			Resource dataElement = dataList.get(i);
			ResourceIdentifier id = dataElement.toIdentifier();
			entityMap.put(id, entityList.get(i));
			dataMap.put(id, dataElement);
		}

		resourceMap = new HashMap<>();
		resourceMap.putAll(dataMap);

		inclusions = new HashSet<>();
		populatedCache = new IncludePopulatedCache(resourceRegistry);
	}

	public synchronized boolean isInclusionRequest(List<ResourceField> fieldPath,
												   ResourceField resourceField) {
		return util.isInclusionRequested(queryAdapter, fieldPath)
				|| resourceField.getSerializeType() == SerializeType.EAGER;
	}

	public synchronized List<Resource> getDataList() {
		return dataList;
	}

	public synchronized void removeDataFromIncluded() {
		// no need to include resources included in the data section
		inclusions.removeAll(dataMap.keySet());
	}

	public synchronized List<Resource> getIncluded() {
		// setup included section
		ArrayList<Resource> included = new ArrayList<>();
		for (ResourceIdentifier inclusionId : inclusions) {
			Resource includedResource = resourceMap.get(inclusionId);
			PreconditionUtil.verify(includedResource != null, "resource with id=%d not found", inclusionId);
			included.add(includedResource);
		}
		Collections.sort(included);
		LOGGER.debug("Extracted included resources {}", included.toString());
		return included;
	}

	public synchronized boolean isRelationIdSerialized(List<ResourceField> resourceFieldPath) {
		ResourceField lastResourceField = resourceFieldPath.get(resourceFieldPath.size() - 1);

		Set<String> fieldsWithEnforcedIdSerialization = mappingConfig.getFieldsWithEnforcedIdSerialization();
		return lastResourceField.getSerializeType() != SerializeType.LAZY
				|| resourceFieldPath.size() == 1 && fieldsWithEnforcedIdSerialization.contains(lastResourceField.getJsonName());
	}

	public synchronized Collection<Resource> filterProcessed(Collection<Resource> resourceList, ResourceField resourceField) {
		return populatedCache.filterProcessed(resourceList, resourceField);
	}

	public synchronized void markForInclusion(Set<Resource> resources) {
		inclusions.addAll(util.toIds(resources));
	}

	public synchronized Resource merge(Object targetEntity) {
		ResourceMappingConfig resourceMappingConfig = mappingConfig.getResourceMapping();
		Resource targetResource = resourceMapper.toData(targetEntity, queryAdapter, resourceMappingConfig);
		ResourceIdentifier targetId = targetResource.toIdentifier();
		if (!resourceMap.containsKey(targetId)) {
			resourceMap.put(targetId, targetResource);
		} else {
			// TODO consider merging
			targetResource = resourceMap.get(targetId);
		}
		if (!(targetEntity instanceof Resource)) {
			entityMap.put(targetId, targetEntity);
		}
		return targetResource;
	}

	public synchronized void setupRelationId(Resource sourceResource, ResourceField relationshipField, Object targetEntityId) {
		// set the relation
		String relationshipName = relationshipField.getJsonName();
		Map<String, Relationship> relationships = sourceResource.getRelationships();
		Relationship relationship = relationships.get(relationshipName);

		String oppositeType = relationshipField.getOppositeResourceType();
		RegistryEntry entry = resourceRegistry.getEntry(oppositeType);
		PreconditionUtil.verify(entry != null, "opposite type %s not found for relationship %s", oppositeType, relationshipName);
		ResourceInformation targetResourceInformation = entry.getResourceInformation();

		if (targetEntityId instanceof Iterable) {
			List<ResourceIdentifier> targetIds = new ArrayList<>();
			for (Object targetElementId : (Iterable<?>) targetEntityId) {
				targetIds.add(util.idToResourceId(targetResourceInformation, targetElementId));
			}
			relationship.setData(Nullable.of(targetIds));
		} else {
			ResourceIdentifier targetResourceId = util.idToResourceId(targetResourceInformation, targetEntityId);
			relationship.setData(Nullable.of(targetResourceId));
		}
	}

	public synchronized List<Resource> setupRelation(Resource sourceResource, ResourceField relationshipField,
													 Object targetEntity) {
		// set the relation
		String relationshipName = relationshipField.getJsonName();
		Map<String, Relationship> relationships = sourceResource.getRelationships();
		Relationship relationship = relationships.get(relationshipName);
		if (targetEntity instanceof Iterable) {
			List<Resource> targets = new ArrayList<>();
			for (Object targetElement : (Iterable<?>) targetEntity) {
				Resource targetResource = merge(targetElement);
				targets.add(targetResource);
			}
			relationship.setData(Nullable.of(util.toIds(targets)));
			return targets;
		} else {
			Resource targetResource = merge(targetEntity);
			relationship.setData(Nullable.of(targetResource.toIdentifier()));
			return Collections.singletonList(targetResource);
		}
	}

	public synchronized QueryAdapter getQueryAdapter() {
		return queryAdapter;
	}

	public synchronized boolean containsResource(ResourceIdentifier id) {
		return resourceMap.containsKey(id);
	}

	public synchronized Resource getResource(ResourceIdentifier id) {
		return Objects.requireNonNull(resourceMap.get(id));
	}

	public synchronized Object getEntity(ResourceIdentifier id) {
		return entityMap.get(id);
	}
}
