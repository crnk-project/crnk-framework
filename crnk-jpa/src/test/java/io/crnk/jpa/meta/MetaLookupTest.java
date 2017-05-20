package io.crnk.jpa.meta;

import io.crnk.jpa.model.TestEntity;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaArrayType;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaPrimitiveType;
import io.crnk.meta.model.MetaType;
import org.junit.Assert;
import org.junit.Test;

public class MetaLookupTest {

	@Test
	public void testObjectArrayMeta() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());

		MetaArrayType meta = lookup.getArrayMeta(TestEntity[].class, MetaEntity.class);
		MetaType elementType = meta.getElementType();
		Assert.assertTrue(elementType instanceof MetaDataObject);
	}

	@Test
	public void testPrimitiveArrayMeta() {
		MetaLookup lookup = new MetaLookup();

		MetaPrimitiveType type = (MetaPrimitiveType) lookup.getMeta(byte[].class).asType();
		Assert.assertEquals(byte[].class, type.getImplementationClass());
	}

}
