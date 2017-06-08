package io.crnk.jpa;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.filter.AbstractDocumentFilter;
import io.crnk.core.engine.filter.DocumentFilterChain;
import io.crnk.core.engine.filter.DocumentFilterContext;
import io.crnk.core.engine.information.resource.ResourceInformationBuilder;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.core.module.Module;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.repository.decorate.RelationshipRepositoryDecorator;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactory;
import io.crnk.core.repository.decorate.ResourceRepositoryDecorator;
import io.crnk.core.resource.meta.DefaultHasMoreResourcesMetaInformation;
import io.crnk.core.resource.meta.DefaultPagedMetaInformation;
import io.crnk.jpa.internal.*;
import io.crnk.jpa.internal.query.backend.querydsl.QuerydslQueryImpl;
import io.crnk.jpa.mapping.JpaMapper;
import io.crnk.jpa.meta.JpaMetaProvider;
import io.crnk.jpa.meta.MetaEntity;
import io.crnk.jpa.meta.MetaJpaDataObject;
import io.crnk.jpa.query.JpaQueryFactory;
import io.crnk.jpa.query.JpaQueryFactoryContext;
import io.crnk.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.crnk.jpa.query.querydsl.QuerydslQueryFactory;
import io.crnk.jpa.query.querydsl.QuerydslRepositoryFilter;
import io.crnk.jpa.query.querydsl.QuerydslTranslationContext;
import io.crnk.jpa.query.querydsl.QuerydslTranslationInterceptor;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceBase;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.ManagedType;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Crnk module that adds support to expose JPA entities as repositories. It
 * supports:
 * <p>
 * <ul>
 * <li>Sorting</li>
 * <li>Filtering</li>
 * <li>Access to relationships for any operation (sorting, filtering, etc.)</li>
 * <li>Includes for relationships</li>
 * <li>Paging</li>
 * <li>Mapping to DTOs</li>
 * <li>Criteria API and QueryDSL support</li>
 * <li>Computed attributes that map JPA Criteria/QueryDSL expressions to DTO
 * attributes</li>
 * <li>JpaRepositoryFilter to customize the repositories</li>
 * <li>Client and server support</li>
 * <li>No need for crnk annotations by default. Reads the entity
 * annotations.</li>
 * </ul>
 * <p>
 * <p>
 * Not supported so far:
 * <p>
 * <ul>
 * <li>Selection of fields, always all fields are returned.</li>
 * <li>Sorting and filtering on related resources. Consider doing separate
 * requests on the relations where necessary.</li>
 * </ul>
 */
public class JpaModule implements Module {

	private static final String MODULE_NAME = "jpa";
	private Logger logger = LoggerFactory.getLogger(JpaModule.class);
	private EntityManagerFactory emFactory;

	private EntityManager em;

	private JpaQueryFactory queryFactory;

	private ResourceInformationBuilder resourceInformationBuilder;

	private TransactionRunner transactionRunner;

	private ModuleContext context;

	private MetaLookup jpaMetaLookup = new MetaLookup();

	private MetaLookup resourceMetaLookup = new MetaLookup();

	/**
	 * Maps resource class to its configuration
	 */
	private Map<Class<?>, JpaRepositoryConfig<?>> repositoryConfigurationMap = new HashMap<>();

	private JpaRepositoryFactory repositoryFactory;

	private List<JpaRepositoryFilter> filters = new CopyOnWriteArrayList<>();

	private ResourceMetaProvider resourceMetaProvider;

	private boolean totalResourceCountUsed = true;

	/**
	 * Constructor used on client side.
	 */
	private JpaModule() {
		this.jpaMetaLookup.addProvider(new JpaMetaProvider());
		this.resourceMetaProvider = new ResourceMetaProvider(false);
		this.resourceMetaLookup.addProvider(resourceMetaProvider);
	}

