package io.crnk.meta;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.DocumentFilterChain;
import io.crnk.core.engine.filter.DocumentFilterContext;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldNameTransformer;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.resource.AnnotationResourceInformationBuilder;
import io.crnk.core.engine.internal.jackson.JacksonAttributeSerializationInformationProvider;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryPartAdapter;
import io.crnk.core.engine.registry.ResourceRegistryPartEvent;
import io.crnk.core.module.InitializingModule;
import io.crnk.core.module.Module;
import io.crnk.core.module.discovery.ResourceLookup;
import io.crnk.core.utils.Supplier;
import io.crnk.legacy.registry.DefaultResourceInformationBuilderContext;
import io.crnk.meta.internal.MetaRelationshipRepositoryImpl;
import io.crnk.meta.internal.MetaResourceRepositoryImpl;
import io.crnk.meta.model.*;
import io.crnk.meta.provider.MetaProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MetaModule implements Module, InitializingModule {

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

	// make protected for CDI in the future and remove deprecation

	/**
	 * @deprecated use {@link #createClientModule()} or {@link #createServerModule(MetaModuleConfig)}
	 */
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

		final Set<Class<? extends MetaElement>> metaClasses = collectMetaClasses();
		AnnotationResourceInformationBuilder informationBuilder = registerInformationBuilder();
		if (context.isServer()) {
			registerRepositories(informationBuilder, metaClasses);
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

		context.addFilter(new DocumentFilter() {
			@Override
			public Response filter(DocumentFilterContext filterRequestContext, DocumentFilterChain chain) {
				try {
					return chain.doFilter(filterRequestContext);
				} finally {
					lookupRequestLocal.remove();
				}
			}
		});
	}

	protected AnnotationResourceInformationBuilder registerInformationBuilder() {
		AnnotationResourceInformationBuilder informationBuilder = new AnnotationResourceInformationBuilder(
				new JacksonAttributeSerializationInformationProvider(),
				new ResourceFieldNameTransformer());
		informationBuilder.init(new DefaultResourceInformationBuilderContext(informationBuilder, context.getTypeParser()));
		return informationBuilder;
	}

	protected void registerRepositories(AnnotationResourceInformationBuilder informationBuilder,
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

		context.addResourceLookup(new ResourceLookup() {

			@SuppressWarnings("unchecked")
			@Override
			public Set<Class<?>> getResourceClasses() {
				return (Set) metaClasses;
			}

			@Override
			public Set<Class<?>> getResourceRepositoryClasses() {
				return Collections.emptySet();
			}
		});
		return metaClasses;
	}

	private void collectMetaClasses(Set<Class<? extends MetaElement>> metaClasses, Collection<MetaProvider> providers) {
		for (MetaProvider provider : providers) {
			metaClasses.addAll(provider.getMetaTypes());
			collectMetaClasses(metaClasses, provider.getDependencies());
		}
	}

	@Override
	public void init() {
		if (context.isServer()) {
			initRefreshListener();
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
