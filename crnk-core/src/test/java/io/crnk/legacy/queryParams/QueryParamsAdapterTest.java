package io.crnk.legacy.queryParams;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.DefaultInformationBuilder;
import io.crnk.core.engine.internal.information.resource.DefaultResourceFieldInformationProvider;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInformationProvider;
import io.crnk.core.engine.internal.jackson.JacksonResourceFieldInformationProvider;
import io.crnk.core.engine.internal.registry.ResourceRegistryImpl;
import io.crnk.core.engine.registry.DefaultResourceRegistryPart;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.legacy.internal.QueryParamsAdapter;
import io.crnk.legacy.registry.DefaultResourceInformationProviderContext;
import org.junit.Assert;
import org.junit.Test;

public class QueryParamsAdapterTest {

	@Test
	public void test() {
		ModuleRegistry moduleRegistry = new ModuleRegistry();
		moduleRegistry.getHttpRequestContextProvider().setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		ResourceRegistry resourceRegistry = new ResourceRegistryImpl(new DefaultResourceRegistryPart(), moduleRegistry);
		QueryParams params = new QueryParams();

		DefaultResourceInformationProvider builder = new DefaultResourceInformationProvider(new DefaultResourceFieldInformationProvider(), new JacksonResourceFieldInformationProvider());
		builder.init(new DefaultResourceInformationProviderContext(builder, new DefaultInformationBuilder(moduleRegistry.getTypeParser()),  moduleRegistry.getTypeParser(), new ObjectMapper()));
		ResourceInformation info = builder.build(Task.class);

		QueryParamsAdapter adapter = new QueryParamsAdapter(info, params, moduleRegistry);
		Assert.assertEquals(Task.class, adapter.getResourceInformation().getResourceClass());
		Assert.assertEquals(resourceRegistry, adapter.getResourceRegistry());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetNonExistingResourceClass() {
		QueryParams params = new QueryParams();
		QueryParamsAdapter adapter = new QueryParamsAdapter(params);
		adapter.getResourceInformation();
	}

	@Test(expected = IllegalStateException.class)
	public void testGetNonExistingRegistry() {
		QueryParams params = new QueryParams();
		QueryParamsAdapter adapter = new QueryParamsAdapter(params);
		adapter.getResourceRegistry();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testDuplicate() {
		QueryParams params = new QueryParams();
		QueryParamsAdapter adapter = new QueryParamsAdapter(params);
		adapter.duplicate();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetLimit() {
		QueryParams params = new QueryParams();
		QueryParamsAdapter adapter = new QueryParamsAdapter(params);
		adapter.getLimit();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetOffset() {
		QueryParams params = new QueryParams();
		QueryParamsAdapter adapter = new QueryParamsAdapter(params);
		adapter.getOffset();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetLimit() {
		QueryParams params = new QueryParams();
		QueryParamsAdapter adapter = new QueryParamsAdapter(params);
		adapter.setLimit(0L);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetOffset() {
		QueryParams params = new QueryParams();
		QueryParamsAdapter adapter = new QueryParamsAdapter(params);
		adapter.setOffset(0L);
	}
}
