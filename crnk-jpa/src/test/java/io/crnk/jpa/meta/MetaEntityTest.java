package io.crnk.jpa.meta;

import io.crnk.jpa.model.SequenceEntity;
import io.crnk.jpa.model.TestMappedSuperclassWithPk;
import io.crnk.jpa.model.TestSubclassWithSuperclassGenerics;
import io.crnk.jpa.model.TestSubclassWithSuperclassPk;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaPrimaryKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class MetaEntityTest {

	private JpaMetaProvider metaProvider;

	@Before
	public void setup() {
		metaProvider = new JpaMetaProvider(Collections.emptySet());
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(metaProvider);
	}

	@Test
	public void testPrimaryKeyOnParentMappedSuperClass() {

		MetaEntity meta = metaProvider.discoverMeta(TestSubclassWithSuperclassPk.class);
		MetaPrimaryKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull(primaryKey);
		Assert.assertEquals(1, primaryKey.getElements().size());
		Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
		Assert.assertTrue(primaryKey.getElements().get(0).isPrimaryKeyAttribute());
		Assert.assertFalse(primaryKey.isGenerated());
	}

	@Test
	public void testPrimaryKeyOnMappedSuperClass() {
		MetaMappedSuperclass meta = metaProvider.discoverMeta(TestMappedSuperclassWithPk.class);
		MetaPrimaryKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull(primaryKey);
		Assert.assertEquals(1, primaryKey.getElements().size());
		Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
		Assert.assertTrue(primaryKey.getElements().get(0).isPrimaryKeyAttribute());
		Assert.assertFalse(primaryKey.isGenerated());
	}

	@Test
	public void testGeneratedPrimaryKey() {
		MetaDataObject meta = metaProvider.discoverMeta(SequenceEntity.class).asDataObject();
		MetaPrimaryKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull(primaryKey);
		Assert.assertEquals(1, primaryKey.getElements().size());
		Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
		Assert.assertTrue(primaryKey.isGenerated());
	}

	@Test
	public void testGenericsOnParentMappedSuperClass() {
		MetaEntity meta = metaProvider.discoverMeta(TestSubclassWithSuperclassGenerics.class);
		MetaPrimaryKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull(primaryKey);
		Assert.assertEquals(1, primaryKey.getElements().size());
		Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
		Assert.assertTrue(primaryKey.getElements().get(0).isPrimaryKeyAttribute());
		Assert.assertFalse(primaryKey.isGenerated());
		MetaAttribute genericAttr = meta.getAttribute("generic");
		Assert.assertNotNull(genericAttr);
		Assert.assertEquals(Integer.class, genericAttr.getType().getImplementationClass());

		MetaAttribute genericListAttr = meta.getAttribute("genericList");
		Assert.assertNotNull(genericListAttr);
		Assert.assertEquals(List.class, genericListAttr.getType().getImplementationClass());

		MetaAttribute genericListList= meta.getAttribute("genericListSuper");
		Assert.assertNotNull(genericListList);
		Assert.assertEquals(List.class, genericListList.getType().getImplementationClass());
	}
}
