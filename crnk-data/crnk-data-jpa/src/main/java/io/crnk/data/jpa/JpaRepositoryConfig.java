package io.crnk.data.jpa;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.links.DefaultPagedLinksInformation;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceListBase;
import io.crnk.core.resource.meta.DefaultPagedMetaInformation;
import io.crnk.core.resource.meta.HasMoreResourcesMetaInformation;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.core.resource.meta.PagedMetaInformation;
import io.crnk.data.jpa.mapping.IdentityMapper;
import io.crnk.data.jpa.mapping.JpaMapper;
import io.crnk.data.jpa.query.JpaQueryFactory;
import net.jodah.typetools.TypeResolver;

/**
 * @param <T> document type (entity or mapped dto)
 */
public class JpaRepositoryConfig<T> {

	private Class<?> entityClass;

	private Class<T> resourceClass;

	private JpaMapper<?, T> mapper;

	private Class<? extends DefaultResourceList<T>> listClass;

	private Class<? extends MetaInformation> listMetaClass;

	private Class<? extends LinksInformation> listLinksClass;

	private Boolean totalAvailable;

	private List<JpaRepositoryFilter> filters = new ArrayList<>();

	private Supplier<JpaQueryFactory> queryFactory;

	private JpaRepositoryConfig() {
	}

	/**
	 * @return {@link JpaQueryFactory}} implementation used to create JPA queries.
	 */
	public JpaQueryFactory getQueryFactory() {
		PreconditionUtil.verify(queryFactory != null, "queryFactory not set, make sure the JpaModule is setup and discovered by Crnk");
		return queryFactory.get();
	}

	public boolean hasQueryFactory() {
		return queryFactory != null;
	}

	public void setQueryFactory(JpaQueryFactory queryFactory) {
		if (this.queryFactory != null) {
			throw new IllegalStateException("queryFactory already set");
		}
		this.queryFactory = () -> queryFactory;
	}

	public void setQueryFactory(Supplier<JpaQueryFactory> queryFactorySupplier) {
		if (this.queryFactory != null) {
			throw new IllegalStateException("queryFactory already set");
		}
		this.queryFactory = queryFactorySupplier;
	}

	public boolean hasTotalAvailable() {
		return totalAvailable != null;
	}

	public boolean isTotalAvailable() {
		return totalAvailable = DefaultPagedMetaInformation.class.isAssignableFrom(listMetaClass) && (totalAvailable == null || totalAvailable);
	}

	/**
	 * If true a total row count will be computed for each query (enabled by default). Can be disabled to optimize for performance.
	 */
	public void setTotalAvailable(Boolean totalAvailable) {
		this.totalAvailable = totalAvailable;
	}

	/**
	 * Adds the given filter to this module. Filter will be used by all repositories managed by this repository.
	 *
	 * @param filter to add
	 */
	public void addFilter(JpaRepositoryFilter filter) {
		filters.add(filter);
	}

	/**
	 * Removes the given filter to this repository.
	 *
	 * @param filter to remove
	 */
	public void removeFilter(JpaRepositoryFilter filter) {
		filters.remove(filter);
	}

	public List<JpaRepositoryFilter> getFilters() {
		return filters;
	}

	/**
	 * Shortcut for builder(entityClass).build().
	 *
	 * @param entityClass to directly expose
	 * @return config
	 */
	public static <E> JpaRepositoryConfig<E> create(Class<E> entityClass) {
		return builder(entityClass).build();
	}

	/**
	 * Prepares a builder to configure a jpa document for the given entity.
	 *
	 * @param <E>         entity type
	 * @param entityClass to directly expose
	 * @return builder
	 */
	public static <E> JpaRepositoryConfig.Builder<E> builder(Class<E> entityClass) {
		JpaRepositoryConfig.Builder<E> builder = new JpaRepositoryConfig.Builder<>();
		builder.entityClass = entityClass;
		builder.resourceClass = entityClass;
		return builder;
	}

