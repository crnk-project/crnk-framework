package io.crnk.meta;

import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.module.Module.ModuleContext;
import io.crnk.core.utils.Optional;
import io.crnk.meta.internal.BaseMetaPartition;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.provider.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
		PreconditionUtil.assertTrue("no discovering", discovering);
		PreconditionUtil.assertNotNull("no name provided", element.getName());


		if (!element.hasId() && element.getParent() != null) {
			element.setId(element.getParent().getId() + "." + element.getName());
		}

		PreconditionUtil.assertNull("already exists", idElementMap.get(element.getId()));

		//	if (idElementMap.get(element.getId()) != element) {
		LOGGER.debug("add {} of type {}", element.getId(), element.getClass().getSimpleName());

		// queue for initialization
		initializationQueue.add(element);

		MetaElement currentElement = idElementMap.get(element.getId());
		PreconditionUtil.assertNull(element.getId(), currentElement);
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
			public Object call() throws Exception {
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
		LOGGER.debug("discovery started");
		discovering = true;
		try {
			T result = callable.call();

			while (!initializationQueue.isEmpty()) {
				MetaElement element = initializationQueue.pollFirst();
				// initialize from roots down to decendants.
				if (element.getParent() == null) {
					initialize(element);
				}
			}
			return result;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		} finally {
			LOGGER.debug("discovery completed");
			discovering = false;
		}
	}

	private void initialize(MetaElement element) {
		LOGGER.debug("discovering {}", element.getId());

		for (MetaFilter filter : filters) {
			filter.onInitializing(element);
		}

		for (MetaElement child : element.getChildren()) {
			initialize(child);
		}

		for (MetaFilter filter : filters) {
			filter.onInitialized(element);
		}
		LOGGER.debug("added {}", element.getId());
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
