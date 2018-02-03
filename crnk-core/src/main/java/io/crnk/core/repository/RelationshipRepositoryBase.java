package io.crnk.core.repository;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
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
import java.util.*;

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

	private ResourceRegistry resourceRegistry;

	private Class<D> targetResourceClass;

	private Class<T> sourceResourceClass;

	/**
	 * default constructor for CDI an other DI libraries
	 */
	protected RelationshipRepositoryBase() {
	}

	protected RelationshipRepositoryBase(Class<T> sourceResourceClass, Class<D> targetResourceClass) {
		this.sourceResourceClass = sourceResourceClass;
		this.targetResourceClass = targetResourceClass;
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
		ResourceRepositoryAdapter<T, I> sourceAdapter = getSourceAdapter();
		D target = getTarget(targetId);
		PropertyUtils.setProperty(source, fieldName, target);
		sourceAdapter.update(source, getSaveQueryAdapter(fieldName));
	}

	protected QueryAdapter getSaveQueryAdapter(String fieldName) {
		QuerySpec querySpec = new QuerySpec(sourceResourceClass);
		querySpec.includeRelation(Arrays.asList(fieldName));
		return new QuerySpecAdapter(querySpec, resourceRegistry);
	}

	@Override
	public void setRelations(T source, Iterable<J> targetIds, String fieldName) {
		ResourceRepositoryAdapter<T, I> sourceAdapter = getSourceAdapter();
		Iterable<D> targets = getTargets(targetIds);
		PropertyUtils.setProperty(source, fieldName, targets);
		sourceAdapter.update(source, getSaveQueryAdapter(fieldName));
	}

	@Override
	public void addRelations(T source, Iterable<J> targetIds, String fieldName) {
		ResourceRepositoryAdapter<T, I> sourceAdapter = getSourceAdapter();
		Iterable<D> targets = getTargets(targetIds);
		@SuppressWarnings("unchecked")
		Collection<D> currentTargets = getOrCreateCollection(source, fieldName);
		for (D target : targets) {
			currentTargets.add(target);
		}
		sourceAdapter.update(source, getSaveQueryAdapter(fieldName));
	}

	@Override
	public void removeRelations(T source, Iterable<J> targetIds, String fieldName) {
		ResourceRepositoryAdapter<T, I> sourceAdapter = getSourceAdapter();
		Iterable<D> targets = getTargets(targetIds);
		@SuppressWarnings("unchecked")
		Collection<D> currentTargets = getOrCreateCollection(source, fieldName);
		for (D target : targets) {
			currentTargets.remove(target);
		}
		sourceAdapter.update(source, getSaveQueryAdapter(fieldName));
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

	@SuppressWarnings("unchecked")
	protected D getTarget(J targetId) {
		if (targetId == null) {
			return null;
		}
		ResourceRepositoryAdapter<D, J> targetAdapter = getTargetAdapter();
		QueryAdapter queryAdapter = new QuerySpecAdapter(new QuerySpec(targetResourceClass), resourceRegistry);
		D target = (D) targetAdapter.findOne(targetId, queryAdapter).getEntity();
		if (target == null) {
			throw new IllegalStateException(targetId + " not found");
		}
		return target;
	}

	@SuppressWarnings("unchecked")
	protected Iterable<D> getTargets(Iterable<J> targetIds) {
		ResourceRepositoryAdapter<D, J> targetAdapter = getTargetAdapter();
		QueryAdapter queryAdapter = new QuerySpecAdapter(new QuerySpec(targetResourceClass), resourceRegistry);
		return (Iterable<D>) targetAdapter.findAll(targetIds, queryAdapter).getEntity();
	}

	@SuppressWarnings("unchecked")
	public MultivaluedMap<I, D> findTargets(Iterable<I> sourceIds, String fieldName, QuerySpec querySpec) {
		RegistryEntry sourceEntry = resourceRegistry.findEntry(sourceResourceClass);
		ResourceInformation sourceInformation = sourceEntry.getResourceInformation();

		String oppositeName = getOppositeName(fieldName);
		QuerySpec idQuerySpec = querySpec.duplicate();
		idQuerySpec.addFilter(
				new FilterSpec(Arrays.asList(oppositeName, sourceInformation.getIdField().getUnderlyingName()), FilterOperator.EQ,
						sourceIds));
		idQuerySpec.includeRelation(Arrays.asList(oppositeName));

		ResourceRepositoryAdapter<D, J> targetAdapter = getTargetAdapter();
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

	private ResourceRepositoryAdapter<D, J> getTargetAdapter() {
		RegistryEntry entry = resourceRegistry.findEntry(targetResourceClass);
		return entry.getResourceRepository(null);
	}

	private ResourceRepositoryAdapter<T, I> getSourceAdapter() {
		RegistryEntry entry = resourceRegistry.findEntry(sourceResourceClass);
		return entry.getResourceRepository(null);
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
