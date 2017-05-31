package io.crnk.meta;

import io.crnk.meta.model.*;
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
		String keyString = "13";
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
		Assert.assertEquals(mapType, impl.getType());
	}

	@Test
	public void getKey() {
		MetaType keyType = Mockito.mock(MetaType.class);
		Mockito.when(keyType.getImplementationClass()).thenReturn((Class) Integer.class);
		Mockito.when(mapType.getKeyType()).thenReturn(keyType);

		Assert.assertEquals(Integer.valueOf(13), impl.getKey());
	}

	@Test
	public void checkForwardIsLazy() {
		impl.isLazy();
		Mockito.verify(mapAttr, Mockito.times(1)).isLazy();
	}

	@Test
	public void checkForwardIsDerived() {
		impl.isDerived();
		Mockito.verify(mapAttr, Mockito.times(1)).isDerived();
	}


	@Test(expected = UnsupportedOperationException.class)
	public void checkGetAnnotationsNotSupported() {
		impl.getAnnotations();
	}


	@Test(expected = UnsupportedOperationException.class)
	public void checkGetAnnotationNotSupported() {
		impl.getAnnotation(null);
	}


	@Test(expected = UnsupportedOperationException.class)
	public void checkSetOppositeAttributeNotSupported() {
		impl.setOppositeAttribute(null);
	}


	@Test
	public void checkForwardIsAssociation() {
		impl.isAssociation();
		Mockito.verify(mapAttr, Mockito.times(1)).isAssociation();
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
