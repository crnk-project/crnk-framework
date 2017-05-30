package io.crnk.core.engine.internal.dispatcher.controller;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInstanceBuilder;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.information.resource.ResourceAttributesBridge;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.properties.ResourceFieldImmutableWriteBehavior;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ResourceException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ResourceUpsert extends BaseController {

	protected final ResourceRegistry resourceRegistry;
	protected final ObjectMapper objectMapper;
	final TypeParser typeParser;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	protected DocumentMapper documentMapper;

	private PropertiesProvider propertiesProvider;

	public ResourceUpsert(ResourceRegistry resourceRegistry, PropertiesProvider propertiesProvider, TypeParser typeParser, ObjectMapper objectMapper, DocumentMapper documentMapper) {
		this.propertiesProvider = propertiesProvider;
		this.resourceRegistry = resourceRegistry;
		this.typeParser = typeParser;
		this.objectMapper = objectMapper;
		this.documentMapper = documentMapper;
	}

	private static boolean allTypesTheSame(Iterable<ResourceIdentifier> linkages) {
		String type = linkages.iterator()
				.hasNext() ? linkages.iterator()
				.next()
				.getType() : null;
		for (ResourceIdentifier linkageData : linkages) {
			if (!Objects.equals(type, linkageData.getType())) {
				return false;
			}
		}
		return true;
	}

	protected Object newResource(ResourceInformation resourceInformation, Resource dataBody) {
		ResourceInstanceBuilder<?> builder = resourceInformation.getInstanceBuilder();
		return builder.buildResource(dataBody);
	}

	protected void setId(Resource dataBody, Object instance, ResourceInformation resourceInformation) {
		if (dataBody.getId() != null) {
			String id = dataBody.getId();

			Serializable castedId = resourceInformation.parseIdString(id);

			ResourceField idField = resourceInformation.getIdField();
			idField.getAccessor().setValue(instance, castedId);
		}
	}

	protected Set<String> getLoadedRelationshipNames(Resource resourceBody) {
		Set<String> result = new HashSet<>();
		for (Entry<String, Relationship> entry : resourceBody.getRelationships().entrySet()) {
			if (entry.getValue() != null && entry.getValue().getData() != null) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	protected void setAttributes(Resource dataBody, Object instance, ResourceInformation resourceInformation) {
		if (dataBody.getAttributes() != null) {

			ResourceAttributesBridge resourceAttributesBridge = resourceInformation.getAttributeFields();

			for (Map.Entry<String, JsonNode> entry : dataBody.getAttributes().entrySet()) {
				String attributeName = entry.getKey();

				ResourceField field = resourceInformation.findAttributeFieldByName(attributeName);
				if (canModifyField(resourceInformation, attributeName, field)) {
					resourceAttributesBridge.setProperty(objectMapper, instance, entry.getValue(), entry.getKey());
				} else {
					handleImmutableField(entry.getKey());
				}
			}

		}
	}

	protected void saveRelations(QueryAdapter queryAdapter, Object savedResource, RegistryEntry registryEntry, Resource dataBody,
								 RepositoryMethodParameterProvider parameterProvider) {
		if (dataBody.getRelationships() != null) {
			for (Map.Entry<String, Relationship> property : dataBody.getRelationships().entrySet()) {
				Relationship relationship = property.getValue();
				if (relationship != null && relationship.getData().isPresent()) {
					Object data = relationship.getData();
					if (data instanceof Iterable) {
						//noinspection unchecked
						saveRelationsField(queryAdapter, savedResource, registryEntry, (Map.Entry) property, registryEntry
								.getResourceInformation(), parameterProvider);
					} else {
						//noinspection unchecked
						saveRelationField(queryAdapter, savedResource, registryEntry, (Map.Entry) property, registryEntry
								.getResourceInformation(), parameterProvider);
					}
				}
			}
		}
	}

	private void saveRelationsField(QueryAdapter queryAdapter, Object savedResource, RegistryEntry registryEntry,
									Map.Entry<String, Iterable<ResourceIdentifier>> property,
									ResourceInformation resourceInformation,
									RepositoryMethodParameterProvider parameterProvider) {
		if (!allTypesTheSame(property.getValue())) {
			throw new ResourceException("Not all types are the same for linkage: " + property.getKey());
		}

		String type = getLinkageType(property.getValue());
		RegistryEntry relationRegistryEntry = getRelationRegistryEntry(type);
		@SuppressWarnings("unchecked")
		Class<? extends Serializable> relationshipIdClass = (Class<? extends Serializable>) relationRegistryEntry
				.getResourceInformation()
				.getIdField()
				.getType();
		List<Serializable> castedRelationIds = new LinkedList<>();

		for (ResourceIdentifier linkageData : property.getValue()) {
			Serializable castedRelationshipId = typeParser.parse(linkageData.getId(), relationshipIdClass);
			castedRelationIds.add(castedRelationshipId);
		}

		Class<?> relationshipClass = relationRegistryEntry.getResourceInformation()
				.getResourceClass();
		RelationshipRepositoryAdapter relationshipRepository = registryEntry
				.getRelationshipRepositoryForClass(relationshipClass, parameterProvider);
		ResourceField relationshipField = resourceInformation.findRelationshipFieldByName(property.getKey());

		if (canModifyField(resourceInformation, property.getKey(), relationshipField)) {
			//noinspection unchecked
			relationshipRepository.setRelations(savedResource, castedRelationIds,
					relationshipField, queryAdapter);
		} else {
			handleImmutableField(property.getKey());
		}
	}

	private void handleImmutableField(String fieldName) {
		String strBehavior = propertiesProvider.getProperty(CrnkProperties.RESOURCE_FIELD_IMMUTABLE_WRITE_BEHAVIOR);
		ResourceFieldImmutableWriteBehavior behavior = strBehavior != null ? ResourceFieldImmutableWriteBehavior.valueOf(strBehavior) : ResourceFieldImmutableWriteBehavior.IGNORE;
		if (behavior == ResourceFieldImmutableWriteBehavior.IGNORE) {
			logger.debug("attribute '{}' is immutable", fieldName);
		} else {
			throw new BadRequestException("attribute '" + fieldName + "' is immutable");
		}
	}

	/**
	 * Allows to check whether the given field can be written.
	 *
	 * @param resourceInformation
	 * @param fieldName
	 * @param field               from the information model or null if is a dynamic field (like JsonAny).
	 */
	protected abstract boolean canModifyField(ResourceInformation resourceInformation, String fieldName, ResourceField field);

	protected String getLinkageType(Iterable<ResourceIdentifier> linkages) {
		return linkages.iterator()
				.hasNext() ? linkages.iterator()
				.next()
				.getType() : null;
	}

	private void saveRelationField(QueryAdapter queryAdapter, Object savedResource, RegistryEntry registryEntry,
								   Map.Entry<String, ResourceIdentifier> property, ResourceInformation resourceInformation,
								   RepositoryMethodParameterProvider parameterProvider) {
		RegistryEntry relationRegistryEntry = getRelationRegistryEntry(property.getValue()
				.getType());

		@SuppressWarnings("unchecked")
		Class<? extends Serializable> relationshipIdClass = (Class<? extends Serializable>) relationRegistryEntry
				.getResourceInformation()
				.getIdField()
				.getType();
		Serializable castedRelationshipId = typeParser.parse(property.getValue()
				.getId(), relationshipIdClass);

		Class<?> relationshipClass = relationRegistryEntry.getResourceInformation()
				.getResourceClass();
		RelationshipRepositoryAdapter relationshipRepository = registryEntry
				.getRelationshipRepositoryForClass(relationshipClass, parameterProvider);
		ResourceField relationshipField = resourceInformation.findRelationshipFieldByName(property.getKey());
		//noinspection unchecked
		relationshipRepository.setRelation(savedResource, castedRelationshipId, relationshipField,
				queryAdapter);
	}

	private RegistryEntry getRelationRegistryEntry(String type) {
		RegistryEntry relationRegistryEntry = resourceRegistry.getEntry(type);
		if (relationRegistryEntry == null) {
			throw new ResourceNotFoundException(type);
		}
		return relationRegistryEntry;
	}

	Object buildNewResource(RegistryEntry registryEntry, Resource dataBody, String resourceName) {
		if (dataBody == null) {
			throw new ResourceException("No data field in the body.");
		}
		if (!resourceName.equals(dataBody.getType())) {
			throw new ResourceException(String.format("Inconsistent type definition between path and body: body type: " +
							"%s, request type: %s",
					dataBody.getType(),
					resourceName));
		}
		try {
			return registryEntry.getResourceInformation()
					.getResourceClass()
					.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ResourceException(
					String.format("couldn't create a new instance of %s", registryEntry.getResourceInformation()
							.getResourceClass()));
		}
	}

	protected void setRelations(Object newResource, RegistryEntry registryEntry, Resource resource, QueryAdapter
			queryAdapter,
								RepositoryMethodParameterProvider parameterProvider) {
		if (resource.getRelationships() != null) {
			for (Map.Entry<String, Relationship> property : resource.getRelationships().entrySet()) {
				String propertyName = property.getKey();
				Relationship relationship = property.getValue();
				if (relationship != null) {

					ResourceInformation resourceInformation = registryEntry.getResourceInformation();
					ResourceField field = resourceInformation.findRelationshipFieldByName(propertyName);
					if (field == null) {
						throw new ResourceException(String.format("Invalid relationship name: %s for %s", property.getKey(), resourceInformation.getResourceType()));
					}
					if (field.isCollection()) {
						//noinspection unchecked
						setRelationsField(newResource,
								registryEntry,
								property,
								queryAdapter,
								parameterProvider);
					} else {
						//noinspection unchecked
						setRelationField(newResource, registryEntry, propertyName, relationship, queryAdapter, parameterProvider);
					}
				}
			}
		}
	}

	protected void setRelationsField(Object newResource, RegistryEntry registryEntry,
									 Map.Entry<String, Relationship> property, QueryAdapter queryAdapter,
									 RepositoryMethodParameterProvider parameterProvider) {
		Relationship relationship = property.getValue();
		if (relationship.getData().isPresent()) {
			String propertyName = property.getKey();
			ResourceField relationshipField = registryEntry.getResourceInformation()
					.findRelationshipFieldByName(propertyName);
			Class idFieldType = null;
			List relationships = new LinkedList<>();
			for (ResourceIdentifier resourceId : relationship.getCollectionData().get()) {
				RegistryEntry entry = resourceRegistry.getEntry(resourceId.getType());
				idFieldType = entry.getResourceInformation()
						.getIdField()
						.getType();
				Serializable castedRelationshipId = typeParser.parse(resourceId.getId(), idFieldType);
				Object relationObject = fetchRelatedObject(entry, castedRelationshipId, parameterProvider, queryAdapter);
				relationships.add(relationObject);
			}
			PropertyUtils.setProperty(newResource, relationshipField.getUnderlyingName(), relationships);
		}
	}

	protected void setRelationField(Object newResource, RegistryEntry registryEntry,
									String relationshipName, Relationship relationship, QueryAdapter queryAdapter,
									RepositoryMethodParameterProvider parameterProvider) {

		if (relationship.getData().isPresent()) {
			ResourceIdentifier relationshipId = (ResourceIdentifier) relationship.getData().get();

			ResourceField relationshipFieldByName = registryEntry.getResourceInformation()
					.findRelationshipFieldByName(relationshipName);

			if (relationshipFieldByName == null) {
				throw new ResourceException(String.format("Invalid relationship name: %s", relationshipName));
			}

			Object relationObject;
			if (relationshipId != null) {
				RegistryEntry entry = resourceRegistry.getEntry(relationshipId.getType());
				Class idFieldType = entry.getResourceInformation()
						.getIdField()
						.getType();
				Serializable castedRelationshipId = typeParser.parse(relationshipId.getId(), idFieldType);

				relationObject = fetchRelatedObject(entry, castedRelationshipId, parameterProvider, queryAdapter);
			} else {
				relationObject = null;
			}
			relationshipFieldByName.getAccessor().setValue(newResource, relationObject);
		}
	}

	protected Object fetchRelatedObject(RegistryEntry entry, Serializable relationId, RepositoryMethodParameterProvider parameterProvider,
										QueryAdapter queryAdapter) {
		return entry.getResourceRepository(parameterProvider).findOne(relationId, queryAdapter).getEntity();
	}
}
