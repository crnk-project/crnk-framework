package io.crnk.jpa.meta;

import io.crnk.jpa.model.TestMappedSuperclass;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

public class MetaMappedSuperTypeTest {

	private JpaMetaProvider metaProvider;

	@Before
	public void setup() {
		metaProvider = new JpaMetaProvider(Collections.emptySet());
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(metaProvider);
	}

	@Test
	public void testMetaMappedSuperclassDiscovery() {
		MetaElement meta = metaProvider.discoverMeta(TestMappedSuperclass.class);
		Assert.assertTrue(meta instanceof MetaMappedSuperclass);
	}
}
