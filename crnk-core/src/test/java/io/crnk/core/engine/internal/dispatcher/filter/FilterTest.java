package io.crnk.core.engine.internal.dispatcher.filter;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.filter.DocumentFilterChain;
import io.crnk.core.engine.filter.DocumentFilterContext;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistry;
import io.crnk.core.engine.internal.dispatcher.controller.CollectionGetController;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.http.HttpRequestDispatcherImpl;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.result.ImmediateResult;
import io.crnk.core.module.SimpleModule;
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

	private TestFilter filter;

	private RequestDispatcher dispatcher;

	private CollectionGetController collectionGetController;

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

		collectionGetController = mock(CollectionGetController.class);
		ControllerRegistry controllerRegistry = container.getBoot().getControllerRegistry();
		controllerRegistry.getControllers().clear();
		controllerRegistry.addController(collectionGetController);

		dispatcher = new HttpRequestDispatcherImpl(container.getModuleRegistry(), null);
	}

	@Test
	public void test() {
		// WHEN
		ArgumentCaptor<DocumentFilterContext> captor = ArgumentCaptor.forClass(DocumentFilterContext.class);
		when(collectionGetController.isAcceptable(any(JsonPath.class), eq(requestType))).thenCallRealMethod();
		when(collectionGetController.isAcceptable(any(JsonPath.class), eq(requestType))).thenCallRealMethod();
		when(collectionGetController.handleAsync(any(JsonPath.class), any(QueryAdapter.class), any(Document.class))).thenReturn(new ImmediateResult<>(null));

		when(filter.filter(any(DocumentFilterContext.class), any(DocumentFilterChain.class))).thenCallRealMethod();

		Map<String, Set<String>> queryParams = new HashMap<>();
		Document requestBody = new Document();
		dispatcher.dispatchRequest(path, requestType, queryParams,  requestBody);

		// THEN
		verify(filter).filter(captor.capture(), any(DocumentFilterChain.class));
		verify(collectionGetController, times(1))
				.handleAsync(any(JsonPath.class), any(QueryAdapter.class), any(Document.class));
		verify(filter, times(1)).filter(any(DocumentFilterContext.class), any(DocumentFilterChain.class));

		DocumentFilterContext value = captor.getValue();
		Assert.assertEquals("tasks", value.getJsonPath().getRootEntry().getResourceInformation().getResourceType());
		Assert.assertEquals(requestBody, value.getRequestBody());
		Assert.assertEquals("GET", value.getMethod());
	}
}
