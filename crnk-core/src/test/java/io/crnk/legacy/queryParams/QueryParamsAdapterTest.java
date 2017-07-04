package io.crnk.legacy.queryParams;

import io.crnk.core.engine.information.resource.ResourceFieldNameTransformer;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.resource.AnnotationResourceInformationBuilder;
import io.crnk.core.engine.internal.registry.ResourceRegistryImpl;
import io.crnk.core.engine.registry.DefaultResourceRegistryPart;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.legacy.internal.QueryParamsAdapter;
import io.crnk.legacy.registry.DefaultResourceInformationBuilderContext;
import org.junit.Assert;
import org.junit.Test;

public class QueryParamsAdapterTest {

	@Test
	public void test() {
		ModuleRegistry moduleRegistry = new ModuleRegistry();
		ResourceRegistry resourceRegistry = new ResourceRegistryImpl(new DefaultResourceRegistryPart(), moduleRegistry, new ConstantServiceUrlProvider("http://localhost"));
		QueryParams params = new QueryParams();

		AnnotationResourceInformationBuilder builder = new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer());
		builder.init(new DefaultResourceInformationBuilderContext(builder, moduleRegistry.getTypeParser()));
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
