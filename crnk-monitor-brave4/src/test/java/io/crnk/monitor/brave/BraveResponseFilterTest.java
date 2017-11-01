package io.crnk.monitor.brave;

import java.util.ArrayList;
import java.util.Arrays;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import io.crnk.monitor.brave.internal.BraveRepositoryFilter;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.ErrorDataBuilder;
import io.crnk.core.engine.filter.RepositoryFilterContext;
import io.crnk.core.engine.filter.RepositoryRequestFilterChain;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.module.Module;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

@Ignore // TODO deal with the new finals in Brave code base
public class BraveResponseFilterTest {

	private Tracing tracing;

	private CrnkBoot boot;

	private Module.ModuleContext moduleContext;

	private BraveRepositoryFilter filter;

	private RepositoryFilterContext filterContext;

	private RepositoryRequestFilterChain filterChain;

	private RepositoryRequestSpec requestSpec;

	private QueryAdapter queryAdapter;

	private Span span;

	private Tracer tracer;


	@Before
	public void setup() {
		boot = new CrnkBoot();
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery("io.crnk.test.mock.repository", new SampleJsonServiceLocator
				()));
		boot.boot();

		span = Mockito.mock(Span.class);
		tracing = Mockito.mock(Tracing.class);
		tracer = Mockito.mock(Tracer.class);
		Mockito.when(tracing.tracer()).thenReturn(tracer);
		Mockito.when(tracer.nextSpan()).thenReturn(span);

		moduleContext = Mockito.mock(Module.ModuleContext.class);
		Mockito.when(moduleContext.getResourceRegistry()).thenReturn(boot.getResourceRegistry());

		QuerySpec querySpec = new QuerySpec(Task.class);
		queryAdapter = new QuerySpecAdapter(querySpec, boot.getResourceRegistry());

		ResourceInformation taskResourceInformation = boot.getResourceRegistry().getEntry(Task.class).getResourceInformation();
		requestSpec = Mockito.mock(RepositoryRequestSpec.class);
		Mockito.when(requestSpec.getMethod()).thenReturn(HttpMethod.GET);
		Mockito.when(requestSpec.getQueryAdapter()).thenReturn(queryAdapter);
		Mockito.when(requestSpec.getQuerySpec(taskResourceInformation))
				.thenReturn(querySpec);

		filter = new BraveRepositoryFilter(tracing, moduleContext);
		filterContext = Mockito.mock(RepositoryFilterContext.class);
		Mockito.when(filterContext.getRequest()).thenReturn(requestSpec);

		filterChain = Mockito.mock(RepositoryRequestFilterChain.class);
	}

	@Test
	public void statusCodeOkWhenNpErrors() {
		JsonApiResponse response = new JsonApiResponse();
		Mockito.when(filterChain.doFilter(Mockito.any(RepositoryFilterContext.class))).thenReturn(response);
		filter.filterRequest(filterContext, filterChain);
		Mockito.verify(span, Mockito.times(1)).tag(BraveRepositoryFilter.STATUS_CODE_ANNOTATION,
				BraveRepositoryFilter.STRING_OK);
	}


	@Test
	public void statusCodeOkWhenEmptyErrors() {
		JsonApiResponse response = new JsonApiResponse();
		response.setErrors(new ArrayList<ErrorData>());
		Mockito.when(filterChain.doFilter(Mockito.any(RepositoryFilterContext.class))).thenReturn(response);
		filter.filterRequest(filterContext, filterChain);
		Mockito.verify(span, Mockito.times(1)).tag(BraveRepositoryFilter.STATUS_CODE_ANNOTATION,
				BraveRepositoryFilter.STRING_OK);
	}


	@Test
	public void statusCodeOkWhenException() {
		Mockito.when(filterChain.doFilter(Mockito.any(RepositoryFilterContext.class))).thenThrow(new IllegalStateException());

		try {
			filter.filterRequest(filterContext, filterChain);
			Assert.fail();
		}
		catch (IllegalStateException e) {
			// ok
		}

		Mockito.verify(span, Mockito.times(1)).tag(BraveRepositoryFilter.STATUS_CODE_ANNOTATION,
				BraveRepositoryFilter.STRING_EXCEPTION);
	}

	@Test
	public void statusCodeNotOkWhenEmptyErrors() {
		ErrorData errorData = new ErrorDataBuilder().setId("test").build();
		JsonApiResponse response = new JsonApiResponse();
		response.setErrors(Arrays.asList(errorData));
		Mockito.when(filterChain.doFilter(Mockito.any(RepositoryFilterContext.class))).thenReturn(response);
		filter.filterRequest(filterContext, filterChain);
		Mockito.verify(span, Mockito.times(1)).tag(BraveRepositoryFilter.STATUS_CODE_ANNOTATION,
				BraveRepositoryFilter.STRING_EXCEPTION);
	}

}
