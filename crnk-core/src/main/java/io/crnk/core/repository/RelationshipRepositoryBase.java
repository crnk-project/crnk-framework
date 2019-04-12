package io.crnk.core.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.crnk.core.engine.http.HttpRequestContextAware;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryAware;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.foward.ForwardingRelationshipRepository;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.annotations.JsonApiToMany;
import io.crnk.core.resource.annotations.JsonApiToOne;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;

/**
 * Recommended base class to implement a relationship repository making use of
 * the QuerySpec and ResourceList. Note that the former
 * {@link RelationshipRepositoryBase} will be removed in the near
 * future.
 * <p>
 * Base implementation for {@link RelationshipRepository} implementing
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
 * @deprecated use {@link ForwardingRelationshipRepository}
 */
@Deprecated
public class RelationshipRepositoryBase<T, I , D, J >
		implements BulkRelationshipRepository<T, I, D, J>, ResourceRegistryAware, HttpRequestContextAware {


	protected ResourceRegistry resourceRegistry;

	private Class<T> sourceResourceClass;

	private String sourceResourceType;

	private Class targetResourceClass;

	private String targetResourceType;

	private HttpRequestContextProvider requestContextProvider;


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
		ResourceRepositoryAdapter sourceAdapter = sourceEntry.getResourceRepository();
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
	public void setRelations(T source, Collection<J> targetIds, String fieldName) {
		RegistryEntry sourceEntry = getSourceEntry();
		ResourceRepositoryAdapter sourceAdapter = sourceEntry.getResourceRepository();
		ResourceInformation sourceInformation = getSourceEntry().getResourceInformation();
		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		if (field.hasIdField()) {
			field.getIdAccessor().setValue(source, targetIds);
		}
		else {
			RegistryEntry targetEntry = getTargetEntry(field);
			Collection<D> targets = getTargets(targetEntry, targetIds);
			field.getAccessor().setValue(source, targets);
		}
		sourceAdapter.update(source, getSaveQueryAdapter(fieldName));
	}

	@Override
	public void addRelations(T source, Collection<J> targetIds, String fieldName) {
		RegistryEntry sourceEntry = getSourceEntry();
		ResourceRepositoryAdapter sourceAdapter = sourceEntry.getResourceRepository();
		ResourceInformation sourceInformation = getSourceEntry().getResourceInformation();
		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		if (field.hasIdField()) {
			Collection currentIds = (Collection) field.getIdAccessor().getValue(source);
			currentIds.addAll(targetIds);
		}
		else {
			RegistryEntry targetEntry = getTargetEntry(field);
			Collection<D> targets = getTargets(targetEntry, targetIds);
			@SuppressWarnings("unchecked")
			Collection<D> currentTargets = getOrCreateCollection(source, fieldName);
			for (D target : targets) {
				currentTargets.add(target);
			}
		}
		sourceAdapter.update(source, getSaveQueryAdapter(fieldName));
	}

	@Override
	public void removeRelations(T source, Collection<J> targetIds, String fieldName) {
		RegistryEntry sourceEntry = getSourceEntry();
		ResourceRepositoryAdapter sourceAdapter = sourceEntry.getResourceRepository();
		ResourceInformation sourceInformation = getSourceEntry().getResourceInformation();
		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		if (field.hasIdField()) {
			Collection currentIds = (Collection) field.getIdAccessor().getValue(source);
			currentIds.removeAll(targetIds);
		}
		else {
			RegistryEntry targetEntry = getTargetEntry(field);
			Collection<D> targets = getTargets(targetEntry, targetIds);
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
		QueryContext queryContext = requestContextProvider.getRequestContext().getQueryContext();
		return new QuerySpecAdapter(querySpec, resourceRegistry, queryContext);
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
	protected Collection<D> getTargets(Collection<J> targetIds) {
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
		ResourceRepositoryAdapter targetAdapter = entry.getResourceRepository();
		QueryContext queryContext = requestContextProvider.getRequestContext().getQueryContext();
		QueryAdapter queryAdapter =
				new QuerySpecAdapter(new QuerySpec(entry.getResourceInformation()), resourceRegistry, queryContext);
		D target = (D) targetAdapter.findOne(targetId, queryAdapter).get().getEntity();
		PreconditionUtil.assertNotNull("related resource not found", target);
		return target;
	}

	@SuppressWarnings("unchecked")
	protected Collection<D> getTargets(RegistryEntry entry, Collection<J> targetIds) {
		ResourceRepositoryAdapter targetAdapter = entry.getResourceRepository();
		QueryContext queryContext = requestContextProvider.getRequestContext().getQueryContext();
		QueryAdapter queryAdapter =
				new QuerySpecAdapter(new QuerySpec(entry.getResourceInformation()), resourceRegistry, queryContext);
		return (Collection<D>) targetAdapter.findAll(targetIds, queryAdapter).get().getEntity();
	}

	@SuppressWarnings("unchecked")
	public MultivaluedMap<I, D> findTargets(Collection<I> sourceIds, String fieldName, QuerySpec querySpec) {
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

		ResourceRepositoryAdapter targetAdapter = targetEntry.getResourceRepository();
		QueryContext queryContext = requestContextProvider.getRequestContext().getQueryContext();
		JsonApiResponse response =
				targetAdapter.findAll(new QuerySpecAdapter(idQuerySpec, resourceRegistry, queryContext)).get();
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
			bulkResult.set(sourceId, new DefaultResourceList<>());
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

		if (property instanceof Collection) {
			for (T potentialSource : (Collection<T>) property) {
				I sourceId = (I) sourceInformation.getId(potentialSource);
				PreconditionUtil.assertNotNull("id must not be null", sourceId);

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
			PreconditionUtil.assertNotNull("id must not be null", sourceId);
			bulkResult.add(sourceId, result);
		}
	}

	protected String getOppositeName(String fieldName) {
		RegistryEntry entry = resourceRegistry.findEntry(sourceResourceClass);
		ResourceInformation resourceInformation = entry.getResourceInformation();
		ResourceField field = resourceInformation.findFieldByUnderlyingName(fieldName);
		PreconditionUtil.verify(field != null, "field %s.%s not found", resourceInformation.getResourceType(), fieldName);
		PreconditionUtil.verify(field.getResourceFieldType() == ResourceFieldType.RELATIONSHIP,
				"field %s.%s must be a relationship to be referenced by other relationship",
				resourceInformation.getResourceType(), fieldName);
		String oppositeName = field.getOppositeName();
		PreconditionUtil.verify(oppositeName != null,"opposite not specified for %s.%s", resourceInformation.getResourceType(), fieldName);
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

	@Override
	public void setHttpRequestContextProvider(HttpRequestContextProvider requestContextProvider) {
		this.requestContextProvider = requestContextProvider;
	}
}
