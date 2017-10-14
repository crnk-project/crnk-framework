package io.crnk.jpa.meta;

import io.crnk.jpa.model.TestEntity;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaArrayType;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

public class MetaLookupTest {

	private JpaMetaProvider metaProvider;

	@Before
	public void setup() {
		metaProvider = new JpaMetaProvider(Collections.<Class>emptySet());
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(metaProvider);
	}

	@Test
	public void testObjectArrayMeta() {
		MetaArrayType meta = metaProvider.discoverMeta(TestEntity[].class);
		MetaType elementType = meta.getElementType();
		Assert.assertTrue(elementType instanceof MetaDataObject);
	}

	@Test
	public void testPrimitiveArrayMeta() {
		MetaArrayType type = (MetaArrayType) metaProvider.discoverMeta(byte[].class).asType();
		Assert.assertEquals(byte[].class, type.getImplementationClass());
	}

}
