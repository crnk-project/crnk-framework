package io.crnk.meta;

import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaMapAttribute;
import io.crnk.meta.model.MetaMapType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class MetaMapAttributeTest {

	private MetaMapAttribute impl;

	private MetaAttribute mapAttr;

	private MetaMapType mapType;

	private MetaDataObject parent;

	@Before
	public void setup() {
		String keyString = "test";
		mapAttr = Mockito.mock(MetaAttribute.class);
		mapType = Mockito.mock(MetaMapType.class);
		impl = new MetaMapAttribute(mapType, mapAttr, keyString);
		parent = Mockito.mock(MetaDataObject.class);
		impl.setParent(parent);
	}

	@Test
	public void testGetters() {
		Assert.assertEquals(mapAttr, impl.getMapAttribute());
		Assert.assertEquals(parent, impl.getParent());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getIdNotSupported() {
		impl.getId();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getVersionNotSupported() {
		impl.isVersion();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void isIdNotSupported() {
		impl.isId();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getOppositeAttributeNotSupported() {
		impl.getOppositeAttribute();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getValueNotSupported() {
		impl.getValue(null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void addValueNotSupported() {
		impl.addValue(null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void removeValueNotSupported() {
		impl.removeValue(null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void setValueNotSupported() {
		impl.setValue(null, null);
	}
}