	/**
	 * Prepares a builder to configure a jpa document for the given entity class which is mapped to a DTO with the provided mapper.
	 *
	 * @param <E>         entity type
	 * @param <D>         dto type
	 * @param entityClass to use
	 * @param dtoClass    to expose
	 * @param mapper      to convert entity to dto and back
	 * @return builder
	 */
	public static <E, D> JpaRepositoryConfig.Builder<D> builder(Class<E> entityClass, Class<D> dtoClass,
			JpaMapper<E, D> mapper) {
		JpaRepositoryConfig.Builder<D> builder = new JpaRepositoryConfig.Builder<>();
		builder.entityClass = entityClass;
		builder.resourceClass = dtoClass;
		builder.mapper = mapper;
		return builder;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

	public Class<T> getResourceClass() {
		return resourceClass;
	}

	public Class<? extends MetaInformation> getListMetaClass() {
		return listMetaClass;
	}

	public Class<? extends LinksInformation> getListLinksClass() {
		return listLinksClass;
	}

	@SuppressWarnings("unchecked")
	public <E> JpaMapper<E, T> getMapper() {
		return (JpaMapper<E, T>) mapper;
	}

	@SuppressWarnings("unchecked")
	public <M extends MetaInformation, L extends LinksInformation> Class<? extends ResourceListBase<T, M, L>> getListClass() {
		return (Class<? extends ResourceListBase<T, M, L>>) listClass;
	}

	public DefaultResourceList<T> newResultList() {
		DefaultResourceList<T> list = ClassUtils.newInstance(listClass);
		list.setMeta(newMetaInformation());
		list.setLinks(newLinksInformation());
		return list;
	}

	private MetaInformation newMetaInformation() {
		if (listMetaClass != null) {
			return ClassUtils.newInstance(listMetaClass);
		}
		else {
			return null;
		}
	}

	private LinksInformation newLinksInformation() {
		if (listLinksClass != null) {
			return ClassUtils.newInstance(listLinksClass);
		}
		else {
			return null;
		}
	}

	public boolean isNextFetched(QuerySpec querySpec) {
		return querySpec.getLimit() != null && !isTotalAvailable()
				&& getListMetaClass() != null
				&& HasMoreResourcesMetaInformation.class.isAssignableFrom(getListMetaClass());
	}

	public boolean isTotalFetched(QuerySpec querySpec) {
		return (querySpec.getOffset() != 0 || querySpec.getLimit() != null) && isTotalAvailable()
				&& getListMetaClass() != null
				&& PagedMetaInformation.class.isAssignableFrom(getListMetaClass());
	}


	protected void setListMetaClass(Class<? extends MetaInformation> listMetaClass) {
		this.listMetaClass = listMetaClass;
	}

	public static class Builder<T> {

		private Class<?> entityClass;

		private Class<T> resourceClass;

		private JpaMapper<?, T> mapper = IdentityMapper.newInstance();

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private Class<? extends DefaultResourceList<T>> listClass = (Class) DefaultResourceList.class;

		private Class<? extends MetaInformation> listMetaClass = DefaultPagedMetaInformation.class;

		private Class<? extends LinksInformation> listLinksClass = DefaultPagedLinksInformation.class;


		public JpaRepositoryConfig<T> build() {
			JpaRepositoryConfig<T> config = new JpaRepositoryConfig<>();
			config.entityClass = entityClass;
			config.resourceClass = resourceClass;
			config.mapper = mapper;
			config.listClass = listClass;
			config.listMetaClass = listMetaClass;
			config.listLinksClass = listLinksClass;
			return config;
		}

		/**
		 * Extracts information about listClass, listMetaClass, listLinkClass from the provided document interface.
		 *
		 * @param interfaceClass of the document
		 * @return this builder
		 */
		@SuppressWarnings("unchecked")
		public Builder<T> setInterfaceClass(Class<? extends ResourceRepository<T, ?>> interfaceClass) {

			try {
				Method findMethod = interfaceClass.getDeclaredMethod("findAll", QuerySpec.class);
				Class<?> returnType = findMethod.getReturnType();
				if (!ResourceListBase.class.isAssignableFrom(returnType)) {
					throw new IllegalStateException("findAll return type must extend " + ResourceListBase.class.getName());
				}
				setListClass((Class<? extends DefaultResourceList<T>>) returnType);

				Class<?>[] typeArgs = TypeResolver.resolveRawArguments(ResourceListBase.class, returnType);
				setListMetaClass((Class<? extends MetaInformation>) typeArgs[1]);
				setListLinksClass((Class<? extends LinksInformation>) typeArgs[2]);
				return this;
			}
			catch (NoSuchMethodException e) {
				throw new IllegalStateException("findAll method not overriden by " + interfaceClass.getName(), e);
			}
		}

		/**
		 * @param listClass to be used to return list of resources
		 * @return this builder
		 */
		public Builder<T> setListClass(Class<? extends DefaultResourceList<T>> listClass) {
			this.listClass = listClass;
			return this;
		}

		/**
		 * @param listMetaClass holding the meta information
		 * @return this builder
		 */
		public Builder<T> setListMetaClass(Class<? extends MetaInformation> listMetaClass) {
			this.listMetaClass = listMetaClass;
			return this;
		}

		/**
		 * @param listLinksClass holding the links information
		 * @return this builder
		 */
		public Builder<T> setListLinksClass(Class<? extends LinksInformation> listLinksClass) {
			this.listLinksClass = listLinksClass;
			return this;
		}
	}
}
