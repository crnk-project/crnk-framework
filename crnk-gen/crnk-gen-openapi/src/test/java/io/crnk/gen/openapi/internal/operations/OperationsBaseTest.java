package io.crnk.gen.openapi.internal.operations;

import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaPrimaryKey;
import io.crnk.meta.model.resource.MetaResource;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collections;

class OperationsBaseTest {

  MetaResource metaResource;

  MetaAttribute metaAttribute;

  MetaPrimaryKey metaPrimaryKey;

  @BeforeEach
  void setUp() {
    metaResource = new MetaResource();
    metaResource.setName("ResourceName");
    metaResource.setResourceType("ResourceType");
    metaResource.setResourcePath("ResourcePath");

    // Set up Primary Key
    metaPrimaryKey = new MetaPrimaryKey();
    metaAttribute = new MetaAttribute();
    metaAttribute.setName("id");
    metaPrimaryKey.setElements(Collections.singletonList(metaAttribute));
    
    metaResource.setPrimaryKey(metaPrimaryKey);
  }
}
