package io.crnk.rs;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.engine.url.ServiceUrlProvider;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.SecurityContext;

public class CrnkFeatureTest {

	@Test
	public void testServiceServiceUrlProvider() {
		CrnkFeature feature = new CrnkFeature();

		ServiceUrlProvider serviceUrlProvider = Mockito.mock(ServiceUrlProvider.class);
		feature.setServiceUrlProvider(serviceUrlProvider);
		Assert.assertSame(serviceUrlProvider, feature.getBoot().getServiceUrlProvider());
	}

	@Test
	public void testSecurityDisabledDoesNotRegisterProvider() {
		testSecurityRegistration(false);
	}

	@Test
	public void testSecurityEnabledDoesRegisterProvider() {
		testSecurityRegistration(true);
	}

	private void testSecurityRegistration(boolean enabled) {
		CrnkFeature feature = new CrnkFeature();
		feature.setSecurityEnabled(enabled);
		feature.securityContext = Mockito.mock(SecurityContext.class);

		FeatureContext context = Mockito.mock(FeatureContext.class);
		Mockito.when(context.getConfiguration()).thenReturn(Mockito.mock(Configuration.class));

		feature.configure(context);

		CrnkBoot boot = feature.getBoot();
		if (enabled) {
			SecurityProvider securityProvider = boot.getModuleRegistry().getSecurityProvider();
			Assert.assertNotNull(securityProvider);
		} else {
			Assert.assertEquals(0, boot.getModuleRegistry().getSecurityProviders().size());
		}
	}
}