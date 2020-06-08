package io.crnk.data.jpa.internal.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.criteria.JoinType;

import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.IncludeFieldSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;
import io.crnk.data.jpa.internal.query.backend.JpaQueryBackend;
import io.crnk.data.jpa.query.JpaQuery;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaAttributePath;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.provider.MetaPartition;

public abstract class AbstractJpaQueryImpl<T, B extends JpaQueryBackend<?, ?, ?, ?>> implements JpaQuery<T> {

	protected final EntityManager em;

	protected final MetaDataObject meta;

	protected final Class<T> clazz;

	protected final Map<MetaAttributePath, JoinType> joinTypes = new HashMap<>();

	protected JoinType defaultJoinType = JoinType.INNER;

	protected ArrayList<FilterSpec> filterSpecs = new ArrayList<>();

	protected ArrayList<SortSpec> sortSpecs = new ArrayList<>();

	protected ArrayList<IncludeFieldSpec> includedFields = new ArrayList<>();

	protected boolean autoDistinct = true;

	protected boolean autoGroupBy = false;

	protected boolean distinct = false;

	protected boolean ensureTotalOrder = true;

	protected boolean alphabeticEmbeddableElementOrder = true;

	protected MetaDataObject parentMeta;

	protected List<?> parentIds;

	protected MetaAttribute parentAttr;

	protected boolean parentIdSelection;

	protected String parentKey;

	private ComputedAttributeRegistryImpl computedAttrs;

	private Object privateData;

	protected AbstractJpaQueryImpl(MetaPartition metaPartition, EntityManager em, Class<T> clazz,
			ComputedAttributeRegistryImpl computedAttrs) {
		this.em = em;
		this.clazz = clazz;
		this.computedAttrs = computedAttrs;

		Optional<MetaElement> optMetaElement = metaPartition.allocateMetaElement(clazz);
		PreconditionUtil.verify(optMetaElement.isPresent(), "failed to JPA meta information for {}, make sure the class is properly annotated with JPA annotations.", clazz);
		this.meta = optMetaElement.get().asDataObject();
	}

	@SuppressWarnings("unchecked")
	public AbstractJpaQueryImpl(MetaPartition metaPartition, EntityManager em, Class<?> entityClass,
			ComputedAttributeRegistryImpl virtualAttrs, String attrName, String parentKey, List<?> entityIds) {
		this.em = em;
		this.computedAttrs = virtualAttrs;

		this.parentMeta = (MetaDataObject) metaPartition.getMeta(entityClass);
		MetaAttribute attrMeta = parentMeta.getAttribute(attrName);
		if (attrMeta.getType().isCollection()) {
			this.meta = (MetaDataObject) attrMeta.getType().asCollection().getElementType();
		}
		else {
			this.meta = (MetaDataObject) attrMeta.getType();
		}
		this.clazz = (Class<T>) meta.getImplementationClass();

		this.parentAttr = attrMeta;
		this.parentIds = entityIds;
		this.parentKey = parentKey;
	}

	public Object getPrivateData() {
		return privateData;
	}

	public void setPrivateData(Object privateData) {
		this.privateData = privateData;
	}

	@Override
	public void addParentIdSelection() {
		this.parentIdSelection = true;
	}

	@Override
	public void addSelection(List<String> path) {
		includedFields.add(new IncludeFieldSpec(path));
	}

	@Override
	public JpaQuery<T> setEnsureTotalOrder(boolean ensureTotalOrder) {
		this.ensureTotalOrder = ensureTotalOrder;
		return this;
	}

	@Override
	public JpaQuery<T> setAlphabeticEmbeddableElementOrder(boolean alphabeticEmbeddableElementOrder) {
		this.alphabeticEmbeddableElementOrder = alphabeticEmbeddableElementOrder;
		return this;
	}


	@Override
	public JpaQuery<T> addFilter(FilterSpec filters) {
		this.filterSpecs.add(filters);
		return this;
	}

