package io.crnk.core.engine.internal.dispatcher.controller;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.filter.ResourceModificationFilter;
import io.crnk.core.engine.filter.ResourceRelationshipModificationType;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.*;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.properties.ResourceFieldImmutableWriteBehavior;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.exception.RequestBodyException;
import io.crnk.core.exception.ResourceException;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;

public abstract class ResourceUpsert extends ResourceIncludeField {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	protected final ObjectMapper objectMapper;

	protected final ResourceFilterDirectory resourceFilterDirectory;

	protected final List<ResourceModificationFilter> modificationFilters;

	private PropertiesProvider propertiesProvider;

	public ResourceUpsert(ResourceRegistry resourceRegistry, PropertiesProvider propertiesProvider, TypeParser typeParser,
						  ObjectMapper objectMapper, DocumentMapper documentMapper,
						  List<ResourceModificationFilter> modificationFilters) {
		super(resourceRegistry, typeParser, documentMapper);
		this.propertiesProvider = propertiesProvider;
		this.modificationFilters = modificationFilters;
		this.objectMapper = objectMapper;
		this.resourceFilterDirectory = documentMapper != null ? documentMapper.getFilterBehaviorManager() : null;
	}

	protected Resource getRequestBody(Document requestDocument, JsonPath path, HttpMethod method) {
		String resourceType = path.getResourceType();

		assertRequestDocument(requestDocument, method, resourceType);

		if (!requestDocument.getData().isPresent() || requestDocument.getData().get() == null) {
			throw new RequestBodyException(method, resourceType, "No data field in the body.");
		}
		if (requestDocument.getData().get() instanceof Collection) {
			throw new RequestBodyException(method, resourceType, "Multiple data in body");
		}

		Resource resourceBody = (Resource) requestDocument.getData().get();
		RegistryEntry bodyRegistryEntry = resourceRegistry.getEntry(resourceBody.getType());
		if (bodyRegistryEntry == null) {
			throw new RepositoryNotFoundException(resourceBody.getType());
		}
		return resourceBody;
	}

