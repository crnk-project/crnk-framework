package io.crnk.core.engine.internal.repository;

import io.crnk.core.CoreTestModule;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryAdapterUtilsTest {

	private static final String TEST_PATH = "http://test.path/";

	private ModuleRegistry moduleRegistry;

	@Mock
	private RepositoryRequestSpec requestSpec;

	@Mock
	private QuerySpecAdapter queryAdapter;

	@Mock
	private ResourceInformation responseResourceInformation;

	@Mock
	private ResourceInformation resourceInformation;

	@Mock
	private ResourceRegistry resourceRegistry;

	@Mock
	private OffsetLimitPagingBehavior offsetLimitPagingBehavior;

	@Before
	public void setup() {
		CrnkBoot boot = new CrnkBoot();
		boot.addModule(new CoreTestModule());
		boot.boot();
		moduleRegistry = boot.getModuleRegistry();

		when(queryAdapter.duplicate()).thenReturn(queryAdapter);
		when(queryAdapter.getResourceInformation()).thenReturn(resourceInformation);
		QueryContext queryContext = Mockito.mock(QueryContext.class);
		when(queryContext.getBaseUrl()).thenReturn("http://localhost:8080");
		when(queryAdapter.getQueryContext()).thenReturn(queryContext);
		when(responseResourceInformation.getResourceType()).thenReturn("tasks");
		when(requestSpec.getQueryAdapter()).thenReturn(queryAdapter);
		when(requestSpec.getResponseResourceInformation()).thenReturn(responseResourceInformation);
	}

	@Test
	public void enrichSelfLinksInformationNoQuerySpec() {
		ResourceList resources = new DefaultResourceList();

		when(requestSpec.getQueryAdapter()).thenReturn(mock(QueryAdapter.class));

		LinksInformation result = RepositoryAdapterUtils.enrichLinksInformation(moduleRegistry, null, resources, requestSpec);

		assertThat(result, is(nullValue()));
	}
}
