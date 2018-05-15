package io.crnk.core.engine.internal.repository;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.core.queryspec.pagingspec.PagingSpecUrlBuilder;
import io.crnk.core.resource.links.*;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.DefaultPagedMetaInformation;
import io.crnk.core.resource.meta.PagedMetaInformation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryAdapterUtilsTest {

	private static final String TEST_PATH = "http://test.path/";

	@Mock
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
		when(requestSpec.getQueryAdapter()).thenReturn(queryAdapter);
		when(requestSpec.getResponseResourceInformation()).thenReturn(responseResourceInformation);
	}

	@Test
	public void enrichSelfLinksInformation() {
		ResourceList resources = new DefaultResourceList();
		SelfLinksInformation linksInformation = new DefaultSelfLinksInformation();

		when(queryAdapter.getResourceInformation()).thenReturn(resourceInformation);
		when(queryAdapter.getResourceRegistry()).thenReturn(resourceRegistry);
		when(resourceRegistry.getResourceUrl(resourceInformation)).thenReturn(TEST_PATH);

		SelfLinksInformation result = (SelfLinksInformation) RepositoryAdapterUtils.enrichLinksInformation(
				moduleRegistry, linksInformation, resources, requestSpec);

		assertThat(result.getSelf(), is(equalTo(TEST_PATH)));
	}

	@Test
	public void enrichPageLinksInformation() {
		OffsetLimitPagingSpec offsetLimitPagingSpec = new OffsetLimitPagingSpec(16L, 4L);
		PagedMetaInformation pagedMetaInformation = new DefaultPagedMetaInformation();
		pagedMetaInformation.setTotalResourceCount(7L);
		PagedLinksInformation pagedLinksInformation = new DefaultPagedLinksInformation();
		ResourceList<String> resources = new DefaultResourceList<>(Collections.emptyList(), pagedMetaInformation,
				pagedLinksInformation);

		when(offsetLimitPagingBehavior.isRequired(offsetLimitPagingSpec)).thenReturn(true);
		when(queryAdapter.getPagingSpec()).thenReturn(offsetLimitPagingSpec);
		when(responseResourceInformation.getPagingBehavior()).thenReturn(offsetLimitPagingBehavior);

		RepositoryAdapterUtils.enrichLinksInformation(moduleRegistry, pagedLinksInformation, resources, requestSpec);

		verify(offsetLimitPagingBehavior, times(1)).build(eq(pagedLinksInformation), eq(resources), eq(queryAdapter),
				any(PagingSpecUrlBuilder.class));
	}

	@Test
	public void enrichPageLinksInformationNull() {
		OffsetLimitPagingSpec offsetLimitPagingSpec = new OffsetLimitPagingSpec(16L, 4L);
		PagedMetaInformation pagedMetaInformation = new DefaultPagedMetaInformation();
		pagedMetaInformation.setTotalResourceCount(7L);
		ResourceList<String> resources = new DefaultResourceList<>(Collections.emptyList(), pagedMetaInformation, null);

		when(offsetLimitPagingBehavior.isRequired(offsetLimitPagingSpec)).thenReturn(true);
		when(queryAdapter.getPagingSpec()).thenReturn(offsetLimitPagingSpec);
		when(responseResourceInformation.getPagingBehavior()).thenReturn(offsetLimitPagingBehavior);

		RepositoryAdapterUtils.enrichLinksInformation(moduleRegistry, null, resources, requestSpec);

		verify(offsetLimitPagingBehavior, times(1)).build(any(DefaultPagedLinksInformation.class), eq(resources),
				eq(queryAdapter), any(PagingSpecUrlBuilder.class));
	}

	@Test
	public void enrichSelfLinksInformationNoList() {
		LinksInformation result = RepositoryAdapterUtils.enrichLinksInformation(moduleRegistry, null, new Object(), requestSpec);

		assertThat(result, is(nullValue()));
	}

	@Test
	public void enrichSelfLinksInformationNoQuerySpec() {
		ResourceList resources = new DefaultResourceList();

		when(requestSpec.getQueryAdapter()).thenReturn(mock(QueryAdapter.class));

		LinksInformation result = RepositoryAdapterUtils.enrichLinksInformation(moduleRegistry, null, resources, requestSpec);

		assertThat(result, is(nullValue()));
	}
}
