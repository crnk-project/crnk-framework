package io.crnk.jpa;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.ManagedType;

import io.crnk.jpa.internal.QueryFactoryDiscovery;
import io.crnk.jpa.query.JpaQueryFactory;

public class JpaModuleConfig {

	private JpaQueryFactory queryFactory;

	private List<JpaRepositoryFilter> filters = new CopyOnWriteArrayList<>();

	private boolean totalResourceCountUsed = true;

	private JpaRepositoryFactory repositoryFactory = new DefaultJpaRepositoryFactory();

	public JpaModuleConfig() {
	}

	public void setRepositoryFactory(JpaRepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
	}

	public JpaRepositoryFactory getRepositoryFactory() {
		return repositoryFactory;
	}

	/**
	 * Maps resource class to its configuration
	 */
	private Map<Class<?>, JpaRepositoryConfig<?>> repositoryConfigurationMap = new HashMap<>();

	/**
	 * Adds the given filter to this module. Filter will be used by all
	 * repositories managed by this module. Filters can also be configured
	 * per-repository with {@link JpaRepositoryConfig#addFilter(JpaRepositoryFilter)}.
	 *
	 * @param filter to add
	 */
	public void addFilter(JpaRepositoryFilter filter) {
		filters.add(filter);
	}

	/**
	 * Removes the given filter to this module.
	 *
	 * @param filter to remove
	 */
	public void removeFilter(JpaRepositoryFilter filter) {
		filters.remove(filter);
	}

	/**
	 * @return all filters
	 */
	public List<JpaRepositoryFilter> getFilters() {
		return filters;
	}


	public boolean isTotalResourceCountUsed() {
		return totalResourceCountUsed;
	}

	/**
	 * Computing the totalResourceCount can be expensive. Internally it is used to compute the last page link.
	 * This flag allows enable (default) or disable totalResourceCount computation. If it is disabled,
	 * limit + 1 resources are fetched and the presence of the last one determines whether a pagination next
	 * link will be provided.
	 * <p>
	 * The behavior can also be configured per-repository with {@link JpaRepositoryConfig#setTotalAvailable(Boolean)}.
	 */
	public void setTotalResourceCountUsed(boolean totalResourceCountUsed) {
		this.totalResourceCountUsed = totalResourceCountUsed;
	}

	/**
	 * @return true if a resource for the given resourceClass is managed by
	 * this module.
	 */
	public boolean hasRepository(Class<?> resourceClass) {
		return repositoryConfigurationMap.containsKey(resourceClass);
	}

	/**
	 * @return config
	 */
	@SuppressWarnings("unchecked")
	public <T> JpaRepositoryConfig<T> getRepository(Class<T> resourceClass) {
		return (JpaRepositoryConfig<T>) repositoryConfigurationMap.get(resourceClass);
	}

	public Collection<JpaRepositoryConfig> getRepositories() {
		return (Collection) repositoryConfigurationMap.values();
	}

	/**
	 * Adds the resource to this module.
	 *
	 * @param config to use
	 */
	public <T> void addRepository(JpaRepositoryConfig<T> config) {
		Class<?> resourceClass = config.getResourceClass();
		if (repositoryConfigurationMap.containsKey(resourceClass)) {
			throw new IllegalStateException(resourceClass.getName() + " is already registered");
		}
		repositoryConfigurationMap.put(resourceClass, config);
	}

	/**
	 * Exposes all entities as repositories.
	 */
	public void exposeAllEntities(EntityManagerFactory emf) {
		Set<ManagedType<?>> managedTypes = emf.getMetamodel().getManagedTypes();
		for (ManagedType<?> managedType : managedTypes) {
			Class<?> managedJavaType = managedType.getJavaType();
			if (managedJavaType.getAnnotation(Entity.class) != null) {
				addRepository(JpaRepositoryConfig.builder(managedJavaType).build());
			}
		}
	}

	/**
	 * Removes the resource with the given type from this module.
	 *
	 * @param <T> resourse class (entity or mapped dto)
	 * @param resourceClass to remove
	 */
	public <T> void removeRepository(Class<T> resourceClass) {
		repositoryConfigurationMap.remove(resourceClass);
	}

	/**
	 * Removes all entity classes registered by default. Use
	 * {@link #addRepository(JpaRepositoryConfig)} (Class)} or
	 * classes manually.
	 */
	public void removeRepositories() {
		repositoryConfigurationMap.clear();
	}

	/**
	 * @return set of resource classes made available as resource (entity or
	 * dto).
	 */
	public Set<Class<?>> getResourceClasses() {
		return Collections.unmodifiableSet(repositoryConfigurationMap.keySet());
	}


	/**
	 * @return {@link JpaQueryFactory}} implementation used to create JPA
	 * queries.
	 */
	public JpaQueryFactory getQueryFactory() {
		if(queryFactory == null){
			QueryFactoryDiscovery queryFactoryDiscovery = new QueryFactoryDiscovery();
			setQueryFactory(queryFactoryDiscovery.discoverDefaultFactory());
		}
		return queryFactory;
	}

	public void setQueryFactory(JpaQueryFactory queryFactory) {
		if(this.queryFactory != null){
			throw new IllegalStateException("queryFactory already set");
		}
		this.queryFactory = queryFactory;
	}
}
