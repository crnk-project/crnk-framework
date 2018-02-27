package io.crnk.rs;

import java.io.Serializable;
import java.util.Collection;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.module.Module;
import io.crnk.core.queryspec.QuerySpecDeserializer;
import io.crnk.legacy.locator.JsonServiceLocator;
import io.crnk.legacy.queryParams.QueryParamsBuilder;
import io.crnk.rs.internal.JaxrsModule;
import io.crnk.rs.internal.legacy.RequestContextParameterProviderRegistry;
import io.crnk.rs.internal.legacy.RequestContextParameterProviderRegistryBuilder;

/**
 * Basic Crnk feature that initializes core classes and provides a starting point to use the framework in
 * another projects.
 * <p>
 * This feature has NO {@link Provider} annotation, thus it require to provide an instance of  {@link ObjectMapper} and
 * {@link JsonServiceLocator} to provide instances of resources.
 */
@ConstrainedTo(RuntimeType.SERVER)
public class CrnkFeature implements Feature {

	private CrnkBoot boot = new CrnkBoot();

	private RequestContextParameterProviderRegistry parameterProviderRegistry;

	@Context
	protected SecurityContext securityContext;

	private boolean securityEnabled = true;

	public CrnkFeature() {
		// nothing to do
	}

	public CrnkFeature(ObjectMapper objectMapper, QueryParamsBuilder queryParamsBuilder,
			JsonServiceLocator jsonServiceLocator) {
		boot.setObjectMapper(objectMapper);
		boot.setQueryParamsBuilds(queryParamsBuilder);
		boot.setServiceLocator(jsonServiceLocator);
	}

	public CrnkFeature(ObjectMapper objectMapper, QuerySpecDeserializer querySpecDeserializer,
			JsonServiceLocator jsonServiceLocator) {
		boot.setObjectMapper(objectMapper);
		boot.setQuerySpecDeserializer(querySpecDeserializer);
		boot.setServiceLocator(jsonServiceLocator);
	}

	/**
	 * Sets a custom ServiceUrlProvider.
	 */
	public void setServiceUrlProvider(ServiceUrlProvider serviceUrlProvider) {
		boot.setServiceUrlProvider(serviceUrlProvider);
	}

	public void addModule(Module module) {
		boot.addModule(module);
	}

	@Override
	public boolean configure(final FeatureContext context) {
		boot.setPropertiesProvider(createPropertiesProvider(context));
		if (securityEnabled) {
			boot.addModule(new JaxrsModule(securityContext));
		}
		boot.boot();

		parameterProviderRegistry = buildParameterProviderRegistry();

		CrnkFilter crnkFilter = createCrnkFilter();
		context.register(crnkFilter);

		registerActionRepositories(context, boot);

		return true;
	}

	protected PropertiesProvider createPropertiesProvider(FeatureContext context) {
		return key -> (String) context.getConfiguration().getProperty(key);
	}

	public void setSecurityEnabled(boolean securityEnabled) {
		this.securityEnabled = securityEnabled;
	}

	/**
	 * All repositories with JAX-RS action need to be registered with JAX-RS as singletons.
	 *
	 * @param context of jaxrs
	 * @param boot of crnk
	 */
	private void registerActionRepositories(FeatureContext context, CrnkBoot boot) {
		ResourceRegistry resourceRegistry = boot.getResourceRegistry();
		Collection<RegistryEntry> registryEntries = resourceRegistry.getResources();
		for (RegistryEntry registryEntry : registryEntries) {
			ResourceRepositoryInformation repositoryInformation = registryEntry.getRepositoryInformation();
			if (repositoryInformation != null && !repositoryInformation.getActions().isEmpty()) {
				ResourceRepositoryAdapter<?, Serializable> repositoryAdapter = registryEntry.getResourceRepository(null);
				Object resourceRepository = repositoryAdapter.getResourceRepository();
				context.register(resourceRepository);
			}
		}
	}

	private RequestContextParameterProviderRegistry buildParameterProviderRegistry() {
		RequestContextParameterProviderRegistryBuilder builder = new RequestContextParameterProviderRegistryBuilder();
		return builder.build(boot.getServiceDiscovery());
	}

	protected CrnkFilter createCrnkFilter() {
		return new CrnkFilter(this);
	}

	public ObjectMapper getObjectMapper() {
		return boot.getObjectMapper();
	}

	public void setDefaultPageLimit(Long defaultPageLimit) {
		boot.setDefaultPageLimit(defaultPageLimit);
	}

	public QuerySpecDeserializer getQuerySpecDeserializer() {
		return boot.getQuerySpecDeserializer();
	}

	public CrnkBoot getBoot() {
		return boot;
	}

	public RequestContextParameterProviderRegistry getParameterProviderRegistry() {
		return parameterProviderRegistry;
	}

	public String getWebPathPrefix() {
		return UrlUtils.removeLeadingSlash(boot.getWebPathPrefix());
	}

}
