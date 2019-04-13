package io.crnk.data.jpa.meta;

import io.crnk.data.jpa.model.AnnotationTestEntity;
import io.crnk.data.jpa.model.TestEntity;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

public class JpaNullabilityMetaTest {

	private JpaMetaProvider metaProvider;

	@Before
	public void setup() {
		metaProvider = new JpaMetaProvider(Collections.emptySet());
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(metaProvider);
	}

	@Test
	public void testPrimaryKeyNotNullable() {
		MetaEntity meta = metaProvider.discoverMeta(TestEntity.class);
		MetaKey primaryKey = meta.getPrimaryKey();
		MetaAttribute idField = primaryKey.getElements().get(0);
		Assert.assertFalse(idField.isNullable());
	}

	@Test
	public void testPrimitiveValueNotNullable() {
		MetaEntity meta = metaProvider.discoverMeta(TestEntity.class);
		MetaAttribute field = meta.getAttribute("longValue");
		Assert.assertFalse(field.isNullable());
	}

	@Test
	public void testObjectValueNullable() {
		MetaEntity meta = metaProvider.discoverMeta(TestEntity.class);
		MetaAttribute field = meta.getAttribute("stringValue");
		Assert.assertTrue(field.isNullable());
	}

	@Test
	public void testColumnAnnotatedValueIsNullable() {
		MetaEntity meta = metaProvider.discoverMeta(AnnotationTestEntity.class);
		MetaAttribute field = meta.getAttribute("nullableValue");
		Assert.assertTrue(field.isNullable());
	}

	@Test
	public void testColumnAnnotatedValueIsNotNullable() {
		MetaEntity meta = metaProvider.discoverMeta(AnnotationTestEntity.class);
		MetaAttribute field = meta.getAttribute("notNullableValue");
		Assert.assertFalse(field.isNullable());
	}

	@Test
	public void testNonOptionalRelatedValue() {
		MetaEntity meta = metaProvider.discoverMeta(AnnotationTestEntity.class);
		MetaAttribute field = meta.getAttribute("nonOptionalRelatedValue");
		Assert.assertFalse(field.isNullable());
	}

	@Test
	public void testOptionalRelatedValue() {
		MetaEntity meta = metaProvider.discoverMeta(AnnotationTestEntity.class);
		MetaAttribute field = meta.getAttribute("optionalRelatedValue");
		Assert.assertTrue(field.isNullable());
	}

}
