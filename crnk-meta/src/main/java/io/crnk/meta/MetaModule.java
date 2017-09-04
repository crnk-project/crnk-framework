package io.crnk.meta;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.DocumentFilterChain;
import io.crnk.core.engine.filter.DocumentFilterContext;
import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.DefaultInformationBuilder;
import io.crnk.core.engine.internal.information.resource.DefaultResourceFieldInformationProvider;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInformationProvider;
import io.crnk.core.engine.internal.jackson.JacksonResourceFieldInformationProvider;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryPartAdapter;
import io.crnk.core.engine.registry.ResourceRegistryPartEvent;
import io.crnk.core.module.ModuleExtensionAware;
import io.crnk.core.module.discovery.ResourceLookup;
import io.crnk.core.utils.Supplier;
import io.crnk.legacy.registry.DefaultResourceInformationProviderContext;
import io.crnk.meta.internal.MetaRelationshipRepositoryImpl;
import io.crnk.meta.internal.MetaResourceRepositoryImpl;
import io.crnk.meta.model.MetaArrayType;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaCollectionType;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaEnumType;
import io.crnk.meta.model.MetaInterface;
import io.crnk.meta.model.MetaKey;
import io.crnk.meta.model.MetaListType;
import io.crnk.meta.model.MetaLiteral;
import io.crnk.meta.model.MetaMapType;
import io.crnk.meta.model.MetaPrimaryKey;
import io.crnk.meta.model.MetaPrimitiveType;
import io.crnk.meta.model.MetaSetType;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.provider.MetaProvider;

public class MetaModule implements ModuleExtensionAware<MetaModuleExtension> {

	/**
	 * Current MetaLookup instance to be used.
	 */
	private MetaLookup currentLookup;

	/**
	 * MetaLookup used by the current request to ensure consistent responses even in case of concurrent
	 * modifications.
	 */
	// TODO move to request scoping once async is in palce
	private ThreadLocal<MetaLookup> lookupRequestLocal = new ThreadLocal<>();

	private ModuleContext context;

	private MetaModuleConfig config;

	private DefaultResourceInformationProvider informationBuilder;


	/**
	 * @deprecated use {@link #createClientModule()} or {@link #createServerModule(MetaModuleConfig)}
	 */
	// make protected for CDI in the future and remove deprecation
	@Deprecated
	public MetaModule() {
		this(new MetaModuleConfig());
	}

	private MetaModule(MetaModuleConfig config) {
		this.config = config;
	}


	/**
	 * @deprecated use {@link #createClientModule()} or {@link #createServerModule(MetaModuleConfig)}
	 */
	@Deprecated
	public static MetaModule create() {
		return new MetaModule();
	}

	public static MetaModule createClientModule() {
		return new MetaModule(new MetaModuleConfig());
	}

	public static MetaModule createServerModule(MetaModuleConfig config) {
		return new MetaModule(config);
	}


	@Override
	public String getModuleName() {
		return "meta";
	}

	/**
	 * @deprecated make use of {@link MetaModuleConfig} and pass to this instance upon creation
	 */
	@Deprecated
	public void putIdMapping(String packageName, String idPrefix) {
		config.addIdMapping(packageName, idPrefix);
	}

	/**
	 * @deprecated make use of {@link MetaModuleConfig} and pass to this instance upon creation
	 */
	public void putIdMapping(String packageName, Class<? extends MetaElement> type, String idPrefix) {
		config.addIdMapping(packageName, type, idPrefix);
	}

	/**
	 * @deprecated make use of {@link MetaModuleConfig} and pass to this instance upon creation
	 */
	public void addMetaProvider(MetaProvider provider) {
		config.addMetaProvider(provider);
	}

	@Override
	public void setupModule(ModuleContext context) {
		this.context = context;

		informationBuilder = registerInformationBuilder();

		if (context.isServer()) {
			context.addFilter(new DocumentFilter() {
				@Override
				public Response filter(DocumentFilterContext filterRequestContext, DocumentFilterChain chain) {
					try {
						return chain.doFilter(filterRequestContext);
					}
					finally {
						lookupRequestLocal.remove();
					}
				}
			});
		}
		else {
			context.addResourceLookup(new ResourceLookup() {

				@SuppressWarnings("unchecked")
				@Override
				public Set<Class<?>> getResourceClasses() {
					return (Set) collectMetaClasses();
				}
			});
		}
	}

