package io.crnk.jpa.util;

import io.crnk.jpa.internal.query.AnyUtils;
import io.crnk.jpa.meta.JpaMetaProvider;
import io.crnk.jpa.model.TestAnyType;
import io.crnk.meta.MetaLookup;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collections;

public class AnyTypeUtilsTest {

	private JpaMetaProvider metaProvider;

	@Before
	public void setup() {
		metaProvider = new JpaMetaProvider(Collections.<Class>emptySet());
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(metaProvider);
		metaProvider.discoverMeta(TestAnyType.class);
	}

	@Test
	public void testNotInstantiable()
			throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException,
			IllegalArgumentException, InvocationTargetException {
		Constructor<AnyUtils> constructor = AnyUtils.class.getDeclaredConstructor();
		Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void testSet() {
		TestAnyType anyValue = new TestAnyType();
		AnyUtils.setValue(metaProvider.getPartition(), anyValue, "stringValue");
		Assert.assertEquals("stringValue", anyValue.getStringValue());
		AnyUtils.setValue(metaProvider.getPartition(), anyValue, 12);
		Assert.assertEquals(12, anyValue.getIntValue().intValue());
		Assert.assertNull(anyValue.getStringValue());
		AnyUtils.setValue(metaProvider.getPartition(), anyValue, null);
		Assert.assertNull(anyValue.getIntValue());
	}
}
