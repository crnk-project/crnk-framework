package io.crnk.core.engine.internal.document.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.AnyResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.SerializerUtil;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.resource.annotations.JsonIncludeStrategy;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.links.SelfLinksInformation;
import io.crnk.core.resource.meta.MetaInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResourceMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceMapper.class);

    private static final String SELF_FIELD_NAME = "self";

    private final String RELATED_FIELD_NAME = "related";

    private final ResourceFilterDirectory resourceFilterDirectory;

    private DocumentMapperUtil util;

    private boolean client;

    private ObjectMapper objectMapper;

    public ResourceMapper(DocumentMapperUtil util, boolean client, ObjectMapper objectMapper,
                          ResourceFilterDirectory resourceFilterDirectory) {
        this.util = util;
        this.client = client;
        this.objectMapper = objectMapper;
        this.resourceFilterDirectory = resourceFilterDirectory;
    }

    public Resource toData(Object entity, QueryAdapter queryAdapter, ResourceMappingConfig mappingConfig) {
        if (entity instanceof Resource) {
            // Resource and ResourceId
            Resource resource = (Resource) entity;
            LOGGER.debug("directly returning id={}, type={}", resource.getId(), resource.getType());
            return resource;
        } else {
            // map resource objects
            QueryContext queryContext = queryAdapter.getQueryContext();
            ResourceInformation resourceInformation = util.getResourceInformation(entity);
            Resource resource = new Resource();
            setId(resource, entity, resourceInformation);
            resource.setType(resourceInformation.getResourceType());

            LOGGER.debug("mapping id={}, type={}", resource.getId(), resource.getType());
            if (mappingConfig.getSerializeLinks()) {
                util.setLinks(resource, getResourceLinks(entity, resourceInformation, queryContext), queryAdapter);
            }
            util.setMeta(resource, getResourceMeta(entity, resourceInformation));
            setAttributes(resource, entity, resourceInformation, queryAdapter, mappingConfig);
            setRelationships(resource, entity, resourceInformation, queryAdapter, mappingConfig);
            return resource;
        }
    }

    private void setId(Resource resource, Object entity, ResourceInformation resourceInformation) {
        ResourceField idField = resourceInformation.getIdField();
        Object value = idField.getAccessor().getValue(entity);
        if (isIncluded(idField, value)) {
            resource.setId(resourceInformation.toIdString(value));
        }
    }

    private MetaInformation getResourceMeta(Object entity, ResourceInformation resourceInformation) {
        if (resourceInformation.getMetaField() != null) {
            return (MetaInformation) resourceInformation.getMetaField().getAccessor().getValue(entity);
        }
        return null;
    }

    public LinksInformation getResourceLinks(Object entity, ResourceInformation resourceInformation, QueryContext queryContext) {
        LinksInformation info;
        if (resourceInformation.getLinksField() != null) {
            info = (LinksInformation) resourceInformation.getLinksField().getAccessor().getValue(entity);
        } else {
            info = new DocumentMapperUtil.DefaultSelfRelatedLinksInformation();
        }
        if (info instanceof SelfLinksInformation) {
            SelfLinksInformation self = (SelfLinksInformation) info;
            if (self.getSelf() == null && !client) {
                self.setSelf(util.getSelfUrl(queryContext, resourceInformation, entity));
            }
        }
        return info;
    }

    @SuppressWarnings("unchecked")
    protected void setAttributes(Resource resource, Object entity, ResourceInformation resourceInformation,
                                 QueryAdapter queryAdapter, ResourceMappingConfig mappingConfig) {
        // fields legacy may further limit the number of fields
        List<ResourceField> fields = DocumentMapperUtil
                .getRequestedFields(resourceInformation, queryAdapter, resourceInformation.getAttributeFields(), false);
        // serialize the individual attributes
        QueryContext queryContext = queryAdapter.getQueryContext();
        for (ResourceField field : fields) {
            if (!isIgnored(field, queryContext) && field.getAccess().isReadable()) {
                Object value = field.getAccessor().getValue(entity);
                if (!mappingConfig.isIgnoreDefaults() || !isDefaultValue(value)) {
                    LOGGER.debug("setting {}={}", field.getUnderlyingName(), value);
                    setAttribute(resource, field, value);
                }else{
                    LOGGER.debug("ignored default value for {}", field.getUnderlyingName());
                }
            }else {
                LOGGER.debug("ignored {} due to filter", field.getUnderlyingName());
            }
        }

        if (resourceInformation.getAnyFieldAccessor() != null) {
            AnyResourceFieldAccessor anyFieldAccessor = resourceInformation.getAnyFieldAccessor();
            Map<String, Object> map = anyFieldAccessor.getValues(entity);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                setAnyAttribute(resource, entry.getKey(), entry.getValue());
            }
        }

    }

    private static Set<Object> defaultValues = new HashSet<>(Arrays.asList(
            Byte.valueOf((byte) 0),
            Short.valueOf((short) 0),
            Integer.valueOf(0),
            Long.valueOf(0l),
            Float.valueOf(0f),
            Double.valueOf(0d)
    ));

    protected boolean isDefaultValue(Object value) {
        return value == null || defaultValues.contains(value);
    }

    protected boolean isIgnored(ResourceField field, QueryContext queryContext) { // NOSONAR signature is ok since protected
        return resourceFilterDirectory != null
                && resourceFilterDirectory.get(field, HttpMethod.GET, queryContext) != FilterBehavior.NONE;
    }

    protected void setAttribute(Resource resource, ResourceField field, Object value) {
        if (isIncluded(field, value)) {
            JsonNode valueNode = objectMapper.valueToTree(value);
            resource.getAttributes().put(field.getJsonName(), valueNode);
        }
    }

    protected void setAnyAttribute(Resource resource, String field, Object value) {
        JsonNode valueNode = objectMapper.valueToTree(value);
        resource.getAttributes().put(field, valueNode);
    }

    private boolean isIncluded(ResourceField field, Object value) {
        JsonIncludeStrategy includeStrategy = field.getJsonIncludeStrategy();
        return JsonIncludeStrategy.DEFAULT.equals(includeStrategy) || value != null && JsonIncludeStrategy.NOT_NULL.equals(includeStrategy)
                || JsonIncludeStrategy.NON_EMPTY.equals(includeStrategy) && !isDefaultValue(value);
    }

    protected void setRelationships(Resource resource, Object entity, ResourceInformation resourceInformation,
                                    QueryAdapter queryAdapter, ResourceMappingConfig mappingConfig) {
        List<ResourceField> fields = DocumentMapperUtil
                .getRequestedFields(resourceInformation, queryAdapter, resourceInformation.getRelationshipFields(), true);
        QueryContext queryContext = queryAdapter.getQueryContext();
        for (ResourceField field : fields) {
            if (!isIgnored(field, queryContext)) {
                setRelationship(resource, field, entity, resourceInformation, queryAdapter, mappingConfig);
            }
        }
    }

    protected void setRelationship(Resource resource, ResourceField field, Object entity, ResourceInformation
            resourceInformation, QueryAdapter queryAdapter, ResourceMappingConfig mappingConfig) {
        { // NOSONAR signature is ok since protected
            SerializerUtil serializerUtil = DocumentMapperUtil.getSerializerUtil();

            Relationship relationship = new Relationship();
            boolean addRelationship = mappingConfig.getSerializeLinks() && (queryAdapter == null || !queryAdapter.getCompactMode());
            if (addRelationship) {
                ObjectNode relationshipLinks = objectMapper.createObjectNode();
                if (mappingConfig.getSerializeSelfRelationshipLinks()) {
                    String selfUrl = util.getRelationshipLink(resource, field, false);
                    if (selfUrl != null) {
                        serializerUtil.serializeLink(objectMapper, relationshipLinks, SELF_FIELD_NAME, selfUrl);
                    }
                }
                String relatedUrl = util.getRelationshipLink(resource, field, true);
                if (relatedUrl != null) {
                    serializerUtil.serializeLink(objectMapper, relationshipLinks, RELATED_FIELD_NAME, relatedUrl);
                    relationship.setLinks(relationshipLinks);
                }
            }
            resource.getRelationships().put(field.getJsonName(), relationship);
        }
    }
}
