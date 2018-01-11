package io.crnk.rs;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.crnk.core.boot.CrnkBoot;
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
	public void setDefaultPageLimit() {
		CrnkFeature feature = new CrnkFeature();
		CrnkBoot boot = feature.getBoot();
		boot.boot();

		feature.setDefaultPageLimit(12L);

		DefaultQuerySpecDeserializer deserializer = (DefaultQuerySpecDeserializer) boot.getQuerySpecDeserializer();
		Assert.assertEquals(12L, deserializer.getDefaultLimit().longValue());
	}

	@Test
	public void getQuerySpecDeserializer() {
		CrnkFeature feature = new CrnkFeature();
		CrnkBoot boot = feature.getBoot();
		boot.boot();

		Assert.assertNotNull(feature.getQuerySpecDeserializer());
	}
}