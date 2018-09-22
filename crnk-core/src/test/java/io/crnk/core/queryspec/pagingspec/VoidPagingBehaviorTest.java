package io.crnk.core.queryspec.pagingspec;

import org.junit.Before;
import org.junit.Test;

/**
 * Rather a dummy-test to not decrease code coverage.
 * No real benefit in testing that all methods of {@link VoidPagingBehavior}
 * throw an {@link UnsupportedOperationException}.
 */
public class VoidPagingBehaviorTest {

	private VoidPagingBehavior voidPagingBehavior;

	@Before
	public void testSetup() {
		voidPagingBehavior = new VoidPagingBehavior();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testBuild() {
		voidPagingBehavior.build(null, null, null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testCreateDefaultPagingSpec() {
		voidPagingBehavior.createDefaultPagingSpec();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testCreateEmptyPagingSpec() {
		voidPagingBehavior.createEmptyPagingSpec();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testDeserialize() {
		voidPagingBehavior.deserialize(null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSerialize() {
		voidPagingBehavior.serialize(null, null);
	}
}
