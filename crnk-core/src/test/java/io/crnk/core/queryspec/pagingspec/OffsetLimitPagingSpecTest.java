package io.crnk.core.queryspec.pagingspec;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OffsetLimitPagingSpecTest {

	@Test
	public void testIsRequired() {
		assertTrue(new OffsetLimitPagingSpec(1L, null).isRequired());
		assertTrue(new OffsetLimitPagingSpec(0L, 30L).isRequired());
	}

	@Test
	public void testIsNotRequired() {
		assertFalse(new OffsetLimitPagingSpec().isRequired());
	}
}
