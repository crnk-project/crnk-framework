package io.crnk.reactive.model;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;

import java.util.HashMap;
import java.util.Map;

import io.crnk.reactive.repository.ReactiveResourceRepositoryBase;
import reactor.core.publisher.Mono;

public class InMemoryReactiveResourceRepository<T, I> extends ReactiveResourceRepositoryBase<T, I> {


	private Map<I, T> resources = new HashMap<>();

	private long nextId = 0;

	public InMemoryReactiveResourceRepository(Class<T> clazz) {
		super(clazz);
	}

	@Override
	public Mono<ResourceList<T>> findAll(QuerySpec querySpec) {
		return Mono.fromCallable(() -> querySpec.apply(resources.values()));
	}

	@Override
	public Mono<T> create(T entity) {
		Object id = PropertyUtils.getProperty(entity, "id");
		if (id == null) {
			PropertyUtils.setProperty(entity, "id", nextId++);
		}

		return save(entity);
	}

	@Override
	public Mono<T> save(T entity) {
		if (entity instanceof ReactiveTask && "badName".equals(((ReactiveTask) entity).getName())) {
			return Mono.error(new BadRequestException("badName not a valid name"));
		}
		RegistryEntry entry = resourceRegistry.findEntry(getResourceClass());
		ResourceField idField = entry.getResourceInformation().getIdField();

		I id = (I) idField.getAccessor().getValue(entity);
		PreconditionUtil.assertNotNull("no id specified", entity);

		resources.put(id, entity);
		return Mono.just(entity);
	}

	@Override
	public Mono<Boolean> delete(I id) {
		return Mono.fromCallable(() -> resources.remove(id) != null);
	}

	public Map<I, T> getMap() {
		return resources;
	}

	public void clear() {
		resources.clear();
	}
}
