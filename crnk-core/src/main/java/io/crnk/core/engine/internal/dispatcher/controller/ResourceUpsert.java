package io.crnk.core.engine.internal.dispatcher.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInstanceBuilder;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.information.resource.ResourceAttributesBridge;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.properties.ResourceFieldImmutableWriteBehavior;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.*;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

public abstract class ResourceUpsert extends ResourceIncludeField {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	protected final ObjectMapper objectMapper;

	protected final ResourceFilterDirectory resourceFilterDirectory;

	private PropertiesProvider propertiesProvider;

	public ResourceUpsert(ResourceRegistry resourceRegistry, PropertiesProvider propertiesProvider, TypeParser typeParser,
						  ObjectMapper objectMapper, DocumentMapper documentMapper) {
		super(resourceRegistry, typeParser, documentMapper);
		this.propertiesProvider = propertiesProvider;
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

			ResourceAttributesBridge resourceAttributesBridge = resourceInformation.getAttributeFields();

			for (Map.Entry<String, JsonNode> entry : dataBody.getAttributes().entrySet()) {
				String attributeName = entry.getKey();

				ResourceField field = resourceInformation.findAttributeFieldByName(attributeName);
				if (canModifyField(resourceInformation, attributeName, field)) {
					resourceAttributesBridge.setProperty(objectMapper, instance, entry.getValue(), entry.getKey());
				}
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
					if(field == null && ignoreMissing){
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

	protected Object fetchRelatedObject(RegistryEntry entry, Serializable relationId,
										RepositoryMethodParameterProvider parameterProvider,
										QueryAdapter queryAdapter) {
		return entry.getResourceRepository(parameterProvider).findOne(relationId, queryAdapter).getEntity();
	}

}
