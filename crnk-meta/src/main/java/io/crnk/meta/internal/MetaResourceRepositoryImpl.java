package io.crnk.meta.internal;

import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestContextAware;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.utils.Supplier;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.MetaLookupImpl;
import io.crnk.meta.model.MetaElement;

import java.util.ArrayList;
import java.util.Collection;

public class MetaResourceRepositoryImpl<T> extends ReadOnlyResourceRepositoryBase<T, String> implements HttpRequestContextAware {

	private final Supplier<MetaLookup> lookupSupplier;

	private HttpRequestContextProvider requestContextProvider;

	public MetaResourceRepositoryImpl(Supplier<MetaLookup> lookupSupplier, Class<T> resourceClass) {
		super(resourceClass);
		this.lookupSupplier = lookupSupplier;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T findOne(String id, QuerySpec querySpec) {
		HttpRequestContext requestContext = requestContextProvider.getRequestContext();
		QueryContext queryContext = requestContext.getQueryContext();

		MetaLookupImpl lookup = (MetaLookupImpl) lookupSupplier.get();
		MetaElement metaElement = lookup.getMetaById().get(id);
		Class<T> resourceClass = this.getResourceClass();
		if (metaElement != null && resourceClass.isInstance(metaElement)) {
			MetaElement wrappedElement = MetaUtils.adjustForRequest(lookup, metaElement, queryContext);
			if (wrappedElement != null) {
				return (T) metaElement;
			}
		}
		throw new ResourceNotFoundException(id);
	}

	@Override
	public ResourceList<T> findAll(QuerySpec querySpec) {
		MetaLookupImpl lookup = (MetaLookupImpl) lookupSupplier.get();
		Collection<T> values = filterByType(lookup.getMetaById().values());
		return querySpec.apply(values);
	}

	@SuppressWarnings("unchecked")
	private Collection<T> filterByType(Collection<MetaElement> values) {
		HttpRequestContext requestContext = requestContextProvider.getRequestContext();
		QueryContext queryContext = requestContext.getQueryContext();

		Collection<T> results = new ArrayList<>();
		Class<T> resourceClass = this.getResourceClass();
		MetaLookupImpl lookup = (MetaLookupImpl) lookupSupplier.get();
		for (MetaElement element : values) {
			if (resourceClass.isInstance(element)) {
				MetaElement wrappedElement = MetaUtils.adjustForRequest(lookup, element, queryContext);
				if (wrappedElement != null) {
					results.add((T) wrappedElement);
				}
			}
		}
		return results;
	}

	@Override
	public void setHttpRequestContextProvider(HttpRequestContextProvider requestContextProvider) {
		this.requestContextProvider = requestContextProvider;
	}
}