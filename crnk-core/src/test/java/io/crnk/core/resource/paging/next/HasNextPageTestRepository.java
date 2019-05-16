package io.crnk.core.resource.paging.next;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.DefaultHasMoreResourcesMetaInformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class HasNextPageTestRepository implements ResourceRepository<HasNextPageResource, Long> {

	private static List<HasNextPageResource> HasNextPageResources = new ArrayList<>();

	public static void clear() {
		HasNextPageResources.clear();
	}

	@Override
	public Class<HasNextPageResource> getResourceClass() {
		return HasNextPageResource.class;
	}

	@Override
	public HasNextPageResource findOne(Long id, QuerySpec querySpec) {
		for (HasNextPageResource HasNextPageResource : HasNextPageResources) {
			if (HasNextPageResource.getId().equals(id)) {
				return HasNextPageResource;
			}
		}
		return null;
	}

	@Override
	public ResourceList<HasNextPageResource> findAll(QuerySpec querySpec) {
		DefaultResourceList<HasNextPageResource> list = new DefaultResourceList<>();
		list.setMeta(new DefaultHasMoreResourcesMetaInformation());
		querySpec.apply(HasNextPageResources, list);
		return list;
	}

	@Override
	public ResourceList<HasNextPageResource> findAll(Collection<Long> ids, QuerySpec querySpec) {
		DefaultResourceList<HasNextPageResource> list = new DefaultResourceList<>();
		list.setMeta(new DefaultHasMoreResourcesMetaInformation());
		querySpec.apply(HasNextPageResources, list);
		return list;
	}

	@Override
	public <S extends HasNextPageResource> S save(S entity) {
		HasNextPageResources.add(entity);
		return null;
	}

	@Override
	public void delete(Long id) {
		Iterator<HasNextPageResource> iterator = HasNextPageResources.iterator();
		while (iterator.hasNext()) {
			HasNextPageResource next = iterator.next();
			if (next.getId().equals(id)) {
				iterator.remove();
			}
		}
	}

	@Override
	public <S extends HasNextPageResource> S create(S entity) {
		return save(entity);
	}
}