package io.crnk.data.jpa.meta;

import io.crnk.data.jpa.model.TestEntity;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaKey;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class MetaKeyImplTest {

	@Test
	public void test() {
		JpaMetaProvider metaProvider = new JpaMetaProvider(Collections.emptySet());
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(metaProvider);
		MetaEntity meta = metaProvider.discoverMeta(TestEntity.class);
		MetaKey primaryKey = meta.getPrimaryKey();
		Assert.assertTrue(primaryKey.isUnique());
		Assert.assertEquals("TestEntity$primaryKey", primaryKey.getName());
		Assert.assertEquals(1, primaryKey.getElements().size());
	}
}
