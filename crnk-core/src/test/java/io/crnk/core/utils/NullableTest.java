package io.crnk.core.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.NoSuchElementException;

public class NullableTest {


	@Test(expected = NoSuchElementException.class)
	public void throwExceptionWhenNotPresent() {
		Nullable.empty().get();
	}

	@Test
	public void ofNullable() {
		Assert.assertEquals(13, Nullable.ofNullable(13).get().intValue());
		Assert.assertFalse(Nullable.ofNullable(null).isPresent());
	}
}

