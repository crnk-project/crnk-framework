package io.crnk.meta;

import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.module.Module.ModuleContext;
import io.crnk.core.utils.Optional;
import io.crnk.meta.internal.BaseMetaPartition;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.provider.MetaFilter;
import io.crnk.meta.provider.MetaPartition;
import io.crnk.meta.provider.MetaPartitionContext;
import io.crnk.meta.provider.MetaProvider;
import io.crnk.meta.provider.MetaProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MetaLookup {

	private static final Logger LOGGER = LoggerFactory.getLogger(MetaLookup.class);


	private LinkedList<MetaElement> initializationQueue = new LinkedList<>();

	private boolean discovering = false;

	private BaseMetaPartition basePartition;

	private boolean discovered;

	private ModuleContext moduleContext;

	private List<MetaPartition> partitions = new ArrayList<>();

	private ConcurrentHashMap<String, MetaElement> idElementMap = new ConcurrentHashMap<>();

	private List<MetaFilter> filters = new CopyOnWriteArrayList<>();

	private List<MetaProvider> providers = new CopyOnWriteArrayList<>();

	private Set<Class<? extends MetaElement>> metaTypes = new HashSet<>();

	public MetaLookup() {
		basePartition = new BaseMetaPartition();
		addPartition(basePartition);
	}

	public void addProvider(MetaProvider provider) {
		providers.add(provider);

		provider.init(new MetaProviderContext() {
			@Override
			public ModuleContext getModuleContext() {
				return moduleContext;
			}

			@Override
			public Optional<MetaElement> getMetaElement(String id) {
				return Optional.ofNullable(idElementMap.get(id));
			}

			@Override
			public void checkInitialized() {
				MetaLookup.this.checkInitialized();
			}

			@Override
			public <T> T runDiscovery(Callable<T> callable) {
				return discover(callable);
			}
		});

		for (MetaFilter filter : provider.getFilters()) {
			addFilter(filter);
		}
		for (MetaPartition partition : provider.getPartitions()) {
			addPartition(partition);
		}
		metaTypes.addAll(provider.getMetaTypes());
	}

	private void addPartition(MetaPartition partition) {
		partitions.add(partition);
		partition.init(new MetaPartitionContext() {
			@Override
			public void addElement(MetaElement element) {
				MetaLookup.this.add(element);
			}

			@Override
			public ModuleContext getModuleContext() {
				return moduleContext;
			}

			@Override
			public Optional<MetaElement> getMetaElement(String id) {
				return Optional.ofNullable(idElementMap.get(id));
			}

			@Override
			public MetaPartition getBasePartition() {
				return basePartition;
			}

			@Override
			public <T> T runDiscovery(Callable<T> callable) {
				return discover(callable);
			}
		});
	}


	private void addFilter(MetaFilter filter) {
		filters.add(filter);
	}

	public void setModuleContext(ModuleContext moduleContext) {
		this.moduleContext = moduleContext;
	}

	public void registerPrimitiveType(Class<?> clazz) {
		basePartition.registerPrimitiveType(clazz);
	}

	public Map<String, MetaElement> getMetaById() {
		checkInitialized();
		return Collections.unmodifiableMap(idElementMap);
	}

	public void add(MetaElement element) {
		PreconditionUtil.verify(discovering, "no discovering");
		PreconditionUtil.verify(element.getName() != null, "no name provided for %s", element);


		if (!element.hasId() && element.getParent() != null) {
			element.setId(element.getParent().getId() + "." + element.getName());
		}

		PreconditionUtil.verify(!idElementMap.contains(element.getId()), "element with id=%d already exists", element.getId());

		//	if (idElementMap.get(element.getId()) != element) {
		LOGGER.trace("trace {} of type {}", element.getId(), element.getClass().getSimpleName());

		// queue for initialization
		initializationQueue.add(element);
		idElementMap.put(element.getId(), element);

		// add children recursively
		for (MetaElement child : element.getChildren()) {
			add(child);
		}
		//}
	}

	private void checkInitialized() {
		if (!discovered && !discovering) {
			initialize();
		}
	}

	public void initialize() {
		discover(new Callable<Object>() {

			@Override
			public Object call() {
				if (!discovered) {
					for (MetaPartition provider : partitions) {
						provider.discoverElements();
					}
					discovered = true;
				}
				return null;
			}
		});
	}

	private <T> T discover(Callable<T> callable) {
		if (discovering) {
			try {
				return callable.call();
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				LOGGER.debug("discovery failed", e);
				throw new IllegalStateException(e);
			}
		}
		try {
			LOGGER.trace("discovery started");
			discovering = true;

			T result = callable.call();

			while (!initializationQueue.isEmpty()) {
				MetaElement element = initializationQueue.pollFirst();
				// initialize from roots down to decendants.
				if (element.getParent() == null) {
					initialize(element);
				}
			}
			LOGGER.trace("discovery completed");
			return result;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.trace("discovery failed", e);
			throw new IllegalStateException(e);
		} finally {
			discovering = false;
		}
	}

	private void initialize(MetaElement element) {
		LOGGER.trace("discovering {}", element.getId());

		for (MetaFilter filter : filters) {
			filter.onInitializing(element);
		}

		for (MetaElement child : element.getChildren()) {
			initialize(child);
		}

		for (MetaFilter filter : filters) {
			filter.onInitialized(element);
		}
		LOGGER.trace("added {}", element.getId());
	}


	public <T extends MetaElement> List<T> findElements(Class<T> metaType) {
		List<T> list = new ArrayList<>();
		for (MetaElement element : idElementMap.values()) {
			if (metaType.isInstance(element)) {
				list.add((T) element);
			}
		}
		return list;
	}

	public void putElement(MetaElement element) {
		this.idElementMap.put(element.getId(), element);
	}

	protected ModuleContext getContext() {
		return moduleContext;
	}

	public List<MetaFilter> getFilters() {
		return filters;
	}

	public <T extends MetaPartition> T getPartition(Class clazz) {
		for (MetaPartition partition : partitions) {
			if (clazz.isInstance(partition)) {
				return (T) partition;
			}
		}
		throw new IllegalStateException();
	}
}
