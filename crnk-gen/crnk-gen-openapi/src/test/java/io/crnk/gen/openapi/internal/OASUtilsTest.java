package io.crnk.gen.openapi.internal;

import io.crnk.meta.model.MetaArrayType;
import io.crnk.meta.model.MetaCollectionType;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaEnumType;
import io.crnk.meta.model.MetaLiteral;
import io.crnk.meta.model.MetaMapType;
import io.crnk.meta.model.MetaPrimitiveType;
import io.crnk.meta.model.MetaSetType;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ByteArraySchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OASUtilsTest {

  @Test
  public void testMetaResource() {
    MetaResource type = new MetaResource();
    type.setName("MyName");
    type.setResourceType("MyType");

    Schema schema = OASUtils.transformMetaResourceField(type);
    Assert.assertEquals("#/components/schemas/MyTypeResourceReference", schema.get$ref());
  }

  @Test
  public void testMetaCollectionType() {
    MetaPrimitiveType childType = new MetaPrimitiveType();
    childType.setName("string");

    MetaCollectionType type = new MetaSetType();
    type.setName("MyName");
    type.setElementType(childType);

    Schema schema = OASUtils.transformMetaResourceField(type);
    Assert.assertTrue(schema instanceof ArraySchema);
    Assert.assertEquals(true, schema.getUniqueItems());
  }

  @Test
  public void testMetaArrayType() {
    MetaPrimitiveType childType = new MetaPrimitiveType();
    childType.setName("string");

    MetaArrayType type = new MetaArrayType();
    type.setName("MyName");
    type.setElementType(childType);

    Schema schema = OASUtils.transformMetaResourceField(type);
    Assert.assertTrue(schema instanceof ArraySchema);
    Assert.assertEquals(false, schema.getUniqueItems());
  }

  @Test
  public void testString() {
    MetaPrimitiveType type = new MetaPrimitiveType();
    type.setName("string");
    Schema schema = OASUtils.transformMetaResourceField(type);
    Assert.assertTrue(schema instanceof StringSchema);
  }

  @Test
  public void testInteger() {
    MetaPrimitiveType type = new MetaPrimitiveType();
    type.setName("integer");
    Schema schema = OASUtils.transformMetaResourceField(type);
    Assert.assertTrue(schema instanceof IntegerSchema);
    Assert.assertEquals("int32", schema.getFormat());
  }

  @Test
  public void testShort() {
    MetaPrimitiveType type = new MetaPrimitiveType();
    type.setName("short");
    Schema schema = OASUtils.transformMetaResourceField(type);
    Assert.assertTrue(schema instanceof IntegerSchema);
    Assert.assertEquals("int32", schema.getFormat());
    Assert.assertEquals(new BigDecimal(-32768), schema.getMinimum());
    Assert.assertEquals(new BigDecimal(32767), schema.getMaximum());
  }

  @Test
  public void testLong() {
    MetaPrimitiveType type = new MetaPrimitiveType();
    type.setName("long");
    Schema schema = OASUtils.transformMetaResourceField(type);
    Assert.assertTrue(schema instanceof IntegerSchema);
    Assert.assertEquals("int64", schema.getFormat());
  }

  @Test
  public void testFloat() {
    MetaPrimitiveType type = new MetaPrimitiveType();
    type.setName("float");
    Schema schema = OASUtils.transformMetaResourceField(type);
    Assert.assertTrue(schema instanceof NumberSchema);
    Assert.assertEquals("float", schema.getFormat());
  }

  @Test
  public void testDouble() {
    MetaPrimitiveType type = new MetaPrimitiveType();
    type.setName("double");
    Schema schema = OASUtils.transformMetaResourceField(type);
    Assert.assertTrue(schema instanceof NumberSchema);
    Assert.assertEquals("double", schema.getFormat());
  }

  @Test
  public void testBoolean() {
    MetaPrimitiveType type = new MetaPrimitiveType();
    type.setName("boolean");
    Schema schema = OASUtils.transformMetaResourceField(type);
    Assert.assertTrue(schema instanceof BooleanSchema);
  }

  @Test
  public void testByte() {
    MetaPrimitiveType type = new MetaPrimitiveType();
    type.setName("byte");
    Schema schema = OASUtils.transformMetaResourceField(type);
    Assert.assertTrue(schema instanceof ByteArraySchema);
  }

  @Test
  public void testDate() {
    MetaPrimitiveType type = new MetaPrimitiveType();
    type.setName("date");
    Schema schema = OASUtils.transformMetaResourceField(type);
    Assert.assertTrue(schema instanceof DateSchema);
  }

  @Test
  public void testOffsetDateTime() {
    MetaPrimitiveType type = new MetaPrimitiveType();
    type.setName("offsetDateTime");
    Schema schema = OASUtils.transformMetaResourceField(type);
    Assert.assertTrue(schema instanceof DateTimeSchema);
  }

  @Test
  public void testLocalDate() {
    MetaPrimitiveType type = new MetaPrimitiveType();
    type.setName("localDate");
    Schema schema = OASUtils.transformMetaResourceField(type);
    Assert.assertTrue(schema instanceof DateSchema);
  }

  @Test
  public void testLocalDateTime() {
    MetaPrimitiveType type = new MetaPrimitiveType();
    type.setName("localDateTime");
    Schema schema = OASUtils.transformMetaResourceField(type);
    Assert.assertTrue(schema instanceof DateTimeSchema);
  }

  @Test
  public void testJson() {
    MetaPrimitiveType type = new MetaPrimitiveType();
    type.setName("json");
    Schema schema = OASUtils.transformMetaResourceField(type);
    Assert.assertTrue(schema instanceof ObjectSchema);
  }

  @Test
  public void testJsonObject() {
    MetaPrimitiveType type = new MetaPrimitiveType();
    type.setName("json.object");
    Schema schema = OASUtils.transformMetaResourceField(type);
    Assert.assertTrue(schema instanceof ObjectSchema);
  }

  @Test
  public void testJsonArray() {
    MetaPrimitiveType type = new MetaPrimitiveType();
    type.setName("json.array");
    Schema schema = OASUtils.transformMetaResourceField(type);
    Assert.assertTrue(schema instanceof ArraySchema);
  }

  @Test
  public void testEnumMetaLiteral() {
    MetaEnumType type = new MetaEnumType();
    type.setName("TestEnum");

    List<String> names = Arrays.asList("Remo", "Mac", "Ralph");
    List<MetaElement> people = new ArrayList<>();
    for (String name : names) {
      MetaLiteral person = new MetaLiteral();
      person.setName(name);
      people.add(person);
    }

    type.setChildren(people);
    Schema schema = OASUtils.transformMetaResourceField(type);
    Assert.assertTrue(schema instanceof StringSchema);
    for (int i=0; i < 3; i++) {
      Assert.assertEquals(names.get(i), schema.getEnum().get(i));
    }
  }

  @Test
  public void testEnumAnythingElse() {
    MetaEnumType type = new MetaEnumType();
    type.setName("TestEnum");

    List<String> names = Arrays.asList("Remo", "Mac", "Ralph");
    List<MetaElement> people = new ArrayList<>();
    for (String name : names) {
      MetaPrimitiveType person = new MetaPrimitiveType();
      person.setName(name);
      people.add(person);
    }

    type.setChildren(people);
    Schema schema = OASUtils.transformMetaResourceField(type);
    Assert.assertTrue(schema instanceof ObjectSchema);
    Assert.assertEquals(true, schema.getAdditionalProperties());
  }

  @Test
  public void testMergeOperations() {
    Operation existingOperation = new Operation();

    Operation newOperation = new Operation();
    newOperation.setOperationId("new id");
    newOperation.setSummary("new summary");
    newOperation.setDescription("new description");

    Map<String, Object> extensions = new HashMap<>();
    extensions.put("new schema", new Schema());
    newOperation.setExtensions(extensions);

    Assert.assertSame(newOperation, OASUtils.mergeOperations(newOperation, null));

    Operation afterMerge = OASUtils.mergeOperations(newOperation, existingOperation);
    Assert.assertEquals("new id", afterMerge.getOperationId());
    Assert.assertEquals("new summary", afterMerge.getSummary());
    Assert.assertEquals("new description", afterMerge.getDescription());
    Assert.assertSame(extensions, afterMerge.getExtensions());

    existingOperation.setOperationId("existing id");
    existingOperation.setSummary("existing summary");
    existingOperation.setDescription("existing description");

    Map<String, Object> existingExtensions = new HashMap<>();
    extensions.put("existing schema", new Schema());
    newOperation.setExtensions(extensions);

    afterMerge = OASUtils.mergeOperations(newOperation, existingOperation);
    Assert.assertEquals("existing id", afterMerge.getOperationId());
    Assert.assertEquals("existing summary", afterMerge.getSummary());
    Assert.assertEquals("existing description", afterMerge.getDescription());
    Assert.assertSame(extensions, afterMerge.getExtensions());
  }

  @Test
  public void testOneToMany() {
    MetaResourceField metaResourceField = new MetaResourceField();
    metaResourceField.setType(new MetaSetType());
    Assert.assertTrue(OASUtils.oneToMany(metaResourceField));

    metaResourceField.setType(new MetaMapType());
    Assert.assertTrue(OASUtils.oneToMany(metaResourceField));

    metaResourceField.setType(new MetaPrimitiveType());
    Assert.assertFalse(OASUtils.oneToMany(metaResourceField));
  }
}
