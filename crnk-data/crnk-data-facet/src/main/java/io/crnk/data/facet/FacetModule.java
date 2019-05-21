package io.crnk.data.facet;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.resource.ReflectionFieldAccessor;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.module.ModuleExtensionAware;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.utils.Prioritizable;
import io.crnk.data.facet.annotation.Facet;
import io.crnk.data.facet.config.BasicFacetInformation;
import io.crnk.data.facet.config.FacetInformation;
import io.crnk.data.facet.config.FacetResourceInformation;
import io.crnk.data.facet.internal.FacetRepositoryImpl;
import io.crnk.data.facet.provider.FacetProvider;
import io.crnk.data.facet.provider.FacetProviderContext;
import io.crnk.data.facet.provider.InMemoryFacetProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FacetModule implements ModuleExtensionAware<FacetModuleExtension> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FacetModule.class);

	private final FacetModuleConfig config;

	private ModuleContext moduleContext;

	private List<FacetModuleExtension> extensions;

	private List<FacetProvider> providers;

	private boolean initialized = false;

	public FacetModule() {
		this(new FacetModuleConfig());
	}

	public FacetModule(FacetModuleConfig config) {
		this.config = config;
	}

	@Override
	public String getModuleName() {
		return "facet";
	}

	@Override
	public void setupModule(ModuleContext context) {
		moduleContext = context;
		moduleContext.addRepository(new FacetRepositoryImpl(config, moduleContext, this::setup));
	}

	@Override
	public void init() {
		// no thing to do
	}

	protected synchronized void setup() {
		if (!initialized) {
			initialized = true;

			collectInformation();

			providers = collectProviders();

			Set<FacetProvider> usedProviders = config.getResources().values().stream()
					.map(it -> setupDefaultProvider(it))
					.map(it -> it.getProvider())
					.collect(Collectors.toSet());

			FacetProviderContext providerContext = new FacetProviderContext() {
				@Override
				public ResourceRepository getRepository(String resourceType) {
					RegistryEntry entry = getEntry(resourceType);
					return entry.getResourceRepositoryFacade();
				}

				@Override
				public ResourceInformation getResourceInformation(String resourceType) {
					RegistryEntry entry = getEntry(resourceType);
					return entry.getResourceInformation();
				}

				@Override
				public TypeParser getTypeParser() {
					return moduleContext.getTypeParser();
				}

				@Override
				public RegistryEntry getEntry(String resourceType) {
					RegistryEntry entry = moduleContext.getResourceRegistry().getEntry(resourceType);
					PreconditionUtil.verify(entry != null, "resource not found: %s", resourceType);
					return entry;
				}
			};

			for (FacetProvider provider : usedProviders) {
				provider.init(providerContext);
			}
		}
	}

	private void collectInformation() {
		Collection<RegistryEntry> entries = moduleContext.getResourceRegistry().getEntries();
		for (RegistryEntry entry : entries) {
			if (entry.getRepositoryInformation().isExposed()) {
				ResourceInformation resourceInformation = entry.getResourceInformation();

				List<FacetInformation> informations = new ArrayList<>();

				for (ResourceField field : resourceInformation.getFields()) {
					ResourceFieldAccessor accessor = field.getAccessor();
					if (accessor instanceof ReflectionFieldAccessor) {
						ReflectionFieldAccessor reflectionFieldAccessor = (ReflectionFieldAccessor) accessor;
						Field classField = reflectionFieldAccessor.getField();
						if (classField == null) {
							continue;
						}
						Facet annotation = classField.getAnnotation(Facet.class);
						if (annotation != null) {
							informations.add(toInformation(field, classField, annotation));
						}
					}
				}
				if (informations.size() > 0) {
					LOGGER.debug("discovered facet for {}", resourceInformation.getResourceType());
					FacetResourceInformation facetResourceInformation = new FacetResourceInformation();
					informations.stream().forEach(it -> facetResourceInformation.addFacet(it));
					facetResourceInformation.setType(resourceInformation.getResourceType());
					config.addResource(facetResourceInformation);
				}
			}
		}
	}

	private FacetInformation toInformation(ResourceField field, Field classField, Facet annotation) {
		BasicFacetInformation nameConfig = new BasicFacetInformation();
		nameConfig.setName(field.getJsonName());
		nameConfig.setPath(PathSpec.of(field.getJsonName()));
		return nameConfig;
	}

	private FacetResourceInformation setupDefaultProvider(FacetResourceInformation facetResourceInformation) {
		RegistryEntry entry = moduleContext.getResourceRegistry().getEntry(facetResourceInformation.getType());

		FacetProvider acceptedFacetProvider = null;
		if (facetResourceInformation.getProvider() == null) {
			for (FacetProvider facetProvider : providers) {
				if (facetProvider.accepts(entry)) {
					acceptedFacetProvider = facetProvider;
					break;
				}
			}
		}
		PreconditionUtil.verify(acceptedFacetProvider != null, "no facet provider for %s", facetResourceInformation);
		facetResourceInformation.setProvider(acceptedFacetProvider);
		return facetResourceInformation;
	}

	private List<FacetProvider> collectProviders() {
		Set<FacetProvider> providers = new HashSet<>();
		providers.addAll(moduleContext.getServiceDiscovery().getInstancesByType(FacetProvider.class));
		if (extensions != null) {
			providers.addAll(extensions.stream().flatMap(it -> it.getProviders().stream()).collect(Collectors.toSet()));
		}
		providers.add(new InMemoryFacetProvider());

		ArrayList<FacetProvider> providerList = new ArrayList<>(providers);
		return Prioritizable.prioritze(providerList);
	}

	@Override
	public void setExtensions(List<FacetModuleExtension> extensions) {
		this.extensions = extensions;
	}
}
