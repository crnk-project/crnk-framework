package io.crnk.core.engine.internal.repository;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.RepositoryBulkRequestFilterChain;
import io.crnk.core.engine.filter.RepositoryFilter;
import io.crnk.core.engine.filter.RepositoryFilterContext;
import io.crnk.core.engine.filter.RepositoryLinksFilterChain;
import io.crnk.core.engine.filter.RepositoryMetaFilterChain;
import io.crnk.core.engine.filter.RepositoryRequestFilterChain;
import io.crnk.core.engine.filter.RepositoryResultFilterChain;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.queryspec.pagingspec.PagingSpecUrlBuilder;
import io.crnk.core.repository.LinksRepositoryV2;
import io.crnk.core.repository.MetaRepositoryV2;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.links.DefaultPagedLinksInformation;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.links.PagedLinksInformation;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.legacy.internal.AnnotatedRepositoryAdapter;
import io.crnk.legacy.repository.LinksRepository;
import io.crnk.legacy.repository.MetaRepository;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The adapter is used to create a common layer between controllers and
 * repositories. Every repository can return either a resource object or a
 * {@link JsonApiResponse} response which should be returned by a controller.
 * Ok, the last sentence is not 100% true since interface based repositories can
 * return only resources, but who's using it anyway?
 * <p>
 * The methods need to know if a repository is interface- or annotation-based
 * since repository methods have different signatures.
 */
public abstract class ResponseRepositoryAdapter {

	protected ResourceInformation resourceInformation;

	protected ModuleRegistry moduleRegistry;

