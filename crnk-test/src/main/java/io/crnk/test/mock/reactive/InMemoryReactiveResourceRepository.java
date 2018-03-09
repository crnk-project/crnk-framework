package io.crnk.test.mock.reactive;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.reactive.repository.ReactiveResourceRepositoryBase;
import io.crnk.test.mock.TestException;
import io.crnk.test.mock.UnknownException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryReactiveResourceRepository<T, I> extends ReactiveResourceRepositoryBase<T, I> {

	private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryReactiveResourceRepository.class);

	protected Map<I, T> resources = new ConcurrentHashMap<>();

	private long nextId = 0;

	public InMemoryReactiveResourceRepository(Class<T> clazz) {
		super(clazz);
	}


	@Override
	public Mono<T> findOne(I id, QuerySpec querySpec) {
		if ((Long) id == 10000L) {
			return Mono.error(new TestException("msg"));
		}
		if ((Long) id == 10001L) {
			return Mono.error(new UnknownException("msg"));
		}
		return super.findOne(id, querySpec);
	}


	@Override
	public Mono<ResourceList<T>> findAll(QuerySpec querySpec) {
		LOGGER.debug("findAll {}", querySpec);
		return Mono.fromCallable(() -> querySpec.apply(resources.values()));
	}

	@Override
	public Mono<T> create(T entity) {
		LOGGER.debug("create {}", entity);

		RegistryEntry entry = resourceRegistry.findEntry(getResourceClass());
		ResourceField idField = entry.getResourceInformation().getIdField();
		Long id = (Long) idField.getAccessor().getValue(entity);
		if (id == null) {
			idField.getAccessor().setValue(entity, nextId++);
		}
		return save(entity);
	}


	@Override
	public Mono<T> save(T entity) {
		LOGGER.debug("save {}", entity);
		RegistryEntry entry = resourceRegistry.findEntry(getResourceClass());
		ResourceField idField = entry.getResourceInformation().getIdField();
		I id = (I) idField.getAccessor().getValue(entity);
		PreconditionUtil.assertNotNull("no id specified", entity);
		if ((Long) id == 10000L) {
			return Mono.error(new TestException("msg"));
		}
		if ((Long) id == 10001L) {
			return Mono.error(new UnknownException("msg"));
		}
		resources.put(id, entity);
		return Mono.just(entity);
	}

	@Override
	public Mono<Boolean> delete(I id) {
		LOGGER.debug("delete {}", id);
		return Mono.fromCallable(() -> resources.remove(id) != null);
	}

	public Map<I, T> getMap() {
		return resources;
	}

	public void clear() {
		resources.clear();
	}
}
