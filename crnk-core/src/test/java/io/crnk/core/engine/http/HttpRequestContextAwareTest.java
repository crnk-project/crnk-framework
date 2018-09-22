package io.crnk.core.engine.http;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.mock.MockConstants;
import io.crnk.core.module.Module;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class HttpRequestContextAwareTest {

	interface HttTestModule extends Module, HttpRequestContextAware {

	}

	@Test
	public void check() {
		HttTestModule testModule = Mockito.mock(HttTestModule.class);

		CrnkBoot boot = new CrnkBoot();
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost:8080"));
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery(MockConstants.TEST_MODELS_PACKAGE));
		boot.addModule(testModule);
		boot.boot();

		Mockito.verify(testModule, Mockito.times(1)).setHttpRequestContextProvider(Mockito.any(HttpRequestContextProvider.class));
	}
}
