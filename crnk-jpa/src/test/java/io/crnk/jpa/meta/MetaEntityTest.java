package io.crnk.jpa.meta;

import io.crnk.jpa.model.SequenceEntity;
import io.crnk.jpa.model.TestMappedSuperclassWithPk;
import io.crnk.jpa.model.TestSubclassWithSuperclassPk;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaPrimaryKey;
import org.junit.Assert;
import org.junit.Test;

//import io.crnk.jpa.model.SequenceEntity;

public class MetaEntityTest {

	@Test
	public void testPrimaryKeyOnParentMappedSuperClass() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		MetaEntity meta = lookup.getMeta(TestSubclassWithSuperclassPk.class, MetaEntity.class);
		MetaPrimaryKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull(primaryKey);
		Assert.assertEquals(1, primaryKey.getElements().size());
		Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
		Assert.assertTrue(primaryKey.getElements().get(0).isPrimaryKeyAttribute());
		Assert.assertFalse(primaryKey.isGenerated());
	}

	@Test
	public void testPrimaryKeyOnMappedSuperClass() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		MetaMappedSuperclass meta = lookup.getMeta(TestMappedSuperclassWithPk.class, MetaMappedSuperclass.class);
		MetaPrimaryKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull(primaryKey);
		Assert.assertEquals(1, primaryKey.getElements().size());
		Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
		Assert.assertTrue(primaryKey.getElements().get(0).isPrimaryKeyAttribute());
		Assert.assertFalse(primaryKey.isGenerated());
	}

	@Test
	public void testGeneratedPrimaryKey() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		MetaDataObject meta = lookup.getMeta(SequenceEntity.class).asDataObject();
		MetaPrimaryKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull(primaryKey);
		Assert.assertEquals(1, primaryKey.getElements().size());
		Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
		Assert.assertTrue(primaryKey.isGenerated());
	}
}
