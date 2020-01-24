package io.crnk.core.engine.internal.utils;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Optional;

public class MethodCacheTest {

	@Test
	public void testMethod() {
		MethodCache cache = new MethodCache();

		Optional<Method> method = cache.find(Date.class, "parse", String.class);
		Assert.assertTrue(method.isPresent());
		Assert.assertEquals("parse", method.get().getName());

		// check for cache hit with assertSame
		Optional<Method> method2 = cache.find(Date.class, "parse", String.class);
		Assert.assertSame(method, method2);
	}

	@Test
	public void testNonExistingMethod() {
		MethodCache cache = new MethodCache();
		Optional<Method> method = cache.find(Date.class, "doesNotExist", String.class);
		Assert.assertFalse(method.isPresent());
	}

	@Test
	public void MethodCacheKeyEquals() {
		EqualsVerifier.forClass(MethodCache.MethodCacheKey.class).usingGetClass().verify();
	}

}
