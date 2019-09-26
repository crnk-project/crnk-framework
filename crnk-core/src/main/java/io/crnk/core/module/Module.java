package io.crnk.core.module;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.RepositoryFilter;
import io.crnk.core.engine.filter.ResourceFilter;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.filter.ResourceModificationFilter;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.engine.http.HttpStatusBehavior;
import io.crnk.core.engine.information.contributor.ResourceFieldContributor;
import io.crnk.core.engine.information.repository.RepositoryInformationProvider;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.exception.ExceptionMapperLookup;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.engine.internal.repository.RepositoryAdapterFactory;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.RegistryEntryBuilder;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryPart;
import io.crnk.core.engine.result.ResultFactory;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.module.discovery.ResourceLookup;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactory;

/**
 * Interface for extensions that can be registered to Crnk to provide a
 * well-defined set of extensions on top of the default functionality.
 */
public interface Module {

	/**
	 * Returns the identifier of this module.
	 *
	 * @return module name
	 */
	String getModuleName();

	/**
	 * Called when the module is registered with Crnk. Allows the module to
	 * register functionality it provides.
	 *
	 * @param context context
	 */
	void setupModule(ModuleContext context);


	/**
	 * Interface Crnk exposes to modules for purpose of registering
	 * extended functionality.
	 */
	interface ModuleContext {

		DocumentMapper getDocumentMapper();

		/**
		 * Sets the ResultFactory used. Can only be called once by all modules!
		 */
		void setResultFactory(ResultFactory resultFactory);

		/**
		 * Adds the given extension
		 */
		void addExtension(ModuleExtension extension);

		void addHttpRequestProcessor(HttpRequestProcessor processor);

		ObjectMapper getObjectMapper();

		/**
		 * Registers a {@link ResourceRegistryPart} implementation. An empty string as prefix will
		 * register the root part.
		 */
		void addRegistryPart(String prefix, ResourceRegistryPart part);

		/**
		 * Return the {@link PropertiesProvider}.
		 *
		 * @return {@link PropertiesProvider}
		 */
		PropertiesProvider getPropertiesProvider();

		/**
		 * @return ServiceDiscovery
		 */
		ServiceDiscovery getServiceDiscovery();

		/**
		 * Register the given {@link ResourceInformationProvider} in Crnk.
		 *
		 * @param resourceInformationProvider resource information builder
		 */
		void addResourceInformationProvider(ResourceInformationProvider resourceInformationProvider);

		/**
		 * Register the given {@link RepositoryInformationProvider} in Crnk.
		 *
		 * @param RepositoryInformationBuilder resource information builder
		 */
		void addRepositoryInformationBuilder(RepositoryInformationProvider repositoryInformationProvider);

		/**
		 * Add the given {@link PagingBehavior} to the module
		 *
		 * @param pagingBehavior the paging behavior
		 */
		void addPagingBehavior(PagingBehavior pagingBehavior);

		/**
		 * Register the given {@link ResourceLookup} in Crnk.
		 *
		 * @param resourceLookup resource lookup
		 */
		void addResourceLookup(ResourceLookup resourceLookup);

		/**
		 * Registers an additional module for Jackson.
		 *
		 * @param module module
		 */
		void addJacksonModule(com.fasterxml.jackson.databind.Module module);

		/**
		 * Adds the given repository for the given type.
		 */
		void addRepository(Object repository);

		/**
		 * Adds a new exception mapper lookup.
		 *
		 * @param exceptionMapperLookup exception mapper lookup
		 */
		void addExceptionMapperLookup(ExceptionMapperLookup exceptionMapperLookup);

		/**
		 * Adds a new exception mapper lookup.
		 *
		 * @param exceptionMapper exception mapper
		 */
		void addExceptionMapper(ExceptionMapper<?> exceptionMapper);

		/**
		 * Adds a filter to intercept requests.
		 *
		 * @param filter filter
		 */
		void addFilter(DocumentFilter filter);

		/**
		 * Adds a repository filter to intercept repository calls.
		 */
		void addRepositoryFilter(RepositoryFilter filter);

		/**
		 * Adds a resource filter to manage access to resources and fields.
		 */
		void addResourceFilter(ResourceFilter filter);


		void addResourceFieldContributor(ResourceFieldContributor contributor);

		/**
		 * Adds a repository decorator to intercept repository calls.
		 */
		void addRepositoryDecoratorFactory(RepositoryDecoratorFactory decorator);

		/**
		 * Returns the ResourceRegistry. Note that instance is not yet available
		 * when {@link Module#setupModule(ModuleContext)} is called. So
		 * consumers may have to hold onto the {@link ModuleContext} instead.
		 *
		 * @return ResourceRegistry
		 */
		ResourceRegistry getResourceRegistry();

		/**
		 * Adds a securityProvider.
		 *
		 * @param securityProvider Ask remo
		 */
		void addSecurityProvider(SecurityProvider securityProvider);

		/**
		 * Returns the security provider. Provides access to security related
		 * feature independent of the underlying implementation.
		 */
		SecurityProvider getSecurityProvider();

		/**
		 * @return if the module runs on the server-side
		 */
		boolean isServer();

		TypeParser getTypeParser();

		/**
		 * @return combined resource information build registered by all modules
		 */
		ResourceInformationProvider getResourceInformationBuilder();

		ExceptionMapperRegistry getExceptionMapperRegistry();

		RequestDispatcher getRequestDispatcher();

		RegistryEntryBuilder newRegistryEntryBuilder();

		void addRegistryEntry(RegistryEntry entry);

		/**
		 * @return information about how resources and field get filtered
		 */
		ResourceFilterDirectory getResourceFilterDirectory();

		void addResourceModificationFilter(ResourceModificationFilter filter);

		ResultFactory getResultFactory();

		List<DocumentFilter> getDocumentFilters();

		void addRepositoryAdapterFactory(RepositoryAdapterFactory repositoryAdapterFactory);

		ModuleRegistry getModuleRegistry();

		void addHttpStatusBehavior(HttpStatusBehavior httpStatusBehavior);
	}
}