	@Override
	public JpaQuery<T> addSortBy(List<String> attributePath, Direction dir) {
		this.sortSpecs.add(new SortSpec(attributePath, dir));
		return this;
	}

	@Override
	public JpaQuery<T> addSortBy(SortSpec order) {
		this.sortSpecs.add(order);
		return this;
	}

	@Override
	public JpaQuery<T> setDefaultJoinType(JoinType joinType) {
		this.defaultJoinType = joinType;
		return this;
	}

	@Override
	public JpaQuery<T> setJoinType(List<String> path, JoinType joinType) {
		joinTypes.put(meta.resolvePath(path), joinType);
		return this;
	}

	@Override
	public JpaQuery<T> setAutoGroupBy(boolean autoGroupBy) {
		this.autoGroupBy = autoGroupBy;
		return this;
	}

	@Override
	public JpaQuery<T> setDistinct(boolean distinct) {
		this.autoDistinct = false;
		this.distinct = distinct;
		return this;
	}

	@Override
	public JpaQuery<T> addFilter(String attrPath, FilterOperator filterOperator, Object value) {
		return addFilter(Arrays.asList(attrPath.split("\\.")), filterOperator, value);
	}

	@Override
	public JpaQuery<T> addFilter(List<String> attrPath, FilterOperator filterOperator, Object value) {
		addFilter(new FilterSpec(attrPath, filterOperator, value));
		return this;
	}

	public List<SortSpec> getSortSpecs() {
		return sortSpecs;
	}

	public boolean getEnsureTotalOrder() {
		return ensureTotalOrder;
	}

	public List<IncludeFieldSpec> getIncludedFields() {
		return includedFields;
	}

	public JoinType getJoinType(MetaAttributePath path, JoinType customDefaultJoinType) {
		JoinType joinType = joinTypes.get(path);
		if (joinType == null) {
			joinType = customDefaultJoinType;
		}
		if (joinType == null) {
			joinType = defaultJoinType;
		}
		return joinType;
	}

	public ComputedAttributeRegistryImpl getComputedAttrs() {
		return computedAttrs;
	}

	public MetaDataObject getMeta() {
		return meta;
	}

	@Override
	public Class<T> getEntityClass() {
		return clazz;
	}

	@Override
	public AbstractQueryExecutorImpl<T> buildExecutor(QuerySpec querySpec) {
		querySpec.getFilters().forEach(filter -> addFilter(filter));
		querySpec.getSort().forEach(sort -> addSortBy(sort));

		AbstractQueryExecutorImpl<T> executor = buildExecutor();
		querySpec.getIncludedRelations().forEach(include -> executor.fetch(include.getAttributePath()));
		executor.setPaging(querySpec.getPaging());
		return executor;
	}


	@Override
	public AbstractQueryExecutorImpl<T> buildExecutor() {
		B backend = newBackend();

		@SuppressWarnings({ "rawtypes", "unchecked" })
		QueryBuilder executorFactory = new QueryBuilder(this, backend);
		Map<String, Integer> selectionBindings = executorFactory.applySelectionSpec();
		executorFactory.applyFilterSpec();
		executorFactory.applySortSpec();
		int numAutoSelections = executorFactory.applyDistinct();
		return newExecutor(backend, numAutoSelections, selectionBindings);
	}

	protected abstract AbstractQueryExecutorImpl<T> newExecutor(B ctx, int numAutoSelections,
			Map<String, Integer> selectionBindings);

	protected abstract B newBackend();

	@SuppressWarnings({ "unchecked", "hiding" })
	public <T> List<T> getParentIds() {
		return (List<T>) parentIds;
	}

	public List<FilterSpec> getFilterSpecs() {
		return filterSpecs;
	}

	public MetaAttribute getParentAttr() {
		return parentAttr;
	}

	public MetaDataObject getParentMeta() {
		return parentMeta;
	}

	public JoinType getDefaultJoinType() {
		return defaultJoinType;
	}
}
