package io.crnk.rs;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.SecurityContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.queryspec.DefaultQuerySpecDeserializer;
import io.crnk.core.queryspec.QuerySpecDeserializer;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class CrnkFeatureTest {

	@Test
	public void testQuerySpecConstructor() {
		ObjectMapper objectMapper = new ObjectMapper();
		QuerySpecDeserializer querySpecDeserializer = new DefaultQuerySpecDeserializer();
		SampleJsonServiceLocator serviceLocator = new SampleJsonServiceLocator();
		CrnkFeature feature = new CrnkFeature(objectMapper, querySpecDeserializer,
				serviceLocator);

		Assert.assertSame(objectMapper, feature.getObjectMapper());
		Assert.assertSame(querySpecDeserializer, feature.getBoot().getQuerySpecDeserializer());
	}

	@Test
	public void testServiceServiceUrlProvider() {
		CrnkFeature feature = new CrnkFeature();

		ServiceUrlProvider serviceUrlProvider = Mockito.mock(ServiceUrlProvider.class);
		feature.setServiceUrlProvider(serviceUrlProvider);
		Assert.assertSame(serviceUrlProvider, feature.getBoot().getServiceUrlProvider());
	}

	@Test
	public void getQuerySpecDeserializer() {
		CrnkFeature feature = new CrnkFeature();
		Assert.assertNotNull(feature.getQuerySpecDeserializer());
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