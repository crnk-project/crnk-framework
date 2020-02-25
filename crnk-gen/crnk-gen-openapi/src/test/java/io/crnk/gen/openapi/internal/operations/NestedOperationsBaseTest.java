package io.crnk.gen.openapi.internal.operations;

import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaPrimaryKey;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collections;

class NestedOperationsBaseTest extends OperationsBaseTest {

  MetaResource metaResource;

  MetaResource relatedMetaResource;

  MetaResourceField metaResourceField;

  MetaAttribute metaAttribute;

  MetaPrimaryKey metaPrimaryKey;

  @BeforeEach
  void setUp() {
    metaResource = new MetaResource();
    metaResource.setName("ResourceName");
    metaResource.setResourceType("ResourceType");
    metaResource.setResourcePath("ResourcePath");

    relatedMetaResource = new MetaResource();
    relatedMetaResource.setName("RelatedResourceName");
    relatedMetaResource.setResourceType("RelatedResourceType");
    relatedMetaResource.setResourcePath("RelatedResourcePath");

    metaResourceField = new MetaResourceField();
    metaResourceField.setName("someRelatedResource");
    metaResourceField.setType(new MetaType());

    // Set up Primary Key
    metaPrimaryKey = new MetaPrimaryKey();
    metaAttribute = new MetaAttribute();
    metaAttribute.setName("id");
    metaPrimaryKey.setElements(Collections.singletonList(metaAttribute));
    
    metaResource.setPrimaryKey(metaPrimaryKey);
  }
}
