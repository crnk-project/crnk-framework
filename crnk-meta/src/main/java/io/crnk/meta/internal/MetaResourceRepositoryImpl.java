package io.crnk.meta.internal;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.utils.Supplier;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaElement;

import java.util.ArrayList;
import java.util.Collection;

public class MetaResourceRepositoryImpl<T> extends ResourceRepositoryBase<T, String> {

	private final Supplier<MetaLookup> lookupSupplier;

	public MetaResourceRepositoryImpl(Supplier<MetaLookup> lookupSupplier, Class<T> resourceClass) {
		super(resourceClass);
		this.lookupSupplier = lookupSupplier;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T findOne(String id, QuerySpec querySpec) {
		MetaLookup lookup = lookupSupplier.get();
		MetaElement metaElement = lookup.getMetaById().get(id);
		Class<T> resourceClass = this.getResourceClass();
		if (metaElement != null && resourceClass.isInstance(metaElement)) {
			MetaElement wrappedElement = MetaUtils.adjustForRequest(lookup, metaElement);
			if (wrappedElement != null) {
				return (T) metaElement;
			}
		}
		throw new ResourceNotFoundException(id);
	}

	@Override
	public ResourceList<T> findAll(QuerySpec querySpec) {
		MetaLookup lookup = lookupSupplier.get();
		Collection<T> values = filterByType(lookup.getMetaById().values());
		return querySpec.apply(values);
	}

	@SuppressWarnings("unchecked")
	private Collection<T> filterByType(Collection<MetaElement> values) {
		Collection<T> results = new ArrayList<>();
		Class<T> resourceClass = this.getResourceClass();
		MetaLookup lookup = lookupSupplier.get();
		for (MetaElement element : values) {
			if (resourceClass.isInstance(element)) {
				MetaElement wrappedElement = MetaUtils.adjustForRequest(lookup, element);
				if (wrappedElement != null) {
					results.add((T) wrappedElement);
				}
			}
		}
		return results;
	}

}