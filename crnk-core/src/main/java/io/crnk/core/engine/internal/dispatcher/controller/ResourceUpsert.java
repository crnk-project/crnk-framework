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
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.filter.ResourceModificationFilter;
import io.crnk.core.engine.filter.ResourceRelationshipModificationType;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.AnyResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInstanceBuilder;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.properties.ResourceFieldImmutableWriteBehavior;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.result.ResultFactory;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.exception.RequestBodyException;
import io.crnk.core.exception.ResourceException;
import io.crnk.core.repository.response.JsonApiResponse;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

public abstract class ResourceUpsert extends ResourceIncludeField {

    protected Resource getRequestBody(Document requestDocument, JsonPath path, HttpMethod method) {
        String resourceType = path.getRootEntry().getResourceInformation().getResourcePath();

        assertRequestDocument(requestDocument, method, resourceType);

        if (!requestDocument.getData().isPresent() || requestDocument.getData().get() == null) {
            throw new RequestBodyException(method, resourceType, "No data field in the body.");
        }
        if (requestDocument.getData().get() instanceof Collection) {
            throw new RequestBodyException(method, resourceType, "Multiple data in body");
        }

        Resource resourceBody = (Resource) requestDocument.getData().get();
        RegistryEntry bodyRegistryEntry = context.getResourceRegistry().getEntry(resourceBody.getType());
        if (bodyRegistryEntry == null) {
            throw new RepositoryNotFoundException(resourceBody.getType());
        }
        return resourceBody;
    }

