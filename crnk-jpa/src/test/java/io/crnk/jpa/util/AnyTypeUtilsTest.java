package io.crnk.jpa.util;

import io.crnk.jpa.internal.query.AnyUtils;
import io.crnk.jpa.meta.JpaMetaProvider;
import io.crnk.jpa.model.TestAnyType;
import io.crnk.meta.MetaLookup;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;

public class AnyTypeUtilsTest {

	@Test
	public void testNotInstantiable() throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		Constructor<AnyUtils> constructor = AnyUtils.class.getDeclaredConstructor();
		Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void testSet() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		lookup.initialize();
		TestAnyType anyValue = new TestAnyType();
		AnyUtils.setValue(lookup, anyValue, "stringValue");
		Assert.assertEquals("stringValue", anyValue.getStringValue());
		AnyUtils.setValue(lookup, anyValue, 12);
		Assert.assertEquals(12, anyValue.getIntValue().intValue());
		Assert.assertNull(anyValue.getStringValue());
		AnyUtils.setValue(lookup, anyValue, null);
		Assert.assertNull(anyValue.getIntValue());
	}

	@Test(expected = IllegalStateException.class)
	public void testNotAssignable() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		TestAnyType anyValue = new TestAnyType();
		AnyUtils.setValue(lookup, anyValue, BigDecimal.ONE);
	}
}
