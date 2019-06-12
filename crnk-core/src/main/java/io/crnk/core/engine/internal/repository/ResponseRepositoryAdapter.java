package io.crnk.core.engine.internal.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.exception.UnauthorizedException;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.repository.LinksRepository;
import io.crnk.core.repository.MetaRepository;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.legacy.repository.LegacyLinksRepository;
import io.crnk.legacy.repository.LegacyMetaRepository;

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


	protected ModuleRegistry moduleRegistry;

	public ResponseRepositoryAdapter(ModuleRegistry moduleRegistry) {
		this.moduleRegistry = moduleRegistry;
		PreconditionUtil.verify(moduleRegistry != null, "moduleRegistry cannot be null");
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected <T> Collection<T> filterResult(Collection<?> resources, RepositoryRequestSpec requestSpec) {
		RepositoryResultFilterChainImpl<T> chain = new RepositoryResultFilterChainImpl<>((Collection) resources);
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
	}

	protected JsonApiResponse getResponse(Object repository, Object result, RepositoryRequestSpec requestSpec) {
		if (result instanceof JsonApiResponse) {
			return (JsonApiResponse) result;
		}

		Collection<?> resources;
		boolean isCollection = result instanceof Collection;
		if (isCollection) {
			resources = (Collection<?>) result;
		} else {
			resources = Collections.singletonList(result);
		}
		Collection<?> filteredResult = filterResult(resources, requestSpec);
		MetaInformation metaInformation = getMetaInformation(repository, resources, requestSpec);
		LinksInformation linksInformation = getLinksInformation(repository, resources, requestSpec);

		Object resultEntity;
		if (isCollection) {
			resultEntity = filteredResult;
		} else {
			Iterator<?> iterator = filteredResult.iterator();
			if (iterator.hasNext()) {
				resultEntity = iterator.next();
				PreconditionUtil.verify(!iterator.hasNext(), "expected unique result, got results=%s for request=%s by repository=%s", filteredResult, requestSpec, repository);
			} else {
				resultEntity = null;
			}
		}

		return new JsonApiResponse().setEntity(resultEntity).setLinksInformation(linksInformation)
				.setMetaInformation(metaInformation);
	}

	private MetaInformation getMetaInformation(Object repository, Collection<?> resources, RepositoryRequestSpec requestSpec) {
		RepositoryMetaFilterChainImpl chain = new RepositoryMetaFilterChainImpl(repository);
		return chain.doFilter(newRepositoryFilterContext(requestSpec), resources);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private MetaInformation doGetMetaInformation(Object repository, Collection<?> resources, RepositoryRequestSpec requestSpec) {
		if (resources instanceof ResourceList) {
			ResourceList<?> resourceList = (ResourceList<?>) resources;
			return resourceList.getMeta();
		}
		QueryAdapter queryAdapter = requestSpec.getQueryAdapter();
		if (repository instanceof MetaRepository) {
			return ((MetaRepository) repository).getMetaInformation(resources, requestSpec.getResponseQuerySpec());
		} else if (repository instanceof LegacyMetaRepository) {
			return ((LegacyMetaRepository) repository).getMetaInformation(resources, requestSpec.getQueryParams());
		}
		return null;
	}

	private LinksInformation getLinksInformation(Object repository, Collection<?> resources, RepositoryRequestSpec requestSpec) {
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

	@SuppressWarnings({"unchecked", "rawtypes"})
	private LinksInformation doGetLinksInformation(Object repository, Collection<?>
			resources, RepositoryRequestSpec requestSpec) {
		if (resources instanceof ResourceList) {
			ResourceList<?> resourceList = (ResourceList<?>) resources;
			boolean createLinksInformation = resourceList instanceof DefaultResourceList;
			LinksInformation newLinksInfo = RepositoryAdapterUtils.enrichLinksInformation(moduleRegistry, resourceList.getLinks(), resources, requestSpec);
			if (createLinksInformation) {
				((DefaultResourceList) resources).setLinks(newLinksInfo);
			}
			return resourceList.getLinks();
		}

		LinksInformation linksInformation = null;
		if (repository instanceof LinksRepository) {
			linksInformation =
					((LinksRepository) repository).getLinksInformation(resources, requestSpec.getResponseQuerySpec());
		} else if (repository instanceof LegacyLinksRepository) {
			linksInformation = ((LegacyLinksRepository) repository).getLinksInformation(resources, requestSpec.getQueryParams());
		}
		// everything deprecated anyway
		return RepositoryAdapterUtils.enrichLinksInformation(moduleRegistry, linksInformation, resources, requestSpec);
	}

	class RepositoryMetaFilterChainImpl implements RepositoryMetaFilterChain {

		protected int filterIndex = 0;

		private Object repository;

		public RepositoryMetaFilterChainImpl(Object repository) {
			this.repository = repository;
		}

		@Override
		public <T> MetaInformation doFilter(RepositoryFilterContext context, Collection<T> resources) { // NOSONAR
			List<RepositoryFilter> filters = moduleRegistry.getRepositoryFilters();
			if (filterIndex == filters.size()) {
				return doGetMetaInformation(repository, resources, context.getRequest());
			} else {
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
		public <T> LinksInformation doFilter(RepositoryFilterContext context, Collection<T> resources) { // NOSONAR
			List<RepositoryFilter> filters = moduleRegistry.getRepositoryFilters();
			if (filterIndex == filters.size()) {
				return doGetLinksInformation(repository, resources, context.getRequest());
			} else {
				RepositoryFilter filter = filters.get(filterIndex);
				filterIndex++;
				return filter.filterLinks(context, resources, this);
			}
		}
	}

	class RepositoryResultFilterChainImpl<T> implements RepositoryResultFilterChain<T> {

		protected int filterIndex = 0;

		private Collection<T> result;

		public RepositoryResultFilterChainImpl(Collection<T> result) {
			this.result = result;
		}

		@Override
		public Collection<T> doFilter(RepositoryFilterContext context) { // NOSONAR
			List<RepositoryFilter> filters = moduleRegistry.getRepositoryFilters();
			if (filterIndex == filters.size()) {
				return result;
			} else {
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
			} else {
				RepositoryFilter filter = filters.get(filterIndex);
				filterIndex++;
				return filter.filterRequest(context, this);
			}
		}

		private void checkResourceAccess(RepositoryFilterContext context) {
			ResourceFilterDirectory resourceFilterDirectory = moduleRegistry.getContext().getResourceFilterDirectory();
			RepositoryRequestSpec request = context.getRequest();
			ResourceInformation resourceInformation = request.getQueryAdapter().getResourceInformation();
			QueryAdapter queryAdapter = context.getRequest().getQueryAdapter();
			QueryContext queryContext = queryAdapter.getQueryContext();
			FilterBehavior filterBehavior = resourceFilterDirectory.get(resourceInformation, request.getMethod(), queryContext);
			if (filterBehavior != FilterBehavior.NONE) {
				if(filterBehavior == FilterBehavior.UNAUTHORIZED){
					throw new UnauthorizedException(resourceInformation, request.getMethod());
				}
				throw new ForbiddenException(resourceInformation, request.getMethod());
			}

			ResourceField relationshipField = request.getRelationshipField();
			if(relationshipField != null){
				boolean canAccess = resourceFilterDirectory.canAccess(relationshipField, request.getMethod(), queryContext, false);
				PreconditionUtil.verify(canAccess, "ignoring not allowed, should not get here");
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
			} else {
				RepositoryFilter filter = filters.get(filterIndex);
				filterIndex++;
				return filter.filterBulkRequest(context, this);
			}
		}

		protected abstract Map<K, JsonApiResponse> invoke(RepositoryFilterContext context);
	}

}
