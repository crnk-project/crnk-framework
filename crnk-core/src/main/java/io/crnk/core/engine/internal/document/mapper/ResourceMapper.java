package io.crnk.core.engine.internal.document.mapper;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.SerializerUtil;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.links.SelfLinksInformation;
import io.crnk.core.resource.meta.MetaInformation;

public class ResourceMapper {

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

	public Resource toData(Object entity, QueryAdapter queryAdapter) {
		ResourceMappingConfig mappingConfig = new ResourceMappingConfig();
		return toData(entity, queryAdapter, mappingConfig);
	}

	public Resource toData(Object entity, QueryAdapter queryAdapter, ResourceMappingConfig mappingConfig) {
		if (entity instanceof Resource) {
			// Resource and ResourceId
			return (Resource) entity;
		}
		else {
			// map resource objects
			Class<?> dataClass = entity.getClass();

			ResourceInformation resourceInformation = util.getResourceInformation(dataClass);

			Resource resource = new Resource();
			resource.setId(util.getIdString(entity, resourceInformation));
			resource.setType(resourceInformation.getResourceType());
			if (!client) {
				if (mappingConfig.getSerializeLinks()) {
					util.setLinks(resource, getResourceLinks(entity, resourceInformation), queryAdapter);
				}
				util.setMeta(resource, getResourceMeta(entity, resourceInformation));
			}
			setAttributes(resource, entity, resourceInformation, queryAdapter);
			setRelationships(resource, entity, resourceInformation, queryAdapter, mappingConfig);
			return resource;
		}
	}

	private MetaInformation getResourceMeta(Object entity, ResourceInformation resourceInformation) {
		if (resourceInformation.getMetaField() != null) {
			return (MetaInformation) resourceInformation.getMetaField().getAccessor().getValue(entity);
		}
		return null;
	}

	public LinksInformation getResourceLinks(Object entity, ResourceInformation resourceInformation) {
		LinksInformation info;
		if (resourceInformation.getLinksField() != null) {
			info = (LinksInformation) resourceInformation.getLinksField().getAccessor().getValue(entity);
		}
		else {
			info = new DocumentMapperUtil.DefaultSelfRelatedLinksInformation();
		}
		if (info instanceof SelfLinksInformation) {
			SelfLinksInformation self = (SelfLinksInformation) info;
			if (self.getSelf() == null) {
				self.setSelf(util.getSelfUrl(resourceInformation, entity));
			}
		}
		return info;
	}

	protected void setAttributes(Resource resource, Object entity, ResourceInformation resourceInformation,
			QueryAdapter queryAdapter) {
		// fields legacy may further limit the number of fields
		List<ResourceField> fields = DocumentMapperUtil
				.getRequestedFields(resourceInformation, queryAdapter, resourceInformation.getAttributeFields(), false);

		// serialize the individual attributes
		for (ResourceField field : fields) {
			if (!isIgnored(field) && field.getAccess().isReadable()) {
				setAttribute(resource, field, entity);
			}
		}
	}

	protected boolean isIgnored(ResourceField field) { // NOSONAR signature is ok since protected
		return resourceFilterDirectory != null && resourceFilterDirectory.get(field, HttpMethod.GET) != FilterBehavior.NONE;
	}

	protected void setAttribute(Resource resource, ResourceField field, Object entity) {
		Object value = field.getAccessor().getValue(entity);
		JsonNode valueNode = objectMapper.valueToTree(value);
		resource.getAttributes().put(field.getJsonName(), valueNode);
	}

	protected void setRelationships(Resource resource, Object entity, ResourceInformation resourceInformation,
			QueryAdapter queryAdapter, ResourceMappingConfig mappingConfig) {
		List<ResourceField> fields = DocumentMapperUtil
				.getRequestedFields(resourceInformation, queryAdapter, resourceInformation.getRelationshipFields(), true);
		for (ResourceField field : fields) {
			if (!isIgnored(field)) {
				setRelationship(resource, field, entity, resourceInformation, queryAdapter, mappingConfig);
			}
		}
	}

	protected void setRelationship(Resource resource, ResourceField field, Object entity, ResourceInformation
			resourceInformation,
			QueryAdapter queryAdapter, ResourceMappingConfig mappingConfig) {
		{ // NOSONAR signature is ok since protected
			SerializerUtil serializerUtil = DocumentMapperUtil.getSerializerUtil();

			Relationship relationship = new Relationship();
			boolean addLinks = mappingConfig.getSerializeLinks() && (queryAdapter == null || !queryAdapter.getCompactMode());
			if (addLinks) {
				ObjectNode relationshipLinks = objectMapper.createObjectNode();
				String selfUrl = util.getRelationshipLink(resourceInformation, entity, field, false);
				serializerUtil.serializeLink(objectMapper, relationshipLinks, SELF_FIELD_NAME, selfUrl);
				String relatedUrl = util.getRelationshipLink(resourceInformation, entity, field, true);
				serializerUtil.serializeLink(objectMapper, relationshipLinks, RELATED_FIELD_NAME, relatedUrl);
				relationship.setLinks(relationshipLinks);
			}
			resource.getRelationships().put(field.getJsonName(), relationship);
		}
	}
}
