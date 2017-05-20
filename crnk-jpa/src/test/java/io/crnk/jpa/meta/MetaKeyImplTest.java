package io.crnk.jpa.meta;

import io.crnk.jpa.model.TestEntity;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaKey;
import org.junit.Assert;
import org.junit.Test;

public class MetaKeyImplTest {

	@Test
	public void test() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		MetaEntity meta = lookup.getMeta(TestEntity.class, MetaEntity.class);
		MetaKey primaryKey = meta.getPrimaryKey();
		Assert.assertTrue(primaryKey.isUnique());
		Assert.assertEquals("TestEntity$primaryKey", primaryKey.getName());
		Assert.assertEquals(1, primaryKey.getElements().size());
	}
}