    protected Object newEntity(ResourceInformation resourceInformation, Resource dataBody) {
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

    public void setLinks(Resource dataBody, Object instance, ResourceInformation resourceInformation) {
        ResourceField linksField = resourceInformation.getLinksField();
        if (dataBody.getLinks() != null && linksField != null) {
            JsonNode linksNode = dataBody.getLinks();
            Class<?> linksClass = linksField.getType();
            ObjectReader linksMapper = context.getObjectMapper().readerFor(linksClass);
            try {
                Object links = linksMapper.readValue(linksNode);
                linksField.getAccessor().setValue(instance, links);
            } catch (IOException e) {
                throw newBodyException("failed to parse links information", e);
            }
        }
    }

    public void setMeta(Resource dataBody, Object instance, ResourceInformation resourceInformation) {
        ResourceField metaField = resourceInformation.getMetaField();
        if (dataBody.getMeta() != null && metaField != null) {
            JsonNode metaNode = dataBody.getMeta();

            Class<?> metaClass = metaField.getType();

            ObjectReader metaMapper = context.getObjectMapper().readerFor(metaClass);
            try {
                Object meta = metaMapper.readValue(metaNode);
                metaField.getAccessor().setValue(instance, meta);
            } catch (IOException e) {
                throw newBodyException("failed to parse links information", e);
            }
        }
    }

    protected RuntimeException newBodyException(String message, IOException e) {
        throw new RequestBodyException(message, e);
    }

    protected void setAttributes(Resource dataBody, Object instance, ResourceInformation resourceInformation, QueryContext queryContext) {
        if (dataBody.getAttributes() != null) {
            for (Map.Entry<String, JsonNode> entry : dataBody.getAttributes().entrySet()) {
                String attributeName = entry.getKey();

                setAttribute(resourceInformation, instance, attributeName, entry.getValue(), queryContext);
            }
        }
    }

    private void setAttribute(ResourceInformation resourceInformation, Object instance, String attributeName,
                              JsonNode valueNode, QueryContext queryContext) {
        ResourceField field = resourceInformation.findAttributeFieldByName(attributeName);

        ResourceFilterDirectory filterDirectory = context.getResourceFilterDirectory();
        if (!checkAccess() || filterDirectory.canAccess(field, getHttpMethod(), queryContext, ignoreImmutableFields())) {
            logger.debug("set attribute {}={}", attributeName, valueNode);
            ObjectMapper objectMapper = context.getObjectMapper();
            List<ResourceModificationFilter> modificationFilters = context.getModificationFilters();
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
                        String.format("Exception while setting %s.%s=%s due to %s", instance, attributeName, valueNode,
                                e.getMessage()), e);
            }
        }
    }

    protected boolean checkAccess() {
        return true;
    }

    protected abstract HttpMethod getHttpMethod();


    Object buildNewResource(RegistryEntry registryEntry, Resource dataBody, String resourceName) {
        PreconditionUtil.verify(dataBody != null, "No data field in the body.");
        PreconditionUtil.verify(resourceName.equals(dataBody.getType()),
                "Inconsistent type definition between path and body: body type: " +
                        "%s, request type: %s",
                dataBody.getType(),
                resourceName);

        ResourceInstanceBuilder<Object> instanceBuilder = registryEntry.getResourceInformation().getInstanceBuilder();
        Object entity = instanceBuilder.buildResource(dataBody);
        logger.debug("created instance %s", entity);
        return entity;
    }

    protected Result<List> setRelationsAsync(Object newResource, RegistryEntry registryEntry, Resource resource, QueryAdapter
            queryAdapter, boolean ignoreMissing) {

        List<Result> results = new ArrayList<>();
        if (resource.getRelationships() != null) {
            for (Map.Entry<String, Relationship> entry : resource.getRelationships().entrySet()) {
                String relationshipName = entry.getKey();
                Relationship relationship = entry.getValue();
                if (relationship != null) {

                    ResourceInformation resourceInformation = registryEntry.getResourceInformation();
                    ResourceField field = resourceInformation.findRelationshipFieldByName(relationshipName);
                    if (field == null && ignoreMissing) {
                        continue;
                    }
                    if (field == null) {
                        throw new ResourceException(String.format("Invalid relationship name: %s for %s", entry.getKey(),
                                resourceInformation.getResourceType()));
                    }

                    ResourceFilterDirectory filterDirectory = context.getResourceFilterDirectory();
                    if (!checkAccess() || filterDirectory.canAccess(field, getHttpMethod(), queryAdapter.getQueryContext(), ignoreImmutableFields())) {
                        Optional<Result> result;
                        if (field.isCollection()) {
                            //noinspection unchecked
                            result = setRelationsFieldAsync(newResource,
                                    registryEntry,
                                    entry,
                                    queryAdapter);
                        } else {
                            //noinspection unchecked
                            result = setRelationFieldAsync(newResource, registryEntry, relationshipName, relationship, queryAdapter);
                        }
                        if (result.isPresent()) {
                            results.add(result.get());
                        }
                    }
                }
            }
        }

        ResultFactory resultFactory = context.getResultFactory();
        return resultFactory.zip((List) results);
    }

    protected Optional<Result> setRelationsFieldAsync(Object newResource, RegistryEntry registryEntry,
                                                      Map.Entry<String, Relationship> property, QueryAdapter queryAdapter) {
        Relationship relationship = property.getValue();
        if (relationship.getData().isPresent()) {
            String propertyName = property.getKey();
            ResourceField relationshipField = registryEntry.getResourceInformation()
                    .findRelationshipFieldByName(propertyName);


            List<ResourceIdentifier> relationshipIds = relationship.getCollectionData().get();
            List<ResourceModificationFilter> modificationFilters = context.getModificationFilters();
            for (ResourceModificationFilter filter : modificationFilters) {
                relationshipIds =
                        filter.modifyManyRelationship(newResource, relationshipField, ResourceRelationshipModificationType.SET,
                                relationshipIds);
            }

            ResourceRegistry resourceRegistry = context.getResourceRegistry();
            List relationshipTypedIds = new LinkedList<>();
            for (ResourceIdentifier resourceId : relationshipIds) {
                RegistryEntry entry = getRegistryEntry(resourceId.getType());
                Class idFieldType = entry.getResourceInformation()
                        .getIdField()
                        .getType();
                Serializable typedRelationshipId = parseId(resourceId, idFieldType);

                relationshipTypedIds.add(typedRelationshipId);
            }

            if (relationshipField.hasIdField()) {
                logger.debug("set relationshipIds {}={}", propertyName, relationshipTypedIds);
                relationshipField.getIdAccessor().setValue(newResource, relationshipTypedIds);
            }

            // FIXME batch fetchRelatedObject
            if (decideSetRelationObjectsField(relationshipField)) {
                List<Result<Object>> relatedResults = new ArrayList<>();
                for (int i = 0; i < relationshipIds.size(); i++) {
                    ResourceIdentifier resourceId = relationshipIds.get(i);
                    Serializable typedRelationshipId = (Serializable) relationshipTypedIds.get(i);
                    RegistryEntry entry = resourceRegistry.getEntry(resourceId.getType());
                    relatedResults.add(fetchRelated(entry, typedRelationshipId, queryAdapter));
                }

                if (relatedResults.isEmpty()) {
                    List relatedList = new LinkedList<>();
                    logger.debug("set relationships {}={}", propertyName, relatedList);
                    relationshipField.getAccessor().setValue(newResource, relatedList);
                } else {
                    return Optional.of(context.getResultFactory().zip(relatedResults).doWork(relatedObjects -> {
                        List relatedList = new LinkedList<>();
                        relatedList.addAll(relatedObjects);

                        logger.debug("set relationships {}={}", propertyName, relatedList);
                        relationshipField.getAccessor().setValue(newResource, relatedList);
                    }));
                }
            }else{
                logger.debug("decideSetRelationObjectsField skipped {}", propertyName, relationshipTypedIds);
            }
        }
        return Optional.empty();
    }

    protected boolean decideSetRelationObjectsField(ResourceField relationshipField) {
        // TODO consider making this configurable with @JsonApiRelationId annotation
        return !relationshipField.hasIdField();
    }

    protected Optional<Result> setRelationFieldAsync(Object newResource, RegistryEntry registryEntry,
                                                     String relationshipName, Relationship relationship, QueryAdapter queryAdapter) {

        if (relationship.getData().isPresent()) {
            ResourceIdentifier relationshipId = (ResourceIdentifier) relationship.getData().get();

            ResourceInformation resourceInformation = registryEntry.getResourceInformation();
            ResourceField field = resourceInformation
                    .findRelationshipFieldByName(relationshipName);

            if (field == null) {
                throw new ResourceException(String.format("Invalid relationship name: %s", relationshipName));
            }
            List<ResourceModificationFilter> modificationFilters = context.getModificationFilters();
            for (ResourceModificationFilter filter : modificationFilters) {
                relationshipId = filter.modifyOneRelationship(newResource, field, relationshipId);
            }

            if (relationshipId == null) {
                logger.debug("set relationship {}=null", relationshipName);
                field.getAccessor().setValue(newResource, null);
            } else {
                RegistryEntry entry = getRegistryEntry(relationshipId.getType());
                Class idFieldType = entry.getResourceInformation()
                        .getIdField()
                        .getType();
                Serializable typedRelationshipId = parseId(relationshipId, idFieldType);

                if (field.hasIdField() && (!resourceInformation.isNested() || resourceInformation.getParentField() != field)) {
                    logger.debug("set relationshipId {}={}", relationshipName, typedRelationshipId);
                    field.getIdAccessor().setValue(newResource, typedRelationshipId);
                }
                if (decideSetRelationObjectField(entry, typedRelationshipId, field)) {
                    Result<Object> result = fetchRelated(entry, typedRelationshipId, queryAdapter)
                            .doWork(relatedObject -> {
                                logger.debug("set relationship {}={}", relationshipName, relatedObject);
                                field.getAccessor().setValue(newResource, relatedObject);
                            });
                    return Optional.of(result);
                } else {
                    logger.debug("decideSetRelationObjectField skipped {}", relationshipName);
                }
            }
        }
        return Optional.empty();
    }


    private boolean ignoreImmutableFields() {
        PropertiesProvider propertiesProvider = context.getPropertiesProvider();
        String strBehavior = propertiesProvider.getProperty(CrnkProperties.RESOURCE_FIELD_IMMUTABLE_WRITE_BEHAVIOR);
        ResourceFieldImmutableWriteBehavior behavior =
                strBehavior != null ? ResourceFieldImmutableWriteBehavior.valueOf(strBehavior)
                        : ResourceFieldImmutableWriteBehavior.IGNORE;
        return behavior == ResourceFieldImmutableWriteBehavior.IGNORE;
    }

    protected Serializable parseId(ResourceIdentifier relationshipId, Class idFieldType) {
        TypeParser typeParser = context.getTypeParser();
        return (Serializable) typeParser.parse(relationshipId.getId(), idFieldType);
    }

    /**
     * for performance reasons this method does not the relation if there is a relationId field. Avoids having to retrieve the
     * full relation.
     */
    protected boolean decideSetRelationObjectField(RegistryEntry entry, Serializable relationId, ResourceField field) {
        return !field.hasIdField();
    }

    protected Result<Object> fetchRelated(RegistryEntry entry, Serializable relationId,
                                          QueryAdapter queryAdapter) {
        return entry.getResourceRepository().findOne(relationId, queryAdapter)
                .map(JsonApiResponse::getEntity);
    }

}
