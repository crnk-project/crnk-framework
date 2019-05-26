package io.crnk.data.jpa.meta;

import io.crnk.data.jpa.model.TestMappedSuperclassWithGenerics;
import io.crnk.meta.MetaLookupImpl;
import io.crnk.meta.model.MetaElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

public class MetaMappedSuperWithGenericsTypeTest {

	private JpaMetaProvider metaProvider;

	@Before
	public void setup() {
		metaProvider = new JpaMetaProvider(Collections.emptySet());
		MetaLookupImpl lookup = new MetaLookupImpl();
		lookup.addProvider(metaProvider);
	}

	@Test
	public void testMetaMappedSuperclassDiscovery() {
		MetaElement meta = metaProvider.discoverMeta(TestMappedSuperclassWithGenerics.class);
		Assert.assertTrue(meta instanceof MetaMappedSuperclass);
	}
}
