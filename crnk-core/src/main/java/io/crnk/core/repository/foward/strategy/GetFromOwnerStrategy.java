package io.crnk.core.repository.foward.strategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.internal.utils.ResourceUtils;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.list.DefaultResourceList;

public class GetFromOwnerStrategy<T, I, D, J> extends ForwardingStrategyBase
		implements ForwardingGetStrategy<T, I, D, J> {


	@SuppressWarnings("unchecked")
	public MultivaluedMap<I, D> findTargets(Collection<I> sourceIds, String fieldName, QuerySpec querySpec, QueryContext queryContext) {
		RegistryEntry sourceEntry = context.getSourceEntry();
		ResourceInformation sourceInformation = sourceEntry.getResourceInformation();
		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		RegistryEntry targetEntry = context.getTargetEntry(field);

		List sources = (List) sourceEntry.getResourceRepository().findAll(sourceIds,
				context.createSaveQueryAdapter(fieldName, queryContext)).get().getEntity();

		ResourceInformation targetInformation = targetEntry.getResourceInformation();

		List<D> targets;
		if (field.hasIdField()) {
			Set targetIds = new HashSet();
			for (Object source : sources) {
				Object targetId = field.getIdAccessor().getValue(source);
				if(targetId == null){
					continue; // null relationship
				}
				if (field.isCollection()) {
					targetIds.addAll((Collection) targetId);
				} else {
					targetIds.add(targetId);
				}
			}

			if(!targetIds.isEmpty()) {
				ResourceRepositoryAdapter targetAdapter = targetEntry.getResourceRepository();
				JsonApiResponse response = targetAdapter.findAll(targetIds, context.createQueryAdapter(querySpec, queryContext)).get();
				targets = (List<D>) response.getEntity();
				return toResult(field, targetInformation, sources, targets);
			}else{
				return newResultMap();
			}
		} else {
			MultivaluedMap bulkResult = newResultMap();
			for (Object source : sources) {
				Object sourceId = sourceInformation.getId(source);

				Object target = field.getAccessor().getValue(source);
				if (target != null) {
					if (field.isCollection()) {
						bulkResult.addAll(sourceId, querySpec.apply((Collection) target));
					} else {
						bulkResult.add(sourceId, target);
					}
				}
			}
			return bulkResult;
		}
	}

	private MultivaluedMap newResultMap() {
		return new MultivaluedMap<I, D>() {

			@Override
			protected List<D> newList() {
				return new DefaultResourceList<>();
			}
		};
	}

	private MultivaluedMap<I, D> toResult(ResourceField field, ResourceInformation targetInformation,
			List sources,
			List<D> targets) {

		MultivaluedMap bulkResult = newResultMap();
		Map targetMap = new HashMap();
		for (D target : targets) {
			Object targetId = targetInformation.getId(target);
			targetMap.put(targetId, target);
		}

		for (Object source : sources) {
			Object sourceId = field.getResourceInformation().getId(source);

			Object targetId = field.getIdAccessor().getValue(source);
			if (field.isCollection()) {
				Collection targetIds = ResourceUtils.toTypedIds(targetInformation, ((Collection) targetId));
				targetIds.retainAll(targetMap.keySet());
				for (Object targetElementId : targetIds) {
					addResult(bulkResult, field, sourceId, targetElementId, targetMap);
				}
			} else if (targetId != null) {
				targetId = ResourceUtils.toTypedId(targetInformation, targetId);
				addResult(bulkResult, field, sourceId, targetId, targetMap);
			} else {
				bulkResult.add(sourceId, null);
			}
		}
		return bulkResult;
	}

	private void addResult(MultivaluedMap bulkResult, ResourceField field, Object sourceId, Object targetId, Map targetMap) {
		Object target = targetMap.get(targetId);
		if (target == null) {
			throw new ResourceNotFoundException("targetId=" + targetId + " not found for sourceId=" + sourceId + ", field=" +
					field.getUnderlyingName() + ", sourceType=" + field.getResourceInformation().getResourceType());
		}
		bulkResult.add(sourceId, target);
	}
}