	/**
	 * Constructor used on server side.
	 */
	private JpaModule(EntityManagerFactory emFactory, EntityManager em, TransactionRunner transactionRunner) {
		this();

		this.emFactory = emFactory;
		this.em = em;
		this.transactionRunner = transactionRunner;
		setQueryFactory(JpaCriteriaQueryFactory.newInstance());

		if (emFactory != null) {
			Set<ManagedType<?>> managedTypes = emFactory.getMetamodel().getManagedTypes();
			for (ManagedType<?> managedType : managedTypes) {
				Class<?> managedJavaType = managedType.getJavaType();
				MetaElement meta = jpaMetaLookup.getMeta(managedJavaType, MetaJpaDataObject.class);
				if (meta instanceof MetaEntity) {
					addRepository(JpaRepositoryConfig.builder(managedJavaType).build());
				}
			}
		}
		this.setRepositoryFactory(new DefaultJpaRepositoryFactory());
	}

	/**
	 * Creates a new JpaModule for a Crnk client.
	 *
	 * @return module
	 */
	public static JpaModule newClientModule() {
		return new JpaModule();
	}

	/**
	 * Creates a new JpaModule for a Crnk server. No entities are by
	 * default exposed as JSON API resources. Make use of
	 * {@link #addEntityClass(Class)} andd
	 * {@link #addMappedEntityClass(Class, Class, JpaMapper)} to add resources.
	 *
	 * @param em                to use
	 * @param transactionRunner to use
	 * @return created module
	 */
	public static JpaModule newServerModule(EntityManager em, TransactionRunner transactionRunner) {
		return new JpaModule(null, em, transactionRunner);
	}

	/**
	 * Creates a new JpaModule for a Crnk server. All entities managed by
	 * the provided EntityManagerFactory are registered to the module and
	 * exposed as JSON API resources if not later configured otherwise.
	 *
	 * @param emFactory         to retrieve the managed entities.
	 * @param em                to use
	 * @param transactionRunner to use
	 * @return created module
	 */
	public static JpaModule newServerModule(EntityManagerFactory emFactory, EntityManager em, TransactionRunner transactionRunner) {
		return new JpaModule(emFactory, em, transactionRunner);
	}

	/**
	 * Adds the given filter to this module. Filter will be used by all
	 * repositories managed by this module.
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

	public void setRepositoryFactory(JpaRepositoryFactory repositoryFactory) {
		checkNotInitialized();
		this.repositoryFactory = repositoryFactory;
	}

	/**
	 * @return set of resource classes made available as resource (entity or
	 * dto).
	 * @Deprecated use getResourceClasses
	 */
	public Set<Class<?>> getResourceClasses() {
		return Collections.unmodifiableSet(repositoryConfigurationMap.keySet());
	}

	/**
	 * Adds the resource to this module.
	 *
	 * @param configuration to use
	 */
	public <T> void addRepository(JpaRepositoryConfig<T> config) {
		checkNotInitialized();
		Class<?> resourceClass = config.getResourceClass();
		if (repositoryConfigurationMap.containsKey(resourceClass)) {
			throw new IllegalArgumentException(resourceClass.getName() + " is already registered");
		}
		repositoryConfigurationMap.put(resourceClass, config);
	}

	/**
	 * Removes the resource with the given type from this module.
	 *
	 * @param <D>           resourse class (entity or mapped dto)
	 * @param resourceClass to remove
	 */
	public <T> void removeRepository(Class<T> resourceClass) {
		checkNotInitialized();
		repositoryConfigurationMap.remove(resourceClass);
	}

	/**
	 * Removes all entity classes registered by default. Use
	 * {@link #addEntityClass(Class)} or
	 * {@link #addMappedEntityClass(Class, Class, JpaMapper)} to register
	 * classes manually.
	 */
	public void removeRepositories() {
		checkNotInitialized();
		repositoryConfigurationMap.clear();
	}

	@Override
	public String getModuleName() {
		return MODULE_NAME;
	}

	private void checkNotInitialized() {
		PreconditionUtil.assertNull("module is already initialized, no further changes can be performed", context);
	}

