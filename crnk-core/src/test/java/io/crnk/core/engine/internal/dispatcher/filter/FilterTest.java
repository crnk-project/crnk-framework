package io.crnk.core.engine.internal.dispatcher.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.filter.DocumentFilterChain;
import io.crnk.core.engine.filter.DocumentFilterContext;
import io.crnk.core.engine.information.resource.ResourceFieldNameTransformer;
import io.crnk.core.engine.information.resource.ResourceInformationBuilder;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistry;
import io.crnk.core.engine.internal.http.HttpRequestProcessorImpl;
import io.crnk.core.engine.internal.dispatcher.controller.CollectionGet;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.internal.information.resource.AnnotationResourceInformationBuilder;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.DefaultQuerySpecDeserializer;
import io.crnk.core.queryspec.internal.QuerySpecAdapterBuilder;
import io.crnk.core.repository.mock.NewInstanceRepositoryMethodParameterProvider;
import io.crnk.core.resource.registry.ResourceRegistryBuilderTest;
import io.crnk.core.resource.registry.ResourceRegistryTest;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.legacy.registry.ResourceRegistryBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class FilterTest {

	String path = "/tasks/";
	String requestType = "GET";
	private ResourceRegistry resourceRegistry;
	private TestFilter filter;
	private RequestDispatcher dispatcher;
	private CollectionGet collectionGet;
	private PathBuilder pathBuilder;
	private ModuleRegistry moduleRegistry;

	@Before
	public void prepare() {
		moduleRegistry = new ModuleRegistry();

		ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer());
		ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(moduleRegistry, new SampleJsonServiceLocator(), resourceInformationBuilder);
		resourceRegistry = registryBuilder.build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, moduleRegistry, new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL));

		pathBuilder = new PathBuilder(resourceRegistry);
		ControllerRegistry controllerRegistry = new ControllerRegistry(null);
		collectionGet = mock(CollectionGet.class);
		controllerRegistry.addController(collectionGet);
		QuerySpecAdapterBuilder queryAdapterBuilder = new QuerySpecAdapterBuilder(new DefaultQuerySpecDeserializer(), moduleRegistry);
		dispatcher = new HttpRequestProcessorImpl(moduleRegistry, controllerRegistry, null, queryAdapterBuilder);
	}

	@Test
	public void test() throws Exception {

		// GIVEN
		filter = mock(TestFilter.class);

		SimpleModule filterModule = new SimpleModule("filter");
		filterModule.addFilter(filter);
		moduleRegistry.addModule(filterModule);
		moduleRegistry.init(new ObjectMapper());

		// WHEN
		ArgumentCaptor<DocumentFilterContext> captor = ArgumentCaptor.forClass(DocumentFilterContext.class);
		when(collectionGet.isAcceptable(any(JsonPath.class), eq(requestType))).thenCallRealMethod();
		when(filter.filter(any(DocumentFilterContext.class), any(DocumentFilterChain.class))).thenCallRealMethod();
		Map<String, Set<String>> queryParams = new HashMap<>();
		RepositoryMethodParameterProvider parameterProvider = new NewInstanceRepositoryMethodParameterProvider();
		Document requestBody = new Document();
		dispatcher.dispatchRequest(path, requestType, queryParams, parameterProvider, requestBody);

		// THEN
		verify(filter).filter(captor.capture(), any(DocumentFilterChain.class));
		verify(collectionGet, times(1)).handle(any(JsonPath.class), any(QueryAdapter.class), any(RepositoryMethodParameterProvider.class), any(Document.class));
		verify(filter, times(1)).filter(any(DocumentFilterContext.class), any(DocumentFilterChain.class));

		DocumentFilterContext value = captor.getValue();
		Assert.assertEquals("tasks", value.getJsonPath().getElementName());
		Assert.assertEquals(parameterProvider, value.getParameterProvider());
		Assert.assertEquals(requestBody, value.getRequestBody());
		Assert.assertEquals("GET", value.getMethod());
	}
}
