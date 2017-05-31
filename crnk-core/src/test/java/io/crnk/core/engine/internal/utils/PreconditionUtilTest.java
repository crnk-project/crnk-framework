package io.crnk.core.engine.internal.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.Assert;
import org.junit.Test;

public class PreconditionUtilTest {

	@Test
	public void testConstructorIsPrivate()
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		Constructor<PreconditionUtil> constructor = PreconditionUtil.class.getDeclaredConstructor();
		Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void testSatisfied() {
		PreconditionUtil.assertEquals(null, null, null);
		PreconditionUtil.assertEquals(null, 1, 1);
		PreconditionUtil.assertTrue(null, true);
		PreconditionUtil.assertFalse(null, false);
		PreconditionUtil.assertNotNull(null, "test");
		PreconditionUtil.assertNull(null, null);
	}

	@Test(expected = IllegalStateException.class)
	public void testObjectEqualsNotSatisfied() {
		PreconditionUtil.assertEquals("message", new Object(), new Object());
	}


	@Test(expected = IllegalStateException.class)
	public void testEqualsNotSatisfied() {
		PreconditionUtil.assertEquals(null, 1, 2);
	}

	@Test(expected = IllegalStateException.class)
	public void testEqualsNotSatisfied2() {
		PreconditionUtil.assertEquals(null, "a", "b");
	}

	@Test(expected = IllegalStateException.class)
	public void testTrueNotSatisfied() {
		PreconditionUtil.assertTrue(null, false);
	}

	@Test(expected = IllegalStateException.class)
	public void testFalseNotSatisfied() {
		PreconditionUtil.assertFalse(null, true);
	}

	@Test(expected = IllegalStateException.class)
	public void testNotNullNotSatisfied() {
		PreconditionUtil.assertNotNull(null, null);
	}

	@Test(expected = IllegalStateException.class)
	public void testNullNotSatisfied() {
		PreconditionUtil.assertNull(null, "not null");
	}
}
