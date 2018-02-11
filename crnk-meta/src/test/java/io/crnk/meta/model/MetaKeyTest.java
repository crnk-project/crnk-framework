package io.crnk.meta.model;

import io.crnk.meta.AbstractMetaTest;
import io.crnk.meta.model.resource.MetaJsonObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

public class MetaKeyTest extends AbstractMetaTest {


	@Test
	@Ignore // no longer supported to find any kind of object
	public void parse() {
		MetaJsonObject metaKeyType = resourceProvider.getMeta(SomePrimaryKey.class);

		MetaAttribute keyAttr = new MetaAttribute();
		keyAttr.setType(metaKeyType);

		MetaKey metaKey = new MetaKey();
		metaKey.setElements(Arrays.asList(keyAttr));

		SomePrimaryKey key = new SomePrimaryKey();
		key.setAttr1("test");
		key.setAttr2(13);

		String keyString = metaKey.toKeyString(key);
		Assert.assertEquals("test-13", keyString);
	}


	@Test(expected = IllegalStateException.class)
	public void testNonUniquePrimaryKeyAttributeThrowsException() {
		MetaKey key = new MetaKey();
		key.setElements(Arrays.asList(new MetaAttribute(), new MetaAttribute()));
		key.getUniqueElement();
	}


	@Test
	public void testToKeyStringWithNull() {
		MetaKey key = new MetaKey();
		Assert.assertNull(key.toKeyString(null));
	}

	public static class SomePrimaryKey {

		private String attr1;

		private int attr2;

		public String getAttr1() {
			return attr1;
		}

		public void setAttr1(String attr1) {
			this.attr1 = attr1;
		}

		public int getAttr2() {
			return attr2;
		}

		public void setAttr2(int attr2) {
			this.attr2 = attr2;
		}
	}
}
