package io.crnk.jpa;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.filter.AbstractDocumentFilter;
import io.crnk.core.engine.filter.DocumentFilterChain;
import io.crnk.core.engine.filter.DocumentFilterContext;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.ExceptionUtil;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.core.module.InitializingModule;
import io.crnk.core.module.ModuleExtension;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.repository.decorate.RelationshipRepositoryDecorator;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactory;
import io.crnk.core.repository.decorate.ResourceRepositoryDecorator;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.meta.DefaultHasMoreResourcesMetaInformation;
import io.crnk.core.resource.meta.DefaultPagedMetaInformation;
import io.crnk.jpa.internal.JpaRepositoryBase;
import io.crnk.jpa.internal.JpaRepositoryUtils;
import io.crnk.jpa.internal.JpaRequestContext;
import io.crnk.jpa.internal.JpaResourceInformationProvider;
import io.crnk.jpa.internal.OptimisticLockExceptionMapper;
import io.crnk.jpa.internal.PersistenceExceptionMapper;
import io.crnk.jpa.internal.PersistenceRollbackExceptionMapper;
import io.crnk.jpa.internal.query.backend.querydsl.QuerydslQueryImpl;
import io.crnk.jpa.meta.JpaMetaProvider;
import io.crnk.jpa.meta.MetaEntity;
import io.crnk.jpa.meta.internal.JpaMetaEnricher;
import io.crnk.jpa.query.JpaQueryFactory;
import io.crnk.jpa.query.JpaQueryFactoryContext;
import io.crnk.jpa.query.querydsl.QuerydslQueryFactory;
import io.crnk.jpa.query.querydsl.QuerydslRepositoryFilter;
import io.crnk.jpa.query.querydsl.QuerydslTranslationContext;
import io.crnk.jpa.query.querydsl.QuerydslTranslationInterceptor;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.MetaModuleExtension;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.provider.MetaPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

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
public class JpaModule implements InitializingModule {

	private static final String MODULE_NAME = "jpa";

	private Logger logger = LoggerFactory.getLogger(JpaModule.class);

	private EntityManagerFactory emFactory;

	private EntityManager em;

	private ResourceInformationProvider resourceInformationProvider;

	private TransactionRunner transactionRunner;

	private ModuleContext context;


	private JpaModuleConfig config;

	private JpaMetaEnricher metaEnricher;

	private MetaLookup jpaMetaLookup;

	private JpaMetaProvider jpaMetaProvider;

	/**
	 * Constructor used on client side.
	 */
	// protected for CDI
	protected JpaModule() {
	}

