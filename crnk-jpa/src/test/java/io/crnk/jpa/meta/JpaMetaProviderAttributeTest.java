package io.crnk.jpa.meta;

import io.crnk.jpa.model.NamingTestEntity;
import io.crnk.jpa.model.RelatedEntity;
import io.crnk.jpa.model.TestEntity;
import io.crnk.jpa.model.WriteOnlyAttributeTestEntity;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaCollectionType;
import io.crnk.meta.model.MetaMapType;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class JpaMetaProviderAttributeTest {

	@Test
	public void testPrimaryKey() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		lookup.initialize();
		MetaEntity meta = lookup.getMeta(TestEntity.class, MetaEntity.class);
		MetaAttribute attr = meta.getAttribute("id");
		Assert.assertFalse(attr.isAssociation());
		Assert.assertEquals("id", attr.getName());
		Assert.assertEquals(TestEntity.class.getName() + ".id", attr.getId());
		Assert.assertFalse(attr.isDerived());
		Assert.assertFalse(attr.isVersion());
		Assert.assertFalse(attr.isLazy());
		Assert.assertFalse(attr.isCascaded());
		Assert.assertNull(attr.getOppositeAttribute());
	}

	@Test
	public void testAttributeOrder() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		lookup.initialize();
		MetaEntity meta = lookup.getMeta(TestEntity.class, MetaEntity.class);

		List<? extends MetaAttribute> attributes = meta.getAttributes();

		Assert.assertEquals("stringValue", attributes.get(0).getName());
		Assert.assertEquals("superRelatedValue", attributes.get(1).getName());
		Assert.assertEquals("id", attributes.get(2).getName());
		Assert.assertEquals("longValue", attributes.get(3).getName());
		Assert.assertEquals("bytesValue", attributes.get(4).getName());
		Assert.assertEquals("embValue", attributes.get(5).getName());
		Assert.assertEquals("mapValue", attributes.get(6).getName());
		Assert.assertEquals("oneRelatedValue", attributes.get(7).getName());
		Assert.assertEquals("eagerRelatedValue", attributes.get(8).getName());
		Assert.assertEquals("manyRelatedValues", attributes.get(9).getName());
	}

	@Test
	public void testCascaded() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		lookup.initialize();
		MetaEntity meta = lookup.getMeta(TestEntity.class, MetaEntity.class);

		MetaAttribute oneRelatedAttr = meta.getAttribute("oneRelatedValue");
		MetaAttribute eagerRelatedAttr = meta.getAttribute("eagerRelatedValue");
		Assert.assertTrue(oneRelatedAttr.isCascaded());
		Assert.assertFalse(eagerRelatedAttr.isCascaded());
	}

	@Test
	public void testWriteOnlyAttributesIngoredAsNotYetSupported() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		lookup.initialize();
		MetaEntity meta = lookup.getMeta(WriteOnlyAttributeTestEntity.class, MetaEntity.class);

		Assert.assertTrue(meta.hasAttribute("id"));

		// not yet supported
		Assert.assertFalse(meta.hasAttribute("writeOnlyValue"));
	}

	@Test
	public void testFirstCharacterOfNameIsLowerCase() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		lookup.initialize();
		MetaEntity meta = lookup.getMeta(NamingTestEntity.class, MetaEntity.class);

		Assert.assertTrue(meta.hasAttribute("id"));
		Assert.assertTrue(meta.hasAttribute("sEcondUpperCaseValue"));
		Assert.assertFalse(meta.hasAttribute("SEcondUpperCaseValue"));
	}

	@Test
	public void testMapAttr() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		lookup.initialize();
		MetaEntity meta = lookup.getMeta(TestEntity.class, MetaEntity.class);
		MetaAttribute attr = meta.getAttribute(TestEntity.ATTR_mapValue);
		Assert.assertFalse(attr.isAssociation());
		Assert.assertEquals(TestEntity.ATTR_mapValue, attr.getName());
		Assert.assertEquals(TestEntity.class.getName() + "." + TestEntity.ATTR_mapValue, attr.getId());
		Assert.assertFalse(attr.isDerived());
		Assert.assertFalse(attr.isVersion());
		Assert.assertFalse(attr.isLazy());
		Assert.assertNull(attr.getOppositeAttribute());

		MetaMapType mapType = attr.getType().asMap();
		Assert.assertTrue(mapType.isMap());
		Assert.assertEquals(String.class, mapType.getKeyType().getImplementationClass());
		Assert.assertEquals(String.class, mapType.getElementType().getImplementationClass());
		Assert.assertEquals(String.class, attr.getType().getElementType().getImplementationClass());
	}

	@Test
	public void testRelationMany() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		lookup.initialize();
		MetaEntity meta = lookup.getMeta(TestEntity.class, MetaEntity.class);
		MetaAttribute attr = meta.getAttribute(TestEntity.ATTR_manyRelatedValues);
		Assert.assertTrue(attr.isAssociation());
		Assert.assertEquals(TestEntity.ATTR_manyRelatedValues, attr.getName());
		Assert.assertEquals(TestEntity.class.getName() + "." + TestEntity.ATTR_manyRelatedValues, attr.getId());
		Assert.assertFalse(attr.isDerived());
		Assert.assertFalse(attr.isVersion());
		Assert.assertTrue(attr.isLazy());
		Assert.assertNotNull(attr.getOppositeAttribute());

		MetaCollectionType colType = attr.getType().asCollection();
		Assert.assertTrue(colType.isCollection());
		Assert.assertEquals(RelatedEntity.class, colType.getElementType().getImplementationClass());
		Assert.assertEquals(RelatedEntity.class, attr.getType().getElementType().getImplementationClass());
	}
}
