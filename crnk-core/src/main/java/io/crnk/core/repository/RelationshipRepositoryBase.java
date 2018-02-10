package io.crnk.core.repository;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryAware;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.annotations.JsonApiToMany;
import io.crnk.core.resource.annotations.JsonApiToOne;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Recommended base class to implement a relationship repository making use of
 * the QuerySpec and ResourceList. Note that the former
 * {@link RelationshipRepositoryBase} will be removed in the near
 * future.
 * <p>
 * Base implementation for {@link RelationshipRepositoryV2} implementing
 * <b>ALL</b> of the methods. Makes use of the source and target resource
 * repository to implement the relationship features. Modification are
 * implemented by fetching the target resources, adding them the the source
 * repository with reflection and then saving the source repository. Lookup is
 * implemented by querying the target resource repository and filtering in the
 * opposite relationship direction. Not that {@link JsonApiToMany} resp.
 * {@link JsonApiToOne} need to declare the opposite name for the relations.
 * <p>
 * Warning: this implementation does not take care of bidirectionality. This is
 * usuefally very implementation-specific and cannot be handled in a generic
 * fashion. Setting a relation on a resource and saving it assumes that the save
 * operation makes sure that the relationship is setup in a bi-directional way.
 * You may run here into issues, for example in basic in-memory repositories
 * (implement getters, setters, adds and remove on beans accordingly) or with
 * JPA (where only the owning entity can update a relation).
 *
 * @param <T> source resource type
 * @param <I> source identity type
 * @param <D> target resource type
 * @param <J> target identity type
 */
