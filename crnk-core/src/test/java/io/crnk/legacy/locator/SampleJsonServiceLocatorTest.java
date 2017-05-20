package io.crnk.legacy.locator;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

public class SampleJsonServiceLocatorTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void onValidClassShouldReturnInstance() {
		// GIVEN
		SampleJsonServiceLocator sut = new SampleJsonServiceLocator();

		// WHEN
		Object object = sut.getInstance(Object.class);

		// THEN
		Assert.assertNotNull(object);
	}

	@Test
	public void onClassWithPrivateConstructorShouldThrowException() {
		// GIVEN
		SampleJsonServiceLocator sut = new SampleJsonServiceLocator();

		// THEN
		expectedException.expect(RuntimeException.class);

		// WHEN
		sut.getInstance(Arrays.class);
	}
}
