package io.crnk.jpa.meta;

import io.crnk.jpa.model.TestEntity;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaKey;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

public class MetaKeyImplTest {

	@Test
	public void test() {
		JpaMetaProvider metaProvider = new JpaMetaProvider((Set) Collections.emptySet());
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(metaProvider);
		MetaEntity meta = metaProvider.discoverMeta(TestEntity.class);
		MetaKey primaryKey = meta.getPrimaryKey();
		Assert.assertTrue(primaryKey.isUnique());
		Assert.assertEquals("TestEntity$primaryKey", primaryKey.getName());
		Assert.assertEquals(1, primaryKey.getElements().size());
	}
}
