package io.crnk.gen.runtime.reflections;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.internal.registry.DefaultRegistryEntryBuilder;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.module.discovery.EmptyServiceDiscovery;
import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.gen.runtime.GeneratorTrigger;
import io.crnk.gen.typescript.RuntimeMetaResolver;
import io.crnk.gen.typescript.TSGeneratorConfig;
import io.crnk.jpa.internal.JpaResourceInformationProvider;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import net.jodah.typetools.TypeResolver;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReflectionsMetaResolver implements RuntimeMetaResolver {

	@Override
	public void run(GeneratorTrigger context, ClassLoader classLoader) {
		try {
			DefaultRegistryEntryBuilder.WARN_MISSING_RELATIONSHIP_REPOSITORIES = false;

			TSGeneratorConfig config = context.getConfig();
			List<String> resourcePackages = config.getResourcePackages();

			ConfigurationBuilder builder = new ConfigurationBuilder();
			FilterBuilder filter = new FilterBuilder();
			for (String resourceSearchPackage : resourcePackages) {
				builder = builder.addUrls(ClasspathHelper.forPackage(resourceSearchPackage));
				filter.includePackage(resourceSearchPackage);
			}
			filter.includePackage(ResourceRepositoryV2.class.getPackage().getName());
			builder = builder.filterInputsBy(filter);
			builder = builder.addUrls(ClasspathHelper.forClass(ResourceRepositoryV2.class));
			builder = builder.setScanners(new SubTypesScanner(false), new TypeAnnotationsScanner());
			Reflections reflections = new Reflections(builder);

			Set<Class<?>> resourceClasses = reflections.getTypesAnnotatedWith(JsonApiResource.class);
			if (resourceClasses.isEmpty()) {
				throw new IllegalStateException("no classes found annotated with @JsonApiResource");
			}

			Set<Class<? extends ResourceRepositoryV2>> repositoryClasses = reflections.getSubTypesOf(ResourceRepositoryV2.class);

			Map<Class, Class> resourceRepositoryMap = new HashMap<>();
			for (Class repositoryClass : repositoryClasses) {
				if (repositoryClass.isInterface()) {
					Class<?>[] typeArgs = TypeResolver.resolveRawArguments(ResourceRepositoryV2.class, repositoryClass);
					if (typeArgs != null) {
						Class<?> resourceClass = typeArgs[0];
						resourceRepositoryMap.put(resourceClass, repositoryClass);
					}
				}
			}

			SimpleModule reflectionsModule = new SimpleModule("reflections");
			for (Class resourceClass : resourceClasses) {
				JsonApiResource resourceAnnotation = (JsonApiResource) resourceClass.getAnnotation(JsonApiResource.class);
				JsonApiResource superAnnotation = (JsonApiResource) resourceClass.getSuperclass().getAnnotation(JsonApiResource.class);
				String superPath = superAnnotation != null ? superAnnotation.resourcePath().isEmpty() ? superAnnotation.type() : superAnnotation.resourcePath() : null;
				if (superPath != null && resourceAnnotation.resourcePath().equals(superPath)) {
					// repository shared path with super type, meaning that it does not have a repository on its own
					continue;
				}

				Class repositoryInterface = resourceRepositoryMap.get(resourceClass);
				if (repositoryInterface != null) {
					// repository class provides further ResourceList meta data
					InvocationHandler handler = (proxy, method, args) -> {
						if (method.getName().equals("getResourceClass")) {
							return resourceClass;
						}
						throw new UnsupportedOperationException();
					};
					Object repository = Proxy.newProxyInstance(classLoader, new Class[]{repositoryInterface}, handler);
					reflectionsModule.addRepository(repository);
				} else {
					reflectionsModule.addRepository(new InMemoryResourceRepository<>(resourceClass));
				}
			}

			MetaModuleConfig metaConfig = new MetaModuleConfig();
			metaConfig.addMetaProvider(new ResourceMetaProvider());
			MetaModule metaModule = MetaModule.createServerModule(metaConfig);

			SimpleModule jpaInfoModule = new SimpleModule("jpa.info");
			jpaInfoModule.addResourceInformationProvider(new JpaResourceInformationProvider(new NullPropertiesProvider()));

			CrnkBoot boot = new CrnkBoot();
			boot.setDefaultPageLimit(10L);
			boot.addModule(jpaInfoModule);
			boot.addModule(reflectionsModule);
			boot.addModule(metaModule);
			boot.setServiceDiscovery(new EmptyServiceDiscovery());
			boot.boot();

			MetaLookup lookup = metaModule.getLookup();
			context.generate(lookup);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
