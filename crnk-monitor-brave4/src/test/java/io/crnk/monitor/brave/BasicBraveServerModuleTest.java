package io.crnk.monitor.brave;

import brave.Tracing;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class BasicBraveServerModuleTest {

	private BraveServerModule module;

	private Tracing tracing;

	@Before
	public void setup() {
		tracing = Mockito.mock(Tracing.class);
		module = BraveServerModule.create(tracing);
	}

	@Test
	public void hasProtectedConstructor() {
		ClassTestUtils.assertProtectedConstructor(BraveServerModule.class);
	}

	@Test
	public void testGetName() {
		Assert.assertEquals("brave-server", module.getModuleName());
	}

	@Test
	public void testGetBrave() {
		Assert.assertSame(tracing, module.getTracing());
	}
}
