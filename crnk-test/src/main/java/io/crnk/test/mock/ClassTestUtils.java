package io.crnk.test.mock;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.Assert;

public class ClassTestUtils {

	public static void assertPrivateConstructor(Class<?> clazz) {
		Constructor[] constructors = clazz.getDeclaredConstructors();
		Assert.assertEquals(1, constructors.length);
		Assert.assertTrue(Modifier.isPrivate(constructors[0].getModifiers()));

		// ensure coverage
		try {
			constructors[0].setAccessible(true);
			Assert.assertNotNull(constructors[0].newInstance());
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * for example CDI in need for such constructors to create proxy objects.
	 */
	public static void assertProtectedConstructor(Class<?> clazz) {
		try {
			Constructor constructor = clazz.getDeclaredConstructor();
			Assert.assertTrue(Modifier.isProtected(constructor.getModifiers()));
			constructor.setAccessible(true);
			Assert.assertNotNull(constructor.newInstance());
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
