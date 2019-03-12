package io.crnk.monitor.brave;

import brave.Tracing;
import io.crnk.client.http.HttpAdapter;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class BasicBraveClientModuleTest {

	private BraveClientModule module;

	private Tracing tracing;

	@Before
	public void setup() {
		tracing = Mockito.mock(Tracing.class);
		module = BraveClientModule.create(tracing);
	}

	@Test
	public void hasProtectedConstructor() {
		ClassTestUtils.assertProtectedConstructor(BraveClientModule.class);
	}

	@Test
	public void testGetName() {
		Assert.assertEquals("brave-client", module.getModuleName());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetInvalidAdapter() {
		module.setHttpAdapter(Mockito.mock(HttpAdapter.class));
	}

	@Test
	public void testGetBrave() {
		Assert.assertSame(tracing, module.getHttpTracing().tracing());
	}
}