	protected RegistryEntry getRegistryEntry(JsonPath jsonPath) {
		String resourceType = jsonPath.getResourceType();
		return getRegistryEntry(resourceType);
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

			for (Map.Entry<String, JsonNode> entry : dataBody.getAttributes().entrySet()) {
				String attributeName = entry.getKey();

				setAttribute(resourceInformation, instance, attributeName, entry.getValue());
			}

		}
	}

	private void setAttribute(ResourceInformation resourceInformation, Object instance, String attributeName, JsonNode valueNode) {
		ResourceField field = resourceInformation.findAttributeFieldByName(attributeName);
		if (canModifyField(resourceInformation, attributeName, field)) {
			try {
				if (field != null) {
					Type valueType = field.getGenericType();
					Object value;
					if (valueNode != null) {
						JavaType jacksonValueType = objectMapper.getTypeFactory().constructType(valueType);
						ObjectReader reader = objectMapper.reader().forType(jacksonValueType);
						value = reader.readValue(valueNode);
					} else {
						value = null;
					}
					for (ResourceModificationFilter filter : modificationFilters) {
						value = filter.modifyAttribute(instance, field, attributeName, value);
					}
					field.getAccessor().setValue(instance, value);
				} else if (resourceInformation.getAnyFieldAccessor() != null) {
					AnyResourceFieldAccessor anyFieldAccessor = resourceInformation.getAnyFieldAccessor();
					Object value = objectMapper.reader().forType(Object.class).readValue(valueNode);
					for (ResourceModificationFilter filter : modificationFilters) {
						value = filter.modifyAttribute(instance, field, attributeName, value);
					}
					anyFieldAccessor.setValue(instance, attributeName, value);
				}
			} catch (IOException e) {
				throw new ResourceException(
						String.format("Exception while setting %s.%s=%s due to %s", instance, attributeName, valueNode, e.getMessage()), e);
			}
		}
	}

	/**
	 * Allows to check whether the given field can be written.
	 *
	 * @param field from the information model or null if is a dynamic field (like JsonAny).
	 */
	protected boolean canModifyField(ResourceInformation resourceInformation, String fieldName, ResourceField field) {
		if (field == null) {
			return true;
		}

		HttpMethod method = getHttpMethod();
		ResourceFieldAccess access = field.getAccess();
		boolean modifiable = method == HttpMethod.POST ? access.isPostable() : access.isPatchable();
		FilterBehavior filterBehavior = modifiable ? FilterBehavior.NONE : getDefaultFilterBehavior();
		filterBehavior = filterBehavior.merge(resourceFilterDirectory.get(field, method));

		if (filterBehavior == FilterBehavior.NONE) {
			return true;
		} else if (filterBehavior == FilterBehavior.FORBIDDEN) {
			throw new ForbiddenException("field '" + fieldName + "' cannot be modified");
		} else {
			PreconditionUtil.assertEquals("unknown behavior", FilterBehavior.IGNORED, filterBehavior);
			return false;
		}
	}


	public FilterBehavior getDefaultFilterBehavior() {
		String strBehavior = propertiesProvider.getProperty(CrnkProperties.RESOURCE_FIELD_IMMUTABLE_WRITE_BEHAVIOR);
		ResourceFieldImmutableWriteBehavior behavior =
				strBehavior != null ? ResourceFieldImmutableWriteBehavior.valueOf(strBehavior)
						: ResourceFieldImmutableWriteBehavior.IGNORE;
		return behavior == ResourceFieldImmutableWriteBehavior.IGNORE ? FilterBehavior.IGNORED : FilterBehavior.FORBIDDEN;
	}

	protected abstract HttpMethod getHttpMethod();


	Object buildNewResource(RegistryEntry registryEntry, Resource dataBody, String resourceName) {
		PreconditionUtil.verify(dataBody != null, "No data field in the body.");
		PreconditionUtil.verify(resourceName.equals(dataBody.getType()), "Inconsistent type definition between path and body: body type: " +
						"%s, request type: %s",
				dataBody.getType(),
				resourceName);
		Class resourceClass = registryEntry.getResourceInformation()
				.getResourceClass();
		return ClassUtils.newInstance(resourceClass);
	}

	protected void setRelations(Object newResource, RegistryEntry registryEntry, Resource resource, QueryAdapter
			queryAdapter, RepositoryMethodParameterProvider parameterProvider, boolean ignoreMissing) {
		if (resource.getRelationships() != null) {
			for (Map.Entry<String, Relationship> property : resource.getRelationships().entrySet()) {
				String propertyName = property.getKey();
				Relationship relationship = property.getValue();
				if (relationship != null) {

					ResourceInformation resourceInformation = registryEntry.getResourceInformation();
					ResourceField field = resourceInformation.findRelationshipFieldByName(propertyName);
					if (field == null && ignoreMissing) {
						continue;
					}
					if (field == null) {
						throw new ResourceException(String.format("Invalid relationship name: %s for %s", property.getKey(),
								resourceInformation.getResourceType()));
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
						setRelationField(newResource, registryEntry, propertyName, relationship, queryAdapter,
								parameterProvider);
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
			List relationships = new LinkedList<>();

			List<ResourceIdentifier> resourceIds = relationship.getCollectionData().get();
			for (ResourceModificationFilter filter : modificationFilters) {
				resourceIds = filter.modifyManyRelationship(newResource, relationshipField, ResourceRelationshipModificationType.SET, resourceIds);
			}

			for (ResourceIdentifier resourceId : resourceIds) {
				RegistryEntry entry = resourceRegistry.getEntry(resourceId.getType());
				Class idFieldType = entry.getResourceInformation()
						.getIdField()
						.getType();
				Serializable castedRelationshipId = (Serializable) typeParser.parse(resourceId.getId(), idFieldType);
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

			for (ResourceModificationFilter filter : modificationFilters) {
				relationshipId = filter.modifyOneRelationship(newResource, relationshipFieldByName, relationshipId);
			}

			Object relationObject;
			if (relationshipId != null) {
				RegistryEntry entry = resourceRegistry.getEntry(relationshipId.getType());
				Class idFieldType = entry.getResourceInformation()
						.getIdField()
						.getType();
				Serializable castedRelationshipId = (Serializable) typeParser.parse(relationshipId.getId(), idFieldType);

				relationObject = fetchRelatedObject(entry, castedRelationshipId, parameterProvider, queryAdapter);
			} else {
				relationObject = null;
			}

			relationshipFieldByName.getAccessor().setValue(newResource, relationObject);
		}
	}

	protected Object fetchRelatedObject(RegistryEntry entry, Serializable relationId,
										RepositoryMethodParameterProvider parameterProvider,
										QueryAdapter queryAdapter) {
		return entry.getResourceRepository(parameterProvider).findOne(relationId, queryAdapter).getEntity();
	}

}
