package io.crnk.core.engine.internal.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.Assert;

public class CoreClassTestUtils {

	public static void assertPrivateConstructor(Class<?> clazz) {
		Constructor[] constructors = clazz.getDeclaredConstructors();
		Assert.assertEquals(1, constructors.length);
		Assert.assertTrue(Modifier.isPrivate(constructors[0].getModifiers()));

		// ensure coverage
		try {
			constructors[0].setAccessible(true);
			Assert.assertNotNull(constructors[0].newInstance());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