	@Override
	public void setupModule(ModuleContext context) {
		this.context = context;

		this.jpaMetaLookup.setModuleContext(context);
		this.jpaMetaLookup.initialize();
		this.resourceMetaLookup.setModuleContext(context);
		this.resourceMetaLookup.initialize();

		context.addResourceInformationBuilder(getResourceInformationBuilder());
		context.addExceptionMapper(new OptimisticLockExceptionMapper());
		context.addExceptionMapper(new PersistenceExceptionMapper(context));
		context.addExceptionMapper(new PersistenceRollbackExceptionMapper(context));

		addHibernateConstraintViolationExceptionMapper();
		addTransactionRollbackExceptionMapper();
		context.addRepositoryDecoratorFactory(new JpaRepositoryDecoratorFactory());

		if (em != null) {
			setupServerRepositories();
			setupTransactionMgmt();
		}
	}

	private void addHibernateConstraintViolationExceptionMapper() {
		try {
			Class.forName("org.hibernate.exception.ConstraintViolationException");
		} catch (ClassNotFoundException e) { // NOSONAR
			// may not be available depending on environment
			return;
		}

		try {
			Class<?> mapperClass = Class.forName("io.crnk.jpa.internal.HibernateConstraintViolationExceptionMapper");
			Constructor<?> constructor = mapperClass.getConstructor();
			ExceptionMapper<?> mapper = (ExceptionMapper<?>) constructor.newInstance();
			context.addExceptionMapper(mapper);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private void addTransactionRollbackExceptionMapper() {
		try {
			Class.forName("javax.transaction.RollbackException");
		} catch (ClassNotFoundException e) { // NOSONAR
			// may not be available depending on environment
			return;
		}

		try {
			Class<?> mapperClass = Class.forName("io.crnk.jpa.internal.TransactionRollbackExceptionMapper");
			Constructor<?> constructor = mapperClass.getConstructor(ModuleContext.class);
			ExceptionMapper<?> mapper = (ExceptionMapper<?>) constructor.newInstance(context);
			context.addExceptionMapper(mapper);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	protected void setupTransactionMgmt() {
		context.addFilter(new AbstractDocumentFilter() {

			@Override
			public Response filter(final DocumentFilterContext context, final DocumentFilterChain chain) {
				return transactionRunner.doInTransaction(new Callable<Response>() {

					@Override
					public Response call() throws Exception {
						return chain.doFilter(context);
					}
				});
			}
		});
	}

	private void setupServerRepositories() {
		for (JpaRepositoryConfig<?> config : repositoryConfigurationMap.values()) {
			setupRepository(config);
		}
	}

	private void setupRepository(JpaRepositoryConfig<?> config) {
		if(config.getListMetaClass() == DefaultPagedMetaInformation.class && !isTotalResourceCountUsed()){
			// TODO not that nice...
			config.setListMetaClass(DefaultHasMoreResourcesMetaInformation.class);
		}

		Class<?> resourceClass = config.getResourceClass();
		MetaEntity metaEntity = jpaMetaLookup.getMeta(config.getEntityClass(), MetaEntity.class);
		if (isValidEntity(metaEntity)) {
			JpaEntityRepository<?, Serializable> jpaRepository = repositoryFactory.createEntityRepository(this, config);

			ResourceRepositoryV2<?, ?> repository = filterResourceCreation(resourceClass, jpaRepository);

			context.addRepository(repository);
			setupRelationshipRepositories(resourceClass, config.getResourceClass() != config.getEntityClass());
		}
	}

	private ResourceRepositoryV2<?, ?> filterResourceCreation(Class<?> resourceClass, JpaEntityRepository<?, ?> repository) {
		JpaEntityRepository<?, ?> filteredRepository = repository;
		for (JpaRepositoryFilter filter : filters) {
			if (filter.accept(resourceClass)) {
				filteredRepository = filter.filterCreation(filteredRepository);
			}
		}
		return filteredRepository;
	}

	private RelationshipRepositoryV2<?, ?, ?, ?> filterRelationshipCreation(Class<?> resourceClass, JpaRelationshipRepository<?, ?, ?, ?> repository) {
		JpaRelationshipRepository<?, ?, ?, ?> filteredRepository = repository;
		for (JpaRepositoryFilter filter : filters) {
			if (filter.accept(resourceClass)) {
				filteredRepository = filter.filterCreation(filteredRepository);
			}
		}
		return filteredRepository;
	}

	/**
	 * Sets up relationship repositories for the given document class. In case
	 * of a mapper the resource class might not correspond to the entity class.
	 */
	private void setupRelationshipRepositories(Class<?> resourceClass, boolean mapped) {
		MetaLookup metaLookup = mapped ? resourceMetaLookup : jpaMetaLookup;

		Class<? extends MetaDataObject> metaClass = mapped ? MetaResourceBase.class : MetaJpaDataObject.class;
		MetaDataObject meta = metaLookup.getMeta(resourceClass, metaClass);

		for (MetaAttribute attr : meta.getAttributes()) {
			if (!attr.isAssociation()) {
				continue;
			}
			MetaType attrType = attr.getType().getElementType();

			if (attrType instanceof MetaEntity) {
				setupRelationshipRepositoryForEntity(resourceClass, attrType);
			} else if (attrType instanceof MetaResource) {
				setupRelationshipRepositoryForResource(resourceClass, attr, attrType);
			} else {
				throw new IllegalStateException("unable to process relation: " + attr.getId() + ", neither a entity nor a mapped entity is referenced");
			}
		}
	}

	private void setupRelationshipRepositoryForEntity(Class<?> resourceClass, MetaType attrType) {
		// normal entity association
		Class<?> attrImplClass = attrType.getImplementationClass();
		JpaRepositoryConfig<?> attrConfig = getRepositoryConfig(attrImplClass);

		// only include relations that are exposed as repositories
		if (attrConfig != null) {
			RelationshipRepositoryV2<?, ?, ?, ?> relationshipRepository = filterRelationshipCreation(attrImplClass, repositoryFactory.createRelationshipRepository(this, resourceClass, attrConfig));
			context.addRepository(relationshipRepository);
		}
	}

	private void setupRelationshipRepositoryForResource(Class<?> resourceClass, MetaAttribute attr, MetaType attrType) {
		Class<?> attrImplClass = attrType.getImplementationClass();
		JpaRepositoryConfig<?> attrConfig = getRepositoryConfig(attrImplClass);
		if (attrConfig == null || attrConfig.getMapper() == null) {
			throw new IllegalStateException("no mapped entity for " + attrType.getName() + " reference by " + attr.getId() + " registered");
		}
		JpaRepositoryConfig<?> targetConfig = getRepositoryConfig(attrImplClass);
		Class<?> targetResourceClass = targetConfig.getResourceClass();

		RelationshipRepositoryV2<?, ?, ?, ?> relationshipRepository = filterRelationshipCreation(targetResourceClass, repositoryFactory.createRelationshipRepository(this, resourceClass, attrConfig));
		context.addRepository(relationshipRepository);
	}


	private boolean isValidEntity(MetaEntity metaEntity) {
		if (metaEntity.getPrimaryKey() == null) {
			logger.warn("{} has no primary key and will be ignored", metaEntity.getName());
			return false;
		}
		if (metaEntity.getPrimaryKey().getElements().size() > 1) {
			logger.warn("{} has a compound primary key and will be ignored", metaEntity.getName());
			return false;
		}
		return true;
	}

	/**
	 * @return ResourceInformationBuilder used to describe JPA classes.
	 */
	public ResourceInformationBuilder getResourceInformationBuilder() {
		if (resourceInformationBuilder == null) {
			resourceInformationBuilder = new JpaResourceInformationBuilder(jpaMetaLookup);
		}
		return resourceInformationBuilder;
	}

	/**
	 * Sets the information builder to use to read JPA classes. See
	 * {@link JpaResourceInformationBuilder}}
	 *
	 * @param resourceInformationBuilder
	 */
	public void setResourceInformationBuilder(ResourceInformationBuilder resourceInformationBuilder) {
		if (this.resourceInformationBuilder != null) {
			throw new IllegalStateException("already set");
		}
		this.resourceInformationBuilder = resourceInformationBuilder;
	}

	/**
	 * @return {@link JpaQueryFactory}} implementation used to create JPA
	 * queries.
	 */
	public JpaQueryFactory getQueryFactory() {
		return queryFactory;
	}

	public void setQueryFactory(JpaQueryFactory queryFactory) {
		this.queryFactory = queryFactory;

		queryFactory.initalize(new JpaQueryFactoryContext() {

			@Override
			public EntityManager getEntityManager() {
				return em;
			}

			@Override
			public MetaLookup getMetaLookup() {
				return jpaMetaLookup;
			}
		});

		if (queryFactory instanceof QuerydslQueryFactory) {
			QuerydslQueryFactory querydslFactory = (QuerydslQueryFactory) queryFactory;
			querydslFactory.addInterceptor(new JpaQuerydslTranslationInterceptor());
		}
	}

	/**
	 * @return {@link EntityManager}} in use.
	 */
	public EntityManager getEntityManager() {
		return em;
	}

	/**
	 * @return {@link EntityManagerFactory}} in use.
	 */
	public EntityManagerFactory getEntityManagerFactory() {
		return emFactory;
	}

	/**
	 * @param resourceClass
	 * @return config
	 */
	@SuppressWarnings("unchecked")
	public <T> JpaRepositoryConfig<T> getRepositoryConfig(Class<T> resourceClass) {
		return (JpaRepositoryConfig<T>) repositoryConfigurationMap.get(resourceClass);
	}

	public MetaLookup getJpaMetaLookup() {
		return jpaMetaLookup;
	}

	public MetaLookup getResourceMetaLookup() {
		return resourceMetaLookup;
	}

	public boolean isTotalResourceCountUsed() {
		return totalResourceCountUsed;
	}

	/**
	 * Computing the totalResourceCount can be expensive. Internally it is used to compute the last page link.
	 * This flag allows enable (default) or disable totalResourceCount computation. If it is disabled,
	 * limit + 1 resources are fetched and the presence of the last one determines whether a pagination next
	 * link will be provided.
	 *
	 * @param totalResourceCountUsed
	 */
	public void setTotalResourceCountUsed(boolean totalResourceCountUsed) {
		this.totalResourceCountUsed = totalResourceCountUsed;
	}

	/**
	 * @param resourceClass
	 * @return true if a resource for the given resourceClass is managed by
	 * this module.
	 */
	public boolean hasRepository(Class<?> resourceClass) {
		return repositoryConfigurationMap.containsKey(resourceClass);
	}

	private final class JpaQuerydslTranslationInterceptor implements QuerydslTranslationInterceptor {

		@Override
		public <T> void intercept(QuerydslQueryImpl<T> query, QuerydslTranslationContext<T> translationContext) {

			JpaRequestContext requestContext = (JpaRequestContext) query.getPrivateData();
			if (requestContext != null) {
				for (JpaRepositoryFilter filter : filters) {
					invokeFilter(filter, requestContext, translationContext);
				}
			}
		}

		private <T> void invokeFilter(JpaRepositoryFilter filter, JpaRequestContext requestContext, QuerydslTranslationContext<T> translationContext) {
			if (filter instanceof QuerydslRepositoryFilter) {
				Object repository = requestContext.getRepository();
				QuerySpec querySpec = requestContext.getQuerySpec();
				((QuerydslRepositoryFilter) filter).filterQueryTranslation(repository, querySpec, translationContext);
			}
		}
	}

	class JpaRepositoryDecoratorFactory implements RepositoryDecoratorFactory {

		@Override
		public <T, I extends Serializable> ResourceRepositoryDecorator<T, I> decorateRepository(ResourceRepositoryV2<T, I> repository) {
			JpaRepositoryConfig<T> config = getRepositoryConfig(repository.getResourceClass());
			if (config != null) {
				return config.getRepositoryDecorator();
			}
			return null;
		}

		@Override
		public <T, I extends Serializable, D, J extends Serializable> RelationshipRepositoryDecorator<T, I, D, J> decorateRepository(RelationshipRepositoryV2<T, I, D, J> repository) {
			JpaRepositoryConfig<T> config = getRepositoryConfig(repository.getSourceResourceClass());
			if (config != null) {
				return config.getRepositoryDecorator(repository.getTargetResourceClass());
			}
			return null;
		}
	}
}
