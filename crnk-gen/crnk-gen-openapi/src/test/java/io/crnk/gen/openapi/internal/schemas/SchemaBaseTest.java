package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.MetaPrimaryKey;
import io.crnk.meta.model.MetaPrimitiveType;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collections;

public class SchemaBaseTest {

  MetaResource metaResource;

  MetaResourceField metaResourceField;

  MetaResourceField additionalMetaResourceField;

  MetaPrimaryKey metaPrimaryKey;

  @BeforeEach
  void setUp() {
    metaResource = new MetaResource();
    metaResource.setName("ResourceName");
    metaResource.setResourceType("ResourceType");
    metaResource.setResourcePath("ResourcePath");

    // Set up Primary Key
    metaPrimaryKey = new MetaPrimaryKey();
    metaResourceField = new MetaResourceField();
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
  }
}
