package io.crnk.core.engine.internal.document.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryAdapter;

public class LinkObjectResourceMapper extends ResourceMapper {

	private static final String HREF_FIELD_NAME = "href";

	public LinkObjectResourceMapper(DocumentMapperUtil util, boolean client, ObjectMapper objectMapper, ResourceFilterDirectory
			resourceFilterDirectory) {

		super(util, client, objectMapper, resourceFilterDirectory);
	}

	@Override
	protected void setRelationship(Resource resource, ResourceField field, Object entity, ResourceInformation resourceInformation, QueryAdapter queryAdapter) {
		{ // NOSONAR signature is ok since protected

			ObjectNode relationshipLinks = objectMapper.createObjectNode();

			String selfLink = util.getRelationshipLink(resourceInformation, entity, field, false);
			ObjectNode selfLinkNode = objectMapper.createObjectNode();
			selfLinkNode.put(HREF_FIELD_NAME, selfLink);
			relationshipLinks.set(SELF_FIELD_NAME, selfLinkNode);

			String relatedLink = util.getRelationshipLink(resourceInformation, entity, field, true);
			ObjectNode relatedLinkNode = objectMapper.createObjectNode();
			relatedLinkNode.put(HREF_FIELD_NAME, relatedLink);
			relationshipLinks.set(RELATED_FIELD_NAME, relatedLinkNode);

			Relationship relationship = new Relationship();
			relationship.setLinks(relationshipLinks);
			resource.getRelationships().put(field.getJsonName(), relationship);
		}
	}

}
