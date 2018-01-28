package io.crnk.core.engine.internal.repository;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.BulkRelationshipRepositoryV2;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;

/**
 * Implements RelationshipRepository for relationships making use of @JsonApiRelationId
 * by delegating to the underlying resource.
 */
public class ImplicitIdBasedRelationshipRepository<T, I extends Serializable, D, J extends Serializable>
		implements BulkRelationshipRepositoryV2<T, I, D, J> {

	private final ResourceInformation sourceInformation;

	private final Class targetResourceClass;

	private ResourceRegistry resourceRegistry;

	public ImplicitIdBasedRelationshipRepository(ResourceRegistry resourceRegistry, ResourceInformation sourceInformation,
			Class targetResourceClass) {
		this.sourceInformation = sourceInformation;
		this.resourceRegistry = resourceRegistry;
		this.targetResourceClass = targetResourceClass;
	}

	@Override
	public D findOneTarget(I sourceId, String fieldName, QuerySpec querySpec) {
		MultivaluedMap<I, D> map = findTargets(Arrays.asList(sourceId), fieldName, querySpec);
		return map.getUnique(sourceId, true);
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
		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		ResourceRepositoryAdapter<T, I> sourceAdapter = getSourceAdapter(field);
		field.getIdAccessor().setValue(source, targetId);
		sourceAdapter.update(source, getSaveQueryAdapter(fieldName));
	}

	@Override
	public void setRelations(T source, Iterable<J> targetIds, String fieldName) {
		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		ResourceRepositoryAdapter<T, I> sourceAdapter = getSourceAdapter(field);
		field.getIdAccessor().setValue(source, targetIds);
		sourceAdapter.update(source, getSaveQueryAdapter(fieldName));
	}

	@Override
	public void addRelations(T source, Iterable<J> targetIds, String fieldName) {
		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		ResourceRepositoryAdapter<T, I> sourceAdapter = getSourceAdapter(field);
		Collection currentIds = (Collection) field.getIdAccessor().getValue(source);
		currentIds.addAll((Collection) targetIds);
		sourceAdapter.update(source, getSaveQueryAdapter(fieldName));
	}

	@Override
	public void removeRelations(T source, Iterable<J> targetIds, String fieldName) {
		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		ResourceRepositoryAdapter<T, I> sourceAdapter = getSourceAdapter(field);
		Collection currentIds = (Collection) field.getIdAccessor().getValue(source);
		currentIds.removeAll((Collection) targetIds);
		sourceAdapter.update(source, getSaveQueryAdapter(fieldName));
	}

	protected QueryAdapter getSaveQueryAdapter(String fieldName) {
		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		QuerySpec querySpec = new QuerySpec(field.getParentResourceInformation().getResourceType());
		querySpec.includeRelation(Arrays.asList(fieldName));
		return new QuerySpecAdapter(querySpec, resourceRegistry);
	}


	@SuppressWarnings("unchecked")
	public MultivaluedMap<I, D> findTargets(Iterable<I> sourceIds, String fieldName, QuerySpec querySpec) {
		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		List sources = (List) getSourceAdapter(field).findAll(sourceIds, getSaveQueryAdapter(fieldName)).getEntity();

		Set targetIds = new HashSet();
		for (Object source : sources) {
			Object targetId = field.getIdAccessor().getValue(source);
			if (field.isCollection()) {
				targetIds.addAll((Collection) targetId);
			}
			else {
				targetIds.add(targetId);
			}
		}

		ResourceInformation targetInformation = getTargetInformation(field);

		QuerySpec idQuerySpec = new QuerySpec(targetInformation.getResourceType());
		idQuerySpec.addFilter(
				new FilterSpec(Arrays.asList(targetInformation.getIdField().getUnderlyingName()), FilterOperator.EQ, targetIds));

		ResourceRepositoryAdapter<D, J> targetAdapter = getTargetAdapter(field);
		JsonApiResponse response = targetAdapter.findAll(new QuerySpecAdapter(idQuerySpec, resourceRegistry));
		List<D> targets = (List<D>) response.getEntity();

		return toResult(fieldName, targetInformation, sources, targets);
	}

	private MultivaluedMap<I, D> toResult(String fieldName, ResourceInformation targetInformation,
			List sources,
			List<D> targets) {

		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		MultivaluedMap bulkResult = new MultivaluedMap<I, D>() {

			@Override
			protected List<D> newList() {
				return new DefaultResourceList<>();
			}
		};

		Map targetMap = new HashMap();
		for (D target : targets) {
			Object targetId = targetInformation.getId(target);
			targetMap.put(targetId, target);
		}

		for (Object source : sources) {
			Object sourceId = sourceInformation.getId(source);
			Object targetId = field.getIdAccessor().getValue(source);
			if (field.isCollection()) {
				for (Object targetElementId : (Collection) targetId) {
					addResult(bulkResult, field, sourceId, targetElementId, targetMap);
				}
			}
			else {
				addResult(bulkResult, field, sourceId, targetId, targetMap);
			}
		}
		return bulkResult;
	}

	private void addResult(MultivaluedMap bulkResult, ResourceField field, Object sourceId, Object targetId, Map targetMap) {
		Object target = targetMap.get(targetId);
		if (target == null) {
			throw new ResourceNotFoundException("targetId=" + targetId + " not found for sourceId=" + sourceId + ", field=" +
					field.getUnderlyingName() + ", sourceType=" + field.getParentResourceInformation().getResourceType());
		}
		bulkResult.add(sourceId, target);
	}

	private ResourceRepositoryAdapter<D, J> getTargetAdapter(ResourceField field) {
		RegistryEntry entry = resourceRegistry.getEntry(field.getOppositeResourceType());
		return entry.getResourceRepository(null);
	}

	private ResourceRepositoryAdapter<T, I> getSourceAdapter(ResourceField field) {
		RegistryEntry entry = resourceRegistry.getEntry(field.getParentResourceInformation().getResourceType());
		return entry.getResourceRepository(null);
	}

	@Override
	public Class<T> getSourceResourceClass() {
		return (Class<T>) sourceInformation.getResourceClass();
	}

	@Override
	public Class<D> getTargetResourceClass() {
		return targetResourceClass;
	}

	private ResourceInformation getTargetInformation(ResourceField field) {
		return resourceRegistry.getEntry(field.getOppositeResourceType()).getResourceInformation();
	}
}