	private void initRefreshListener() {
		ResourceRegistry resourceRegistry = context.getResourceRegistry();
		resourceRegistry.addListener(new ResourceRegistryPartAdapter() {
			@Override
			public void onChanged(ResourceRegistryPartEvent event) {
				currentLookup = null;
			}
		});
	}

	protected DefaultResourceInformationProvider registerInformationBuilder() {
		InformationBuilder informationBuilder = new DefaultInformationBuilder(context.getTypeParser());
		DefaultResourceInformationProvider informationProvider = new DefaultResourceInformationProvider(
				new DefaultResourceFieldInformationProvider(),
				new JacksonResourceFieldInformationProvider());
		informationProvider.init(new DefaultResourceInformationProviderContext(informationProvider, informationBuilder,
				context.getTypeParser(), null) {
			@Override
			public ObjectMapper getObjectMapper() {
				return context.getObjectMapper();
			}
		});
		return informationProvider;
	}

	protected void registerRepositories(DefaultResourceInformationProvider informationBuilder,
			Set<Class<? extends MetaElement>> metaClasses) {

		Supplier<MetaLookup> lookupSupplier = new Supplier<MetaLookup>() {
			@Override
			public MetaLookup get() {
				return getLookup();
			}
		};

		for (Class<? extends MetaElement> metaClass : metaClasses) {
			context.addRepository(new MetaResourceRepositoryImpl<>(lookupSupplier, metaClass));

			HashSet<Class<? extends MetaElement>> targetResourceClasses = new HashSet<>();
			ResourceInformation information = informationBuilder.build(metaClass);
			for (ResourceField relationshipField : information.getRelationshipFields()) {
				if (!MetaElement.class.isAssignableFrom(relationshipField.getElementType())) {
					throw new IllegalStateException("only MetaElement relations supported, got " + relationshipField);
				}
				targetResourceClasses.add((Class<? extends MetaElement>) relationshipField.getElementType());
			}
			for (Class<? extends MetaElement> targetResourceClass : targetResourceClasses) {
				context.addRepository(new MetaRelationshipRepositoryImpl(lookupSupplier, metaClass, targetResourceClass));
			}
		}
	}

	protected Set<Class<? extends MetaElement>> collectMetaClasses() {
		final Set<Class<? extends MetaElement>> metaClasses = new HashSet<>();
		metaClasses.add(MetaArrayType.class);
		metaClasses.add(MetaAttribute.class);
		metaClasses.add(MetaCollectionType.class);
		metaClasses.add(MetaDataObject.class);
		metaClasses.add(MetaElement.class);
		metaClasses.add(MetaEnumType.class);
		metaClasses.add(MetaInterface.class);
		metaClasses.add(MetaKey.class);
		metaClasses.add(MetaListType.class);
		metaClasses.add(MetaLiteral.class);
		metaClasses.add(MetaMapType.class);
		metaClasses.add(MetaPrimaryKey.class);
		metaClasses.add(MetaPrimitiveType.class);
		metaClasses.add(MetaSetType.class);
		metaClasses.add(MetaType.class);

		collectMetaClasses(metaClasses, config.getProviders());
		return metaClasses;
	}

	private void collectMetaClasses(Set<Class<? extends MetaElement>> metaClasses, Collection<MetaProvider> providers) {
		for (MetaProvider provider : providers) {
			metaClasses.addAll(provider.getMetaTypes());
			collectMetaClasses(metaClasses, provider.getDependencies());
		}
	}

	@Override
	public void setExtensions(List<MetaModuleExtension> extensions) {
		for (MetaModuleExtension extension : extensions) {
			for (MetaProvider provider : extension.getProviders()) {
				config.addMetaProvider(provider);
			}
		}
	}

	@Override
	public void init() {
		final Set<Class<? extends MetaElement>> metaClasses = collectMetaClasses();
		if (context.isServer()) {
			initRefreshListener();
			registerRepositories(informationBuilder, metaClasses);
		}
	}

	public synchronized MetaLookup getLookup() {
		if (currentLookup == null) {
			initLookup();
		}

		MetaLookup requestLookup = lookupRequestLocal.get();
		if (requestLookup == null) {
			requestLookup = currentLookup;
			lookupRequestLocal.set(requestLookup);
		}
		return requestLookup;
	}

	private void initLookup() {
		MetaLookup lookup = new MetaLookup();
		config.apply(lookup);
		lookup.setModuleContext(context);
		lookup.initialize();
		currentLookup = lookup;
	}

	protected ThreadLocal<MetaLookup> getLookupRequestLocal() {
		return lookupRequestLocal;
	}

	protected void reset() {
		currentLookup = null;
	}

}
