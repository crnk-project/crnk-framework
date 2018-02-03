package io.crnk.core.repository.implicit;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.RelationshipRepositoryBase;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.list.DefaultResourceList;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implements RelationshipRepository for relationships making use of @JsonApiRelationId
 * by delegating to the underlying resource. Provides an implementation for
 * {@link io.crnk.core.resource.annotations.RelationshipRepositoryBehavior#IMPLICIT_FROM_OWNER}.
 */
public class ImplicitOwnerBasedRelationshipRepository<T, I extends Serializable, D, J extends Serializable>
		extends RelationshipRepositoryBase<T, I, D, J> {

	/**
	 * default constructor for CDI an other DI libraries
	 */
	protected ImplicitOwnerBasedRelationshipRepository() {
	}

	public ImplicitOwnerBasedRelationshipRepository(Class sourceResourceClass, Class targetResourceClass) {
		super(sourceResourceClass, targetResourceClass);
	}

	public ImplicitOwnerBasedRelationshipRepository(String sourceResourceType, String targetResourceType) {
		super(sourceResourceType, targetResourceType);
	}

	@SuppressWarnings("unchecked")
	public MultivaluedMap<I, D> findTargets(Iterable<I> sourceIds, String fieldName, QuerySpec querySpec) {
		RegistryEntry sourceEntry = getSourceEntry();
		ResourceInformation sourceInformation = sourceEntry.getResourceInformation();

		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		List sources = (List) sourceEntry.getResourceRepository().findAll(sourceIds, getSaveQueryAdapter(fieldName)).getEntity();

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

		ResourceRepositoryAdapter<D, J> targetAdapter = getTargetEntry(field).getResourceRepository();
		JsonApiResponse response = targetAdapter.findAll(new QuerySpecAdapter(idQuerySpec, resourceRegistry));
		List<D> targets = (List<D>) response.getEntity();

		return toResult(fieldName, targetInformation, sources, targets);
	}

	private MultivaluedMap<I, D> toResult(String fieldName, ResourceInformation targetInformation,
			List sources,
			List<D> targets) {

		RegistryEntry sourceEntry = getSourceEntry();
		ResourceInformation sourceInformation = sourceEntry.getResourceInformation();
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

	private ResourceInformation getTargetInformation(ResourceField field) {
		return resourceRegistry.getEntry(field.getOppositeResourceType()).getResourceInformation();
	}
}
