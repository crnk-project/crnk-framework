package io.crnk.core.utils;

import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

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