	public ResponseRepositoryAdapter(ResourceInformation resourceInformation, ModuleRegistry moduleRegistry) {
		this.moduleRegistry = moduleRegistry;
		this.resourceInformation = resourceInformation;
		PreconditionUtil.assertNotNull("moduleRegistry cannot be null", moduleRegistry);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <T> Iterable<T> filterResult(Iterable<?> resources, RepositoryRequestSpec requestSpec) {
		RepositoryResultFilterChainImpl<T> chain = new RepositoryResultFilterChainImpl<>((Iterable) resources);
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
	}

	protected JsonApiResponse getResponse(Object repository, Object result, RepositoryRequestSpec requestSpec) {
		if (result instanceof JsonApiResponse) {
			return (JsonApiResponse) result;
		}

		Iterable<?> resources;
		boolean isCollection = result instanceof Iterable;
		if (isCollection) {
			resources = (Iterable<?>) result;
		}
		else {
			resources = Collections.singletonList(result);
		}
		Iterable<?> filteredResult = filterResult(resources, requestSpec);
		MetaInformation metaInformation = getMetaInformation(repository, resources, requestSpec);
		LinksInformation linksInformation = getLinksInformation(repository, resources, requestSpec);

		Object resultEntity;
		if (isCollection) {
			resultEntity = filteredResult;
		}
		else {
			Iterator<?> iterator = filteredResult.iterator();
			if (iterator.hasNext()) {
				resultEntity = iterator.next();
				PreconditionUtil.assertFalse("expected unique result", iterator.hasNext());
			}
			else {
				resultEntity = null;
			}
		}

		return new JsonApiResponse().setEntity(resultEntity).setLinksInformation(linksInformation)
				.setMetaInformation(metaInformation);
	}

	private MetaInformation getMetaInformation(Object repository, Iterable<?> resources, RepositoryRequestSpec requestSpec) {
		RepositoryMetaFilterChainImpl chain = new RepositoryMetaFilterChainImpl(repository);
		return chain.doFilter(newRepositoryFilterContext(requestSpec), resources);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private MetaInformation doGetMetaInformation(Object repository, Iterable<?> resources, RepositoryRequestSpec requestSpec) {
		if (resources instanceof ResourceList) {
			ResourceList<?> resourceList = (ResourceList<?>) resources;
			return resourceList.getMeta();
		}
		QueryAdapter queryAdapter = requestSpec.getQueryAdapter();
		if (repository instanceof AnnotatedRepositoryAdapter) {
			if (((AnnotatedRepositoryAdapter) repository).metaRepositoryAvailable()) {
				return ((AnnotatedRepositoryAdapter) repository).getMetaInformation(resources, queryAdapter);
			}
		}
		else if (repository instanceof MetaRepositoryV2) {
			return ((MetaRepositoryV2) repository).getMetaInformation(resources, requestSpec.getResponseQuerySpec());
		}
		else if (repository instanceof MetaRepository) {
			return ((MetaRepository) repository).getMetaInformation(resources, requestSpec.getQueryParams());
		}
		return null;
	}

	private LinksInformation getLinksInformation(Object repository, Iterable<?> resources, RepositoryRequestSpec requestSpec) {
		RepositoryLinksFilterChainImpl chain = new RepositoryLinksFilterChainImpl(repository);
		return chain.doFilter(newRepositoryFilterContext(requestSpec), resources);
	}

	protected RepositoryFilterContext newRepositoryFilterContext(final RepositoryRequestSpec requestSpec) {
		return new RepositoryFilterContext() {

			@Override
			public RepositoryRequestSpec getRequest() {
				return requestSpec;
			}
		};
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private LinksInformation doGetLinksInformation(Object repository, Iterable<?>
			resources, RepositoryRequestSpec requestSpec) {
		if (resources instanceof ResourceList) {
			ResourceList<?> resourceList = (ResourceList<?>) resources;
			boolean createLinksInformation = resourceList instanceof DefaultResourceList;
			LinksInformation newLinksInfo = enrichLinksInformation(resourceList.getLinks(), resources, requestSpec);
			if (createLinksInformation) {
				((DefaultResourceList) resources).setLinks(newLinksInfo);
			}
			return resourceList.getLinks();
		}

		LinksInformation linksInformation = null;
		if (repository instanceof AnnotatedRepositoryAdapter) {
			if (((AnnotatedRepositoryAdapter) repository).linksRepositoryAvailable()) {
				linksInformation = ((LinksRepository) repository).getLinksInformation(resources, requestSpec.getQueryParams());
			}
		}
		else if (repository instanceof LinksRepositoryV2) {
			linksInformation =
					((LinksRepositoryV2) repository).getLinksInformation(resources, requestSpec.getResponseQuerySpec());
		}
		else if (repository instanceof LinksRepository) {
			linksInformation = ((LinksRepository) repository).getLinksInformation(resources, requestSpec.getQueryParams());
		}
		// everything deprecated anyway
		return enrichLinksInformation(linksInformation, resources, requestSpec);
	}

	private LinksInformation enrichLinksInformation(LinksInformation linksInformation, Iterable<?> resources,
			RepositoryRequestSpec requestSpec) {
		QueryAdapter queryAdapter = requestSpec.getQueryAdapter();
		LinksInformation enrichedLinksInformation = linksInformation;
		if (queryAdapter instanceof QuerySpecAdapter && resources instanceof ResourceList && queryAdapter.getPagingSpec().isRequired()) {
			enrichedLinksInformation = enrichPageLinksInformation(enrichedLinksInformation, (ResourceList<?>) resources, queryAdapter,
					requestSpec);
		}
		return enrichedLinksInformation;
	}

	private LinksInformation enrichPageLinksInformation(LinksInformation linksInformation, ResourceList<?> resources,
			QueryAdapter queryAdapter, RepositoryRequestSpec requestSpec) {
		if (linksInformation == null) {
			// use default implementation if no link information
			// provided by resource
			linksInformation = new DefaultPagedLinksInformation();
		}
		if (linksInformation instanceof PagedLinksInformation) {
			PagingSpecUrlBuilder urlBuilder = new PagingSpecUrlBuilder(moduleRegistry.getResourceRegistry(), requestSpec);
			queryAdapter.getPagingSpec().build((PagedLinksInformation) linksInformation, resources,
					queryAdapter, urlBuilder);
		}
		return linksInformation;
	}

	class RepositoryMetaFilterChainImpl implements RepositoryMetaFilterChain {

		protected int filterIndex = 0;

		private Object repository;

		public RepositoryMetaFilterChainImpl(Object repository) {
			this.repository = repository;
		}

		@Override
		public <T> MetaInformation doFilter(RepositoryFilterContext context, Iterable<T> resources) { // NOSONAR
			List<RepositoryFilter> filters = moduleRegistry.getRepositoryFilters();
			if (filterIndex == filters.size()) {
				return doGetMetaInformation(repository, resources, context.getRequest());
			}
			else {
				RepositoryFilter filter = filters.get(filterIndex);
				filterIndex++;
				return filter.filterMeta(context, resources, this);
			}
		}
	}

	class RepositoryLinksFilterChainImpl implements RepositoryLinksFilterChain {

		protected int filterIndex = 0;

		private Object repository;

		public RepositoryLinksFilterChainImpl(Object repository) {
			this.repository = repository;
		}

		@Override
		public <T> LinksInformation doFilter(RepositoryFilterContext context, Iterable<T> resources) { // NOSONAR
			List<RepositoryFilter> filters = moduleRegistry.getRepositoryFilters();
			if (filterIndex == filters.size()) {
				return doGetLinksInformation(repository, resources, context.getRequest());
			}
			else {
				RepositoryFilter filter = filters.get(filterIndex);
				filterIndex++;
				return filter.filterLinks(context, resources, this);
			}
		}
	}

	class RepositoryResultFilterChainImpl<T> implements RepositoryResultFilterChain<T> {

		protected int filterIndex = 0;

		private Iterable<T> result;

		public RepositoryResultFilterChainImpl(Iterable<T> result) {
			this.result = result;
		}

		@Override
		public Iterable<T> doFilter(RepositoryFilterContext context) { // NOSONAR
			List<RepositoryFilter> filters = moduleRegistry.getRepositoryFilters();
			if (filterIndex == filters.size()) {
				return result;
			}
			else {
				RepositoryFilter filter = filters.get(filterIndex);
				filterIndex++;
				return filter.filterResult(context, this);
			}
		}
	}

	protected abstract class RepositoryRequestFilterChainImpl implements RepositoryRequestFilterChain {

		protected int filterIndex = 0;

		@Override
		public JsonApiResponse doFilter(RepositoryFilterContext context) {
			if (filterIndex == 0) {
				checkResourceAccess(context);
			}
			List<RepositoryFilter> filters = moduleRegistry.getRepositoryFilters();
			if (filterIndex == filters.size()) {
				return invoke(context);
			}
			else {
				RepositoryFilter filter = filters.get(filterIndex);
				filterIndex++;
				return filter.filterRequest(context, this);
			}
		}

		private void checkResourceAccess(RepositoryFilterContext context) {
			ResourceFilterDirectory resourceFilterDirectory = moduleRegistry.getContext().getResourceFilterDirectory();
			RepositoryRequestSpec request = context.getRequest();
			ResourceInformation resourceInformation = request.getQueryAdapter().getResourceInformation();
			FilterBehavior filterBehavior = resourceFilterDirectory.get(resourceInformation, request.getMethod());
			if (filterBehavior != FilterBehavior.NONE) {
				String msg = "not allowed to access " + resourceInformation.getResourceType();
				throw new ForbiddenException(msg);
			}
		}

		protected abstract JsonApiResponse invoke(RepositoryFilterContext context);
	}

	protected abstract class RepositoryBulkRequestFilterChainImpl<K> implements RepositoryBulkRequestFilterChain<K> {

		protected int filterIndex = 0;

		@Override
		public Map<K, JsonApiResponse> doFilter(RepositoryFilterContext context) {
			List<RepositoryFilter> filters = moduleRegistry.getRepositoryFilters();
			if (filterIndex == filters.size()) {
				return invoke(context);
			}
			else {
				RepositoryFilter filter = filters.get(filterIndex);
				filterIndex++;
				return filter.filterBulkRequest(context, this);
			}
		}

		protected abstract Map<K, JsonApiResponse> invoke(RepositoryFilterContext context);
	}

}
