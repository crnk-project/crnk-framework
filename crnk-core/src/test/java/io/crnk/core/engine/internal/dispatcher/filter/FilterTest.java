package io.crnk.core.engine.internal.dispatcher.filter;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.filter.DocumentFilterChain;
import io.crnk.core.engine.filter.DocumentFilterContext;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistry;
import io.crnk.core.engine.internal.dispatcher.controller.CollectionGet;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.internal.http.HttpRequestDispatcherImpl;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.repository.mock.NewInstanceRepositoryMethodParameterProvider;
import io.crnk.core.engine.result.ImmediateResult;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.mock.MockConstants;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.core.resource.registry.ResourceRegistryTest;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class FilterTest {

	String path = "/tasks/";

	String requestType = "GET";

	private TestFilter filter;

	private RequestDispatcher dispatcher;

	private CollectionGet collectionGet;

	private CoreTestContainer container;


	@Before
	public void prepare() {
		// GIVEN
		filter = mock(TestFilter.class);

		SimpleModule filterModule = new SimpleModule("filter");
		filterModule.addFilter(filter);

		container = new CoreTestContainer();
		container.setDefaultPackage();
		container.addModule(filterModule);
		container.boot();

		collectionGet = mock(CollectionGet.class);
		ControllerRegistry controllerRegistry = container.getBoot().getControllerRegistry();
		controllerRegistry.getControllers().clear();
		controllerRegistry.addController(collectionGet);

		dispatcher = new HttpRequestDispatcherImpl(container.getModuleRegistry(), null);
	}

	@Test
	public void test() {
		// WHEN
		ArgumentCaptor<DocumentFilterContext> captor = ArgumentCaptor.forClass(DocumentFilterContext.class);
		when(collectionGet.isAcceptable(any(JsonPath.class), eq(requestType))).thenCallRealMethod();
		when(collectionGet.isAcceptable(any(JsonPath.class), eq(requestType))).thenCallRealMethod();
		when(collectionGet.handleAsync(any(JsonPath.class), any(QueryAdapter.class),
				any(RepositoryMethodParameterProvider.class), any(Document.class))).thenReturn(new ImmediateResult<>(null));

		when(filter.filter(any(DocumentFilterContext.class), any(DocumentFilterChain.class))).thenCallRealMethod();

		Map<String, Set<String>> queryParams = new HashMap<>();
		RepositoryMethodParameterProvider parameterProvider = new NewInstanceRepositoryMethodParameterProvider();
		Document requestBody = new Document();
		dispatcher.dispatchRequest(path, requestType, queryParams, parameterProvider, requestBody);

		// THEN
		verify(filter).filter(captor.capture(), any(DocumentFilterChain.class));
		verify(collectionGet, times(1))
				.handleAsync(any(JsonPath.class), any(QueryAdapter.class), any(RepositoryMethodParameterProvider.class),
						any(Document.class));
		verify(filter, times(1)).filter(any(DocumentFilterContext.class), any(DocumentFilterChain.class));

		DocumentFilterContext value = captor.getValue();
		Assert.assertEquals("tasks", value.getJsonPath().getElementName());
		Assert.assertEquals(parameterProvider, value.getParameterProvider());
		Assert.assertEquals(requestBody, value.getRequestBody());
		Assert.assertEquals("GET", value.getMethod());
	}
}
