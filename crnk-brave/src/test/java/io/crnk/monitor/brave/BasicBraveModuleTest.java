package io.crnk.monitor.brave;

import com.github.kristofa.brave.Brave;
import io.crnk.client.http.HttpAdapter;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class BasicBraveModuleTest {

	private BraveModule module;

	private Brave brave;

	@Before
	public void setup() {
		brave = Mockito.mock(Brave.class);
		module = BraveModule.newServerModule(brave);
	}

	@Test
	public void hasProtectedConstructor() {
		ClassTestUtils.assertProtectedConstructor(BraveModule.class);
	}

	@Test
	public void testGetName() {
		Assert.assertEquals("brave", module.getModuleName());
	}


	@Test(expected = IllegalArgumentException.class)
	public void testSetInvalidAdapter() {
		module.setHttpAdapter(Mockito.mock(HttpAdapter.class));
	}


	@Test
	public void testGetBrave() {
		Assert.assertSame(brave, module.getBrave());
	}

}
