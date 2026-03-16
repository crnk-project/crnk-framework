package io.crnk.data.jpa.information;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.DefaultInformationBuilder;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.data.jpa.internal.JpaResourceInformationProvider;
import io.crnk.data.jpa.meta.JpaMetaProvider;
import io.crnk.data.jpa.model.AnnotationMappedSuperclassEntity;
import io.crnk.data.jpa.model.AnnotationTestEntity;
import io.crnk.data.jpa.model.JpaResourcePathTestEntity;
import io.crnk.data.jpa.model.JpaTransientTestEntity;
import io.crnk.data.jpa.model.JsonapiResourcePathTestEntity;
import io.crnk.data.jpa.model.ManyToManyOppositeEntity;
import io.crnk.data.jpa.model.ManyToManyTestEntity;
import io.crnk.data.jpa.model.OneToOneTestEntity;
import io.crnk.data.jpa.model.ReadOnlyAnnotatedEntity;
import io.crnk.data.jpa.model.RelatedEntity;
import io.crnk.data.jpa.model.RenamedTestEntity;
import io.crnk.data.jpa.model.TestEmbeddable;
import io.crnk.data.jpa.model.TestEntity;
import io.crnk.data.jpa.model.TestMappedSuperclass;
import io.crnk.data.jpa.model.TestSubclassWithSuperclassGenericsInterface;
import io.crnk.data.jpa.model.VersionedEntity;
import io.crnk.data.jpa.util.ResourceFieldComparator;
import io.crnk.legacy.registry.DefaultResourceInformationProviderContext;
import io.crnk.meta.MetaLookupImpl;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JpaResourceInformationProviderTest {

    private JpaResourceInformationProvider builder;

    private JpaMetaProvider jpaMetaProvider;

    @Before
    public void setup() {
        jpaMetaProvider = new JpaMetaProvider(Collections.emptySet());
        MetaLookupImpl lookup = new MetaLookupImpl();
        lookup.addProvider(jpaMetaProvider);
        builder = new JpaResourceInformationProvider(new NullPropertiesProvider());
        builder.init(new DefaultResourceInformationProviderContext(builder, new DefaultInformationBuilder(new TypeParser()),
                new TypeParser(), () -> new ObjectMapper()));
    }

    @Test
    public void checkNotAcceptMappedSuperClass() throws SecurityException, IllegalArgumentException {
        Assert.assertFalse(builder.accept(TestMappedSuperclass.class));
    }

    @Test
    public void checkAcceptEntity() throws SecurityException, IllegalArgumentException {
        Assert.assertTrue(builder.accept(TestEntity.class));
    }

    @Test
    public void checkResourceAccessAnnotations() {
        ResourceInformation information = builder.build(ReadOnlyAnnotatedEntity.class);
        Assert.assertTrue(information.getAccess().isReadable());
        Assert.assertFalse(information.getAccess().isPostable());
        Assert.assertFalse(information.getAccess().isDeletable());
        Assert.assertFalse(information.getAccess().isPatchable());
        for (ResourceField field : information.getFields()) {
            Assert.assertTrue(field.getAccess().isReadable());
            Assert.assertFalse(field.getAccess().isPostable());
            Assert.assertFalse(field.getAccess().isDeletable());
            Assert.assertFalse(field.getAccess().isPatchable());
        }
    }

    @Test
    public void test() throws SecurityException, IllegalArgumentException {

        ResourceInformation info = builder.build(TestEntity.class);
        ResourceField idField = info.getIdField();
        assertNotNull(idField);
        assertEquals("id", idField.getJsonName());
        assertEquals("id", idField.getUnderlyingName());
        assertEquals(Long.class, idField.getType());
        assertEquals(Long.class, idField.getGenericType());

        List<ResourceField> attrFields = new ArrayList<>(info.getAttributeFields());
        attrFields.sort(ResourceFieldComparator.INSTANCE);
        assertEquals(5, attrFields.size());
        ResourceField embField = attrFields.get(1);
        assertEquals(TestEntity.ATTR_embValue, embField.getJsonName());
        assertEquals(TestEntity.ATTR_embValue, embField.getUnderlyingName());
        assertEquals(TestEmbeddable.class, embField.getType());
        assertEquals(TestEmbeddable.class, embField.getGenericType());
        Assert.assertTrue(embField.getAccess().isPostable());
        Assert.assertTrue(embField.getAccess().isPatchable());
        Assert.assertTrue(embField.getAccess().isSortable());
        Assert.assertTrue(embField.getAccess().isFilterable());

        ArrayList<ResourceField> relFields = new ArrayList<>(info.getRelationshipFields());
        relFields.sort(ResourceFieldComparator.INSTANCE);
        assertEquals(6, relFields.size());
        boolean found = false;
        for (ResourceField relField : relFields) {
            if (relField.getUnderlyingName().equals(TestEntity.ATTR_oneRelatedValue)) {
                assertEquals(TestEntity.ATTR_oneRelatedValue, relField.getJsonName());
                assertEquals(RelatedEntity.class, relField.getType());
                assertEquals(RelatedEntity.class, relField.getGenericType());
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    @Test
    public void testPrimitiveTypesProperlyRecognized() {
        ResourceInformation info = builder.build(TestEntity.class);
        ResourceField field = info.findAttributeFieldByName("longValue");
        Assert.assertNotNull(field);
        Assert.assertEquals(long.class, field.getType());
        Assert.assertEquals(long.class, field.getGenericType());
    }

    @Test
    public void testIdAccess() {
        ResourceInformation info = builder.build(TestEntity.class);
        ResourceField idField = info.getIdField();
        Assert.assertTrue(idField.getAccess().isPostable());
        Assert.assertFalse(idField.getAccess().isPatchable());
        Assert.assertTrue(idField.getAccess().isSortable());
        Assert.assertTrue(idField.getAccess().isFilterable());
    }

    @Test
    public void testJpaTransient() {
        // available on resource-layer
        ResourceInformation information = builder.build(JpaTransientTestEntity.class);
        Assert.assertNotNull(information.findFieldByName("id"));
        Assert.assertNotNull(information.findFieldByName("task"));

        // not available on jpa-layer
        MetaDataObject entityMeta = jpaMetaProvider.discoverMeta(JpaTransientTestEntity.class);
        Assert.assertTrue(entityMeta.hasAttribute("id"));
        Assert.assertFalse(entityMeta.hasAttribute("task"));
    }

    @Test
    public void testStringAttributeAccess() {
        ResourceInformation info = builder.build(TestEntity.class);
        ResourceField field = info.findAttributeFieldByName("stringValue");
        Assert.assertTrue(field.getAccess().isPostable());
        Assert.assertTrue(field.getAccess().isPatchable());
        Assert.assertTrue(field.getAccess().isSortable());
        Assert.assertTrue(field.getAccess().isFilterable());
    }

    @Test
    public void testLongAttributeAccess() {
        ResourceInformation info = builder.build(VersionedEntity.class);
        ResourceField field = info.findAttributeFieldByName("longValue");
        Assert.assertTrue(field.getAccess().isPostable());
        Assert.assertTrue(field.getAccess().isPatchable());
    }

    @Test
    public void testVersionAccess() {
        ResourceInformation info = builder.build(VersionedEntity.class);
        ResourceField field = info.findAttributeFieldByName("version");
        // must not be immutable to support optimistic locking
        Assert.assertTrue(field.getAccess().isPostable());
        Assert.assertTrue(field.getAccess().isPatchable());
    }

    @Test
    public void testOneToOneRelation() {
        ResourceInformation info = builder.build(OneToOneTestEntity.class);
        ResourceField field = info.findRelationshipFieldByName("oneRelatedValue");
        Assert.assertEquals(ResourceFieldType.RELATIONSHIP, field.getResourceFieldType());
        Assert.assertEquals("related", field.getOppositeResourceType());
        Assert.assertEquals(SerializeType.LAZY, field.getSerializeType());
    }

    @Test
    public void testManyToManyRelation() {
        ResourceInformation info = builder.build(ManyToManyTestEntity.class);
        ResourceField field = info.findRelationshipFieldByName("opposites");
        Assert.assertEquals(ResourceFieldType.RELATIONSHIP, field.getResourceFieldType());
        Assert.assertEquals("manyToManyOpposite", field.getOppositeResourceType());
        Assert.assertEquals(SerializeType.LAZY, field.getSerializeType());
        Assert.assertEquals(RelationshipRepositoryBehavior.FORWARD_OWNER, field.getRelationshipRepositoryBehavior());
    }

    @Test
    public void testManyToManyOppositeRelation() {
        ResourceInformation info = builder.build(ManyToManyOppositeEntity.class);
        ResourceField field = info.findRelationshipFieldByName("tests");
        Assert.assertEquals(ResourceFieldType.RELATIONSHIP, field.getResourceFieldType());
        Assert.assertEquals("manyToManyTest", field.getOppositeResourceType());
        Assert.assertEquals(SerializeType.LAZY, field.getSerializeType());
        Assert.assertEquals("opposites", field.getOppositeName());
        Assert.assertEquals(RelationshipRepositoryBehavior.FORWARD_OPPOSITE, field.getRelationshipRepositoryBehavior());
    }

    @Test
    public void testManyToOneRelation() {
        ResourceInformation info = builder.build(TestEntity.class);
        ResourceField field = info.findRelationshipFieldByName("oneRelatedValue");
        Assert.assertEquals(ResourceFieldType.RELATIONSHIP, field.getResourceFieldType());
        Assert.assertEquals("related", field.getOppositeResourceType());
        Assert.assertEquals(SerializeType.LAZY, field.getSerializeType());
        Assert.assertNull(field.getOppositeName());
        Assert.assertEquals(RelationshipRepositoryBehavior.FORWARD_OWNER, field.getRelationshipRepositoryBehavior());
    }

    @Test
    public void testOneToManyRelation() {
        ResourceInformation info = builder.build(TestEntity.class);
        ResourceField field = info.findRelationshipFieldByName("manyRelatedValues");
        Assert.assertEquals(ResourceFieldType.RELATIONSHIP, field.getResourceFieldType());
        Assert.assertEquals("related", field.getOppositeResourceType());
        Assert.assertEquals(SerializeType.LAZY, field.getSerializeType());
        Assert.assertEquals("testEntity", field.getOppositeName());
        Assert.assertEquals(RelationshipRepositoryBehavior.FORWARD_OPPOSITE, field.getRelationshipRepositoryBehavior());
    }
	
	@Test
	public void testManyToOneRelationWithSuperclassGenericsInterface() {
		ResourceInformation info = builder.build(TestSubclassWithSuperclassGenericsInterface.class);
		ResourceField field = info.findRelationshipFieldByName("generic");
		Assert.assertEquals(ResourceFieldType.RELATIONSHIP, field.getResourceFieldType());
		Assert.assertEquals("testSubclassWithSuperclassGenericsInterface", field.getOppositeResourceType());
		Assert.assertEquals(SerializeType.LAZY, field.getSerializeType());
		Assert.assertNull(field.getOppositeName());
		Assert.assertEquals(RelationshipRepositoryBehavior.FORWARD_OWNER, field.getRelationshipRepositoryBehavior());
	}

    @Test
    public void testAttributeAnnotations() throws SecurityException, IllegalArgumentException {
        ResourceInformation info = builder.build(AnnotationTestEntity.class);

        ResourceField lobField = info.findAttributeFieldByName("lobValue");
        ResourceField fieldAnnotatedField = info.findAttributeFieldByName("fieldAnnotatedValue");
        ResourceField columnAnnotatedField = info.findAttributeFieldByName("columnAnnotatedValue");

        Assert.assertFalse(lobField.getAccess().isSortable());
        Assert.assertFalse(lobField.getAccess().isFilterable());
        Assert.assertTrue(lobField.getAccess().isPostable());
        Assert.assertTrue(lobField.getAccess().isPatchable());

        Assert.assertFalse(fieldAnnotatedField.getAccess().isSortable());
        Assert.assertFalse(fieldAnnotatedField.getAccess().isFilterable());
        Assert.assertTrue(fieldAnnotatedField.getAccess().isPostable());
        Assert.assertFalse(fieldAnnotatedField.getAccess().isPatchable());

        Assert.assertTrue(columnAnnotatedField.getAccess().isSortable());
        Assert.assertTrue(columnAnnotatedField.getAccess().isFilterable());
        Assert.assertFalse(columnAnnotatedField.getAccess().isPostable());
        Assert.assertTrue(columnAnnotatedField.getAccess().isPatchable());

        MetaDataObject meta = jpaMetaProvider.discoverMeta(AnnotationTestEntity.class).asDataObject();
        Assert.assertTrue(meta.getAttribute("lobValue").isLob());
        Assert.assertFalse(meta.getAttribute("fieldAnnotatedValue").isLob());
    }

    @Test
    public void testRenamedResourceType() {
        ResourceInformation info = builder.build(RenamedTestEntity.class);
        Assert.assertEquals("renamedResource", info.getResourceType());
    }

    @Test
    public void testJpaResourceAnnotationPath() {
        ResourceInformation info = builder.build(JpaResourcePathTestEntity.class);
        Assert.assertEquals("jpaResourceTestEntity", info.getResourceType());
        Assert.assertEquals("jpa-resource-test-entity", info.getResourcePath());
    }


    @Test
    public void testJsonapiResourceAnnotationPath() {
        ResourceInformation info = builder.build(JsonapiResourcePathTestEntity.class);
        Assert.assertEquals("jsonapiResourceTestEntity", info.getResourceType());
        Assert.assertEquals("jsonapi-resource-test-entity", info.getResourcePath());
    }


    @Test
    public void testReadOnlyField() throws SecurityException, IllegalArgumentException {
        ResourceInformation info = builder.build(AnnotationTestEntity.class);

        ResourceField field = info.findAttributeFieldByName("readOnlyValue");

        Assert.assertFalse(field.getAccess().isPostable());
        Assert.assertFalse(field.getAccess().isPatchable());

        MetaDataObject meta = jpaMetaProvider.discoverMeta(AnnotationTestEntity.class).asDataObject();
        MetaAttribute attribute = meta.getAttribute("readOnlyValue");

        Assert.assertFalse(attribute.isInsertable());
        Assert.assertFalse(attribute.isUpdatable());
    }

    @Test
    public void testMappedSuperclass() throws SecurityException, IllegalArgumentException {
        ResourceInformation info = builder.build(AnnotationMappedSuperclassEntity.class);

        Assert.assertNull(info.getResourceType());

        ResourceField lobField = info.findAttributeFieldByName("lobValue");
        ResourceField fieldAnnotatedField = info.findAttributeFieldByName("fieldAnnotatedValue");
        ResourceField columnAnnotatedField = info.findAttributeFieldByName("columnAnnotatedValue");

        Assert.assertFalse(lobField.getAccess().isSortable());
        Assert.assertFalse(lobField.getAccess().isFilterable());
        Assert.assertTrue(lobField.getAccess().isPostable());
        Assert.assertTrue(lobField.getAccess().isPatchable());

        Assert.assertFalse(fieldAnnotatedField.getAccess().isSortable());
        Assert.assertFalse(fieldAnnotatedField.getAccess().isFilterable());
        Assert.assertTrue(fieldAnnotatedField.getAccess().isPostable());
        Assert.assertFalse(fieldAnnotatedField.getAccess().isPatchable());

        Assert.assertTrue(columnAnnotatedField.getAccess().isSortable());
        Assert.assertTrue(columnAnnotatedField.getAccess().isFilterable());
        Assert.assertFalse(columnAnnotatedField.getAccess().isPostable());
        Assert.assertTrue(columnAnnotatedField.getAccess().isPatchable());

        MetaDataObject meta = jpaMetaProvider.discoverMeta(AnnotationMappedSuperclassEntity.class).asDataObject();
        Assert.assertTrue(meta.getAttribute("lobValue").isLob());
        Assert.assertFalse(meta.getAttribute("fieldAnnotatedValue").isLob());
    }
}