	/**
	 * Constructor used on server side.
	 */
	private JpaModule(JpaModuleConfig config, EntityManagerFactory emFactory, EntityManager em, TransactionRunner
			transactionRunner) {
		this();

		this.config = config;
		this.emFactory = emFactory;
		this.em = em;
		this.transactionRunner = transactionRunner;
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
	 * {@link #addRepository(JpaRepositoryConfig)} to add resources.
	 *
	 * @param em                to use
	 * @param transactionRunner to use
	 * @return created module
	 * @deprecated use with JpaModuleConfig
	 */
	@Deprecated
	public static JpaModule newServerModule(EntityManager em, TransactionRunner transactionRunner) {
		return new JpaModule(new JpaModuleConfig(), null, em, transactionRunner);
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
	 * @deprecated use with JpaModuleConfig
	 */
	@Deprecated
	public static JpaModule newServerModule(EntityManagerFactory emFactory, EntityManager em,
											TransactionRunner transactionRunner) {
		JpaModuleConfig config = new JpaModuleConfig();
		config.exposeAllEntities(emFactory);
		return new JpaModule(config, emFactory, em, transactionRunner);
	}

	/**
	 * Creates a new JpaModule for a Crnk server. No entities are by
	 * default exposed as JSON API resources. Make use of
	 * {@link #addRepository(JpaRepositoryConfig)} to add resources.
	 *
	 * @param em                to use
	 * @param transactionRunner to use
	 * @return created module
	 */
	public static JpaModule createServerModule(JpaModuleConfig config, EntityManager em, TransactionRunner transactionRunner) {
		return new JpaModule(config, null, em, transactionRunner);
	}

	/**
	 * Adds the given filter to this module. Filter will be used by all
	 * repositories managed by this module.
	 *
	 * @param filter to add
	 * @deprecated use {@link JpaModuleConfig}
	 */
	@Deprecated
	public void addFilter(JpaRepositoryFilter filter) {
		checkNotInitialized();
		config.addFilter(filter);
	}

	/**
	 * @deprecated use {@link JpaModuleConfig}
	 */
	@Deprecated
	public void removeFilter(JpaRepositoryFilter filter) {
		checkNotInitialized();
		config.removeFilter(filter);
	}

	/**
	 * @deprecated use {@link JpaModuleConfig}
	 */
	@Deprecated
	public List<JpaRepositoryFilter> getFilters() {
		return config.getFilters();
	}

	/**
	 * @deprecated use {@link JpaModuleConfig}
	 */
	@Deprecated
	public void setRepositoryFactory(JpaRepositoryFactory repositoryFactory) {
		checkNotInitialized();
		this.config.setRepositoryFactory(repositoryFactory);
	}

	/**
	 * @deprecated use {@link JpaModuleConfig}
	 */
	@Deprecated
	public Set<Class<?>> getResourceClasses() {
		return config.getResourceClasses();
	}

	/**
	 * @deprecated use {@link JpaModuleConfig}
	 */
	@Deprecated
	public <T> void addRepository(JpaRepositoryConfig<T> config) {
		checkNotInitialized();
		this.config.addRepository(config);
	}

	/**
	 * Removes the resource with the given type from this module.
	 *
	 * @param <T>           resourse class (entity or mapped dto)
	 * @param resourceClass to remove
	 */
	public <T> void removeRepository(Class<T> resourceClass) {
		checkNotInitialized();
		config.removeRepository(resourceClass);
	}

	/**
	 * Removes all entity classes registered by default. Use
	 * {@link #addRepository(JpaRepositoryConfig)} (Class)} or
	 * classes manually.
	 */
	public void removeRepositories() {
		checkNotInitialized();
		config.removeRepositories();
	}

	@Override
	public String getModuleName() {
		return MODULE_NAME;
	}

	private void checkNotInitialized() {
		PreconditionUtil.verify(context == null, "module is already initialized, no further changes can be performed");
	}

	@Override
	public void setupModule(ModuleContext context) {
		this.context = context;

		Set<Class> jpaTypes = new HashSet<>();
		if (config != null) {
			for (JpaRepositoryConfig<?> config : config.getRepositories()) {
				jpaTypes.add(config.getEntityClass());
			}
		}
		jpaMetaProvider = new JpaMetaProvider(jpaTypes);
		jpaMetaLookup = new MetaLookup();
		jpaMetaLookup.addProvider(jpaMetaProvider);
		jpaMetaLookup.setModuleContext(context);
		jpaMetaLookup.initialize();

		if (config != null) {
			initQueryFactory();
		}

		context.addResourceInformationBuilder(
				getResourceInformationProvider(context.getPropertiesProvider()));
		context.addExceptionMapper(new OptimisticLockExceptionMapper());
		context.addExceptionMapper(new PersistenceExceptionMapper(context));
		context.addExceptionMapper(new PersistenceRollbackExceptionMapper(context));

		addHibernateConstraintViolationExceptionMapper();
		addTransactionRollbackExceptionMapper();
		context.addRepositoryDecoratorFactory(new JpaRepositoryDecoratorFactory());

		if (em != null) {
			metaEnricher = new JpaMetaEnricher();

			// enrich resource meta model with JPA information where incomplete
			MetaModuleExtension metaModuleExtension = new MetaModuleExtension();
			metaModuleExtension.addProvider(metaEnricher.getProvider());
			context.addExtension(metaModuleExtension);

			setupTransactionMgmt();

			setupFacetExtension(context);
		}
	}

	private void setupFacetExtension(ModuleContext context) {
		if (ClassUtils.existsClass("io.crnk.data.facet.FacetModuleExtension")) {
			try {
				Class clazz = Class.forName("io.crnk.jpa.internal.facet.JpaFacetModuleExtensionFactory");
				Method method = clazz.getMethod("create");
				ModuleExtension homeExtension = (ModuleExtension) method.invoke(clazz);
				context.addExtension(homeExtension);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
	}

	private void initQueryFactory() {
		JpaQueryFactory queryFactory = config.getQueryFactory();
		queryFactory.initalize(new JpaQueryFactoryContext() {

			@Override
			public EntityManager getEntityManager() {
				return em;
			}

			@Override
			public MetaPartition getMetaPartition() {
				return jpaMetaProvider.getPartition();
			}
		});

		if (queryFactory instanceof QuerydslQueryFactory) {
			QuerydslQueryFactory querydslFactory = (QuerydslQueryFactory) queryFactory;
			querydslFactory.addInterceptor(new JpaQuerydslTranslationInterceptor());
		}
	}

	@Override
	public void init() {
		if (em != null) {
			setupServerRepositories();
		}
	}

	private void addHibernateConstraintViolationExceptionMapper() {
		// may not be available depending on environment
		if (ClassUtils.existsClass("org.hibernate.exception.ConstraintViolationException")) {
			ExceptionUtil.wrapCatchedExceptions(new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					Class<?> mapperClass = Class.forName("io.crnk.jpa.internal.HibernateConstraintViolationExceptionMapper");
					Constructor<?> constructor = mapperClass.getConstructor();
					ExceptionMapper<?> mapper = (ExceptionMapper<?>) constructor.newInstance();
					context.addExceptionMapper(mapper);
					return null;
				}
			});
		}
	}

	private void addTransactionRollbackExceptionMapper() {
		// may not be available depending on environment
		if (ClassUtils.existsClass("javax.transaction.RollbackException")) {
			ExceptionUtil.wrapCatchedExceptions(new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					Class<?> mapperClass = Class.forName("io.crnk.jpa.internal.TransactionRollbackExceptionMapper");
					Constructor<?> constructor = mapperClass.getConstructor(ModuleContext.class);
					ExceptionMapper<?> mapper = (ExceptionMapper<?>) constructor.newInstance(context);
					context.addExceptionMapper(mapper);
					return null;
				}
			});
		}
	}

	protected void setupTransactionMgmt() {
		context.addFilter(new AbstractDocumentFilter() {

			@Override
			public Response filter(final DocumentFilterContext context, final DocumentFilterChain chain) {
				return transactionRunner.doInTransaction(new Callable<Response>() {

					@Override
					public Response call() {
						return chain.doFilter(context);
					}
				});
			}
		});
	}

	private void setupServerRepositories() {
		metaEnricher.setMetaProvider(jpaMetaProvider);

		for (JpaRepositoryConfig<?> config : config.getRepositories()) {
			setupRepository(config);
		}
	}

	private void setupRepository(JpaRepositoryConfig<?> repositoryConfig) {
		if (repositoryConfig.getListMetaClass() == DefaultPagedMetaInformation.class && !isTotalResourceCountUsed()) {
			// TODO not that nice...
			repositoryConfig.setListMetaClass(DefaultHasMoreResourcesMetaInformation.class);
		}

		Class<?> resourceClass = repositoryConfig.getResourceClass();
		Class<?> entityClass = repositoryConfig.getEntityClass();
		MetaEntity metaEntity;
		try {
			metaEntity = jpaMetaProvider.getMeta(entityClass);
		} catch (RuntimeException e) {
			throw new IllegalStateException(
					"failed to gather entity informations from " + entityClass
							+ ", make sure it is probably annotated with JPA annotations", e);
		}
		if (isValidEntity(metaEntity)) {
			JpaRepositoryFactory repositoryFactory = config.getRepositoryFactory();

			JpaRepositoryUtils.setDefaultConfig(config, repositoryConfig);

			JpaEntityRepository<?, Serializable> jpaRepository = repositoryFactory.createEntityRepository(this,
					repositoryConfig);

			ResourceRepositoryV2<?, ?> repository = filterResourceCreation(resourceClass, jpaRepository);

			context.addRepository(repository);
			setupRelationshipRepositories(resourceClass,
					repositoryConfig.getResourceClass() != entityClass);
		}
	}

	private ResourceRepositoryV2<?, ?> filterResourceCreation(Class<?> resourceClass, JpaEntityRepository<?, ?> repository) {
		JpaEntityRepository<?, ?> filteredRepository = repository;
		for (JpaRepositoryFilter filter : config.getFilters()) {
			if (filter.accept(resourceClass)) {
				filteredRepository = filter.filterCreation(filteredRepository);
			}
		}
		return filteredRepository;
	}

	private RelationshipRepositoryV2<?, ?, ?, ?> filterRelationshipCreation(Class<?> resourceClass,
																			JpaRelationshipRepository<?, ?, ?, ?> repository) {
		JpaRelationshipRepository<?, ?, ?, ?> filteredRepository = repository;
		for (JpaRepositoryFilter filter : config.getFilters()) {
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
		if (config.getRelationshipsEnabled() && context.getResourceInformationBuilder().accept(resourceClass)) {
			ResourceInformation information = context.getResourceInformationBuilder().build(resourceClass);


			for (ResourceField field : information.getFields()) {
				if (field.getResourceFieldType() != ResourceFieldType.RELATIONSHIP) {
					continue;
				}

				Class<?> attrType = field.getElementType();
				boolean isEntity = attrType.getAnnotation(Entity.class) != null;
				if (isEntity) {
					setupRelationshipRepositoryForEntity(resourceClass, field);
				} else {
					setupRelationshipRepositoryForResource(resourceClass, field);
				}
			}
		}
	}

	private void setupRelationshipRepositoryForEntity(Class<?> resourceClass, ResourceField field) {
		// normal entity association
		Class<?> attrType = field.getElementType();
		JpaRepositoryConfig<?> attrConfig = getRepositoryConfig(attrType);

		// only include relations that are exposed as repositories
		if (attrConfig != null) {
			JpaRepositoryFactory repositoryFactory = config.getRepositoryFactory();
			RelationshipRepositoryV2<?, ?, ?, ?> relationshipRepository = filterRelationshipCreation(attrType,
					repositoryFactory.createRelationshipRepository(this, field, attrConfig));
			context.addRepository(relationshipRepository);
		}
	}

	private void setupRelationshipRepositoryForResource(Class<?> resourceClass, ResourceField field) {
		Class<?> attrImplClass = field.getElementType();
		JpaRepositoryConfig<?> attrConfig = getRepositoryConfig(attrImplClass);

		if (attrConfig != null) {
			PreconditionUtil.verify(attrConfig.getMapper() != null,
					"no mapped entity %s referenced from %s.%s registered", attrImplClass.getName(),
					field.getParentResourceInformation().getResourceType(), field.getUnderlyingName());

			JpaRepositoryConfig<?> targetConfig = getRepositoryConfig(attrImplClass);
			Class<?> targetResourceClass = targetConfig.getResourceClass();

			JpaRepositoryFactory repositoryFactory = config.getRepositoryFactory();
			RelationshipRepositoryV2<?, ?, ?, ?> relationshipRepository = filterRelationshipCreation(targetResourceClass,
					repositoryFactory.createRelationshipRepository(this, field, attrConfig));
			context.addRepository(relationshipRepository);
		}
	}


	private boolean isValidEntity(MetaEntity metaEntity) {
		for (MetaAttribute attribute : metaEntity.getAttributes()) {
			if (attribute.getAnnotation(JsonApiId.class) != null) {
				return true;
			}
		}

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
	 * @return ResourceInformationProvider used to describe JPA classes.
	 */
	protected ResourceInformationProvider getResourceInformationProvider(PropertiesProvider propertiesProvider) {
		if (resourceInformationProvider == null) {
			resourceInformationProvider = new JpaResourceInformationProvider(propertiesProvider);
		}
		return resourceInformationProvider;
	}

	/**
	 * Sets the information builder to use to read JPA classes. See
	 * {@link JpaResourceInformationProvider}}
	 */
	public void setResourceInformationProvider(ResourceInformationProvider resourceInformationProvider) {
		PreconditionUtil.verify(this.resourceInformationProvider == null, "already set");
		this.resourceInformationProvider = resourceInformationProvider;
	}

	/**
	 * @return {@link JpaQueryFactory}} implementation used to create JPA
	 * queries.
	 * @deprecated use JpaModuleConfig
	 */
	@Deprecated
	public JpaQueryFactory getQueryFactory() {
		return config.getQueryFactory();
	}

	/**
	 * @deprecated use JpaModuleConfig
	 */
	@Deprecated
	public void setQueryFactory(JpaQueryFactory queryFactory) {
		checkNotInitialized();
		config.setQueryFactory(queryFactory);
		if (context != null) {
			initQueryFactory();
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
	 * @return config
	 * @deprecated use {@link JpaModuleConfig}
	 */
	@Deprecated
	public <T> JpaRepositoryConfig<T> getRepositoryConfig(Class<T> resourceClass) {
		return config.getRepository(resourceClass);
	}

	public MetaLookup getJpaMetaLookup() {
		return jpaMetaLookup;
	}

	/**
	 * @deprecated use {@link JpaModuleConfig}
	 */
	@Deprecated
	public boolean isTotalResourceCountUsed() {
		return config.isTotalResourceCountUsed();
	}

	/**
	 * @deprecated use {@link JpaModuleConfig}
	 */
	public void setTotalResourceCountUsed(boolean totalResourceCountUsed) {
		checkNotInitialized();
		config.setTotalResourceCountUsed(totalResourceCountUsed);
	}

	/**
	 * @return true if a resource for the given resourceClass is managed by
	 * this module.
	 */
	public boolean hasRepository(Class<?> resourceClass) {
		return config.hasRepository(resourceClass);
	}

	public JpaMetaProvider getJpaMetaProvider() {
		return jpaMetaProvider;
	}

	public JpaModuleConfig getConfig() {
		return config;
	}

	private final class JpaQuerydslTranslationInterceptor implements QuerydslTranslationInterceptor {

		@Override
		public <T> void intercept(QuerydslQueryImpl<T> query, QuerydslTranslationContext<T> translationContext) {

			JpaRequestContext requestContext = (JpaRequestContext) query.getPrivateData();
			if (requestContext != null) {
				for (JpaRepositoryFilter filter : config.getFilters()) {
					invokeFilter(filter, requestContext, translationContext);
				}
			}
		}

		private <T> void invokeFilter(JpaRepositoryFilter filter, JpaRequestContext requestContext,
									  QuerydslTranslationContext<T> translationContext) {
			if (filter instanceof QuerydslRepositoryFilter) {
				Object repository = requestContext.getRepository();
				QuerySpec querySpec = requestContext.getQuerySpec();
				((QuerydslRepositoryFilter) filter).filterQueryTranslation(repository, querySpec, translationContext);
			}
		}
	}

	class JpaRepositoryDecoratorFactory implements RepositoryDecoratorFactory {

		@Override
		public <T, I extends Serializable> ResourceRepositoryDecorator<T, I> decorateRepository(
				ResourceRepositoryV2<T, I> repository) {
			if (repository instanceof JpaRepositoryBase) {
				JpaRepositoryConfig<T> config = getRepositoryConfig(repository.getResourceClass());
				if (config != null) {
					return config.getRepositoryDecorator();
				}
			}
			return null;
		}

		@Override
		public <T, I extends Serializable, D, J extends Serializable> RelationshipRepositoryDecorator<T, I, D, J>
		decorateRepository(
				RelationshipRepositoryV2<T, I, D, J> repository) {
			if (repository instanceof JpaRepositoryBase) {
				JpaRepositoryConfig<T> config = getRepositoryConfig(repository.getSourceResourceClass());
				if (config != null) {
					return config.getRepositoryDecorator(repository.getTargetResourceClass());
				}
			}
			return null;
		}
	}
}
