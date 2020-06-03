package io.crnk.gen.openapi.internal;

import io.crnk.meta.model.MetaPrimaryKey;
import io.crnk.meta.model.MetaPrimitiveType;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collections;

public class MetaResourceBaseTest {

	public MetaResource metaResource;

	public MetaResourceField metaResourceField;
	public MetaResourceField additionalMetaResourceField;
	public MetaResourceField relationshipMetaResourceField;

	@BeforeEach
	void setUp() {
		metaResource = getTestMetaResource();
		metaResourceField = (MetaResourceField) metaResource.getChildren().get(0);
	}

	protected MetaResource getTestMetaResource() {
		MetaResource metaResource = new MetaResource();
		metaResource.setName("ResourceName");
		metaResource.setResourceType("ResourceType");
		metaResource.setResourcePath("ResourcePath");

		// Set up Primary Key
		MetaPrimaryKey metaPrimaryKey = new MetaPrimaryKey();
		MetaResourceField metaResourceField = new MetaResourceField();
		metaResourceField.setName("id");
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setName("string");
		metaResourceField.setType(type);
		metaResourceField.setPrimaryKeyAttribute(true);
		metaPrimaryKey.setElements(Collections.singletonList(metaResourceField));

		metaResource.setPrimaryKey(metaPrimaryKey);
		metaResource.addChild(metaResourceField);

		additionalMetaResourceField = new MetaResourceField();
		additionalMetaResourceField.setName("name");
		additionalMetaResourceField.setType(type);
		metaResource.addChild(additionalMetaResourceField);

		final MetaResource relatedResource = new MetaResource();
		relatedResource.setName("RelatedResourceName");
		relatedResource.setResourceType("RelatedResourceType");
		relatedResource.setResourcePath("RelatedResourcePath");
		relatedResource.setElementType(relatedResource);
		relationshipMetaResourceField = new MetaResourceField();
		relationshipMetaResourceField.setName("resourceRelation");
		relationshipMetaResourceField.setType(relatedResource);
		relationshipMetaResourceField.setAssociation(true);
		metaResource.addChild(relationshipMetaResourceField);

		return metaResource;
	}
}