public class RelationshipRepositoryBase<T, I extends Serializable, D, J extends Serializable>
		implements BulkRelationshipRepositoryV2<T, I, D, J>, ResourceRegistryAware {


	protected ResourceRegistry resourceRegistry;

	private Class<T> sourceResourceClass;

	private String sourceResourceType;

	private Class targetResourceClass;

	private String targetResourceType;


	/**
	 * default constructor for CDI an other DI libraries
	 */
	protected RelationshipRepositoryBase() {
	}

	public RelationshipRepositoryBase(Class<T> sourceResourceClass, Class<D> targetResourceClass) {
		this(sourceResourceClass);
		this.targetResourceClass = targetResourceClass;
	}

	public RelationshipRepositoryBase(String sourceResourceType, String targetResourceType) {
		this(sourceResourceType);
		this.targetResourceType = targetResourceType;
	}

	public RelationshipRepositoryBase(Class<T> sourceResourceClass) {
		this.sourceResourceClass = sourceResourceClass;
	}

	public RelationshipRepositoryBase(String sourceResourceType) {
		this.sourceResourceType = sourceResourceType;
	}

	@Override
	public RelationshipMatcher getMatcher() {
		RelationshipMatcher matcher = new RelationshipMatcher();
		matcher.rule().source(sourceResourceType).source(sourceResourceClass).target(targetResourceType)
				.target(targetResourceClass).add();
		return matcher;
	}


	@Override
	public D findOneTarget(I sourceId, String fieldName, QuerySpec querySpec) {
		MultivaluedMap<I, D> map = findTargets(Arrays.asList(sourceId), fieldName, querySpec);
		if (map.isEmpty()) {
			return null;
		}
		return map.getUnique(sourceId);
	}

	@Override
	public ResourceList<D> findManyTargets(I sourceId, String fieldName, QuerySpec querySpec) {
		MultivaluedMap<I, D> map = findTargets(Arrays.asList(sourceId), fieldName, querySpec);
		if (map.isEmpty()) {
			return new DefaultResourceList<>();
		}
		return (ResourceList<D>) map.getList(sourceId);
	}

	@Override
	public void setRelation(T source, J targetId, String fieldName) {
		RegistryEntry sourceEntry = getSourceEntry();
		ResourceRepositoryAdapter<T, I> sourceAdapter = sourceEntry.getResourceRepository();
		ResourceInformation sourceInformation = getSourceEntry().getResourceInformation();
		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		if (field.hasIdField()) {
			field.getIdAccessor().setValue(source, targetId);
		}
		else {
			RegistryEntry targetEntry = getTargetEntry(field);
			D target = getTarget(targetEntry, targetId);
			field.getAccessor().setValue(source, target);
		}
		sourceAdapter.update(source, getSaveQueryAdapter(fieldName));
	}

	@Override
	public void setRelations(T source, Iterable<J> targetIds, String fieldName) {
		RegistryEntry sourceEntry = getSourceEntry();
		ResourceRepositoryAdapter<T, I> sourceAdapter = sourceEntry.getResourceRepository();
		ResourceInformation sourceInformation = getSourceEntry().getResourceInformation();
		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		if (field.hasIdField()) {
			field.getIdAccessor().setValue(source, targetIds);
		}
		else {
			RegistryEntry targetEntry = getTargetEntry(field);
			Iterable<D> targets = getTargets(targetEntry, targetIds);
			field.getAccessor().setValue(source, targets);
		}
		sourceAdapter.update(source, getSaveQueryAdapter(fieldName));
	}

	@Override
	public void addRelations(T source, Iterable<J> targetIds, String fieldName) {
		RegistryEntry sourceEntry = getSourceEntry();
		ResourceRepositoryAdapter<T, I> sourceAdapter = sourceEntry.getResourceRepository();
		ResourceInformation sourceInformation = getSourceEntry().getResourceInformation();
		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		if (field.hasIdField()) {
			Collection currentIds = (Collection) field.getIdAccessor().getValue(source);
			currentIds.addAll((Collection) targetIds);
		}
		else {
			RegistryEntry targetEntry = getTargetEntry(field);
			Iterable<D> targets = getTargets(targetEntry, targetIds);
			@SuppressWarnings("unchecked")
			Collection<D> currentTargets = getOrCreateCollection(source, fieldName);
			for (D target : targets) {
				currentTargets.add(target);
			}
		}
		sourceAdapter.update(source, getSaveQueryAdapter(fieldName));
	}

	@Override
	public void removeRelations(T source, Iterable<J> targetIds, String fieldName) {
		RegistryEntry sourceEntry = getSourceEntry();
		ResourceRepositoryAdapter<T, I> sourceAdapter = sourceEntry.getResourceRepository();
		ResourceInformation sourceInformation = getSourceEntry().getResourceInformation();
		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		if (field.hasIdField()) {
			Collection currentIds = (Collection) field.getIdAccessor().getValue(source);
			currentIds.removeAll((Collection) targetIds);
		}
		else {
			RegistryEntry targetEntry = getTargetEntry(field);
			Iterable<D> targets = getTargets(targetEntry, targetIds);
			@SuppressWarnings("unchecked")
			Collection<D> currentTargets = getOrCreateCollection(source, fieldName);
			for (D target : targets) {
				currentTargets.remove(target);
			}
		}
		sourceAdapter.update(source, getSaveQueryAdapter(fieldName));
	}


	protected QueryAdapter getSaveQueryAdapter(String fieldName) {
		QuerySpec querySpec = newSourceQuerySpec();
		querySpec.includeRelation(Arrays.asList(fieldName));
		return new QuerySpecAdapter(querySpec, resourceRegistry);
	}

	private QuerySpec newSourceQuerySpec() {
		return new QuerySpec(sourceResourceClass, sourceResourceType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Collection<D> getOrCreateCollection(Object source, String fieldName) {
		Object property = PropertyUtils.getProperty(source, fieldName);
		if (property == null) {
			Class<?> propertyClass = PropertyUtils.getPropertyClass(source.getClass(), fieldName);
			boolean isList = List.class.isAssignableFrom(propertyClass);
			property = isList ? new ArrayList() : new HashSet();
			PropertyUtils.setProperty(source, fieldName, property);
		}
		return (Collection<D>) property;
	}

	@Deprecated
	protected Iterable<D> getTargets(Iterable<J> targetIds) {
		RegistryEntry entry = resourceRegistry.getEntry(targetResourceClass);
		return getTargets(entry, targetIds);
	}

	@Deprecated
	protected D getTargets(J targetId) {
		RegistryEntry entry = resourceRegistry.getEntry(targetResourceClass);
		return getTarget(entry, targetId);
	}


	@SuppressWarnings("unchecked")
	protected D getTarget(RegistryEntry entry, J targetId) {
		if (targetId == null) {
			return null;
		}
		ResourceRepositoryAdapter<D, J> targetAdapter = entry.getResourceRepository();
		QueryAdapter queryAdapter = new QuerySpecAdapter(new QuerySpec(entry.getResourceInformation()), resourceRegistry);
		D target = (D) targetAdapter.findOne(targetId, queryAdapter).getEntity();
		if (target == null) {
			throw new IllegalStateException(targetId + " not found");
		}
		return target;
	}

	@SuppressWarnings("unchecked")
	protected Iterable<D> getTargets(RegistryEntry entry, Iterable<J> targetIds) {
		ResourceRepositoryAdapter<D, J> targetAdapter = entry.getResourceRepository();
		QueryAdapter queryAdapter = new QuerySpecAdapter(new QuerySpec(entry.getResourceInformation()), resourceRegistry);
		return (Iterable<D>) targetAdapter.findAll(targetIds, queryAdapter).getEntity();
	}

	@SuppressWarnings("unchecked")
	public MultivaluedMap<I, D> findTargets(Iterable<I> sourceIds, String fieldName, QuerySpec querySpec) {
		RegistryEntry sourceEntry = resourceRegistry.findEntry(sourceResourceClass);
		ResourceInformation sourceInformation = sourceEntry.getResourceInformation();

		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		RegistryEntry targetEntry = getTargetEntry(field);

		String oppositeName = getOppositeName(fieldName);
		QuerySpec idQuerySpec = querySpec.duplicate();
		idQuerySpec.addFilter(
				new FilterSpec(Arrays.asList(oppositeName, sourceInformation.getIdField().getUnderlyingName()), FilterOperator
						.EQ, sourceIds));
		idQuerySpec.includeRelation(Arrays.asList(oppositeName));

		ResourceRepositoryAdapter<D, J> targetAdapter = targetEntry.getResourceRepository();
		JsonApiResponse response = targetAdapter.findAll(new QuerySpecAdapter(idQuerySpec, resourceRegistry));
		List<D> results = (List<D>) response.getEntity();

		MultivaluedMap<I, D> bulkResult = new MultivaluedMap<I, D>() {

			@Override
			protected List<D> newList() {
				return new DefaultResourceList<>();
			}
		};

		Set<I> sourceIdSet = new HashSet<>();
		for (I sourceId : sourceIds) {
			sourceIdSet.add(sourceId);
		}

		for (D result : results) {
			handleTarget(bulkResult, result, sourceIdSet, oppositeName, sourceInformation);
		}
		return bulkResult;
	}

	@SuppressWarnings("unchecked")
	private void handleTarget(MultivaluedMap<I, D> bulkResult, D result, Set<I> sourceIdSet, String oppositeName,
			ResourceInformation sourceInformation) {
		Object property = PropertyUtils.getProperty(result, oppositeName);
		if (property == null) {
			throw new IllegalStateException("field " + oppositeName + " is null for " + result
					+ ", make sure to properly implement relationship inclusions");
		}
		if (property instanceof Iterable) {
			for (T potentialSource : (Iterable<T>) property) {
				I sourceId = (I) sourceInformation.getId(potentialSource);
				if (sourceId == null) {
					throw new IllegalStateException("id is null for " + potentialSource);
				}
				// for to-many relations we have to assigned the found resource
				// to all matching sources
				if (sourceIdSet.contains(sourceId)) {
					bulkResult.add(sourceId, result);
				}
			}
		}
		else {
			T source = (T) property;
			I sourceId = (I) sourceInformation.getId(source);
			PreconditionUtil.assertTrue("filtering not properly implemented in resource repository", sourceIdSet.contains
					(sourceId));
			if (sourceId == null) {
				throw new IllegalStateException("id is null for " + source);
			}
			bulkResult.add(sourceId, result);
		}
	}

	protected String getOppositeName(String fieldName) {
		RegistryEntry entry = resourceRegistry.findEntry(sourceResourceClass);
		ResourceInformation resourceInformation = entry.getResourceInformation();
		ResourceField field = resourceInformation.findRelationshipFieldByName(fieldName);
		if (field == null) {
			throw new IllegalStateException("field " + sourceResourceClass.getSimpleName() + "." + fieldName + " not found");
		}
		String oppositeName = field.getOppositeName();
		if (oppositeName == null) {
			throw new IllegalStateException(
					"no opposite specified for field " + sourceResourceClass.getSimpleName() + "." + fieldName);
		}
		return oppositeName;
	}


	protected RegistryEntry getSourceEntry() {
		if (sourceResourceType != null) {
			return resourceRegistry.getEntry(sourceResourceType);
		}
		return resourceRegistry.findEntry(sourceResourceClass);
	}

	protected RegistryEntry getTargetEntry(ResourceField field) {
		return resourceRegistry.getEntry(field.getOppositeResourceType());
	}

	@Override
	public Class<T> getSourceResourceClass() {
		return sourceResourceClass;
	}

	@Override
	public Class<D> getTargetResourceClass() {
		return targetResourceClass;
	}

	@Override
	public void setResourceRegistry(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}
}
