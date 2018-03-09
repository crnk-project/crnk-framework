package io.crnk.core.repository.foward.strategy;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.list.DefaultResourceList;

/**
 * Looks up related resources by querying the related/opposite resource repository. A filter based on the opposite relationship
 * is applied to that request. Instead of requesting the related resources B of A, it will query all B that are related A.
 * For this, {@link JsonApiRelation#opposite()} must be defined.<p>
 * <p>
 * </p>For example, consider a setup with a Task and a Project resource. Each has a project respectively tasks relationship to
 * the other resource. To fetch all tasks of a project, this strategy will use a filter <i>project.id EQ xy</i> since
 * project is the opposite relationship to tasks.
 */
public class GetFromOppositeStrategy<T, I extends Serializable, D, J extends Serializable>
		extends ForwardingStrategyBase implements ForwardingGetStrategy<T, I, D, J> {


	@SuppressWarnings("unchecked")
	public MultivaluedMap<I, D> findTargets(Iterable<I> sourceIds, String fieldName, QuerySpec querySpec, QueryContext queryContext) {
		RegistryEntry sourceEntry = context.getSourceEntry();
		ResourceInformation sourceInformation = sourceEntry.getResourceInformation();

		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		RegistryEntry targetEntry = context.getTargetEntry(field);
		ResourceInformation targetInformation = targetEntry.getResourceInformation();
		ResourceField oppositeField =
				Objects.requireNonNull(targetInformation.findFieldByUnderlyingName(field.getOppositeName()));

		QuerySpec idQuerySpec = querySpec.duplicate();
		idQuerySpec.addFilter(
				new FilterSpec(
						Arrays.asList(oppositeField.getUnderlyingName(), sourceInformation.getIdField().getUnderlyingName()),
						FilterOperator.EQ, sourceIds));
		idQuerySpec.includeRelation(Arrays.asList(oppositeField.getUnderlyingName()));

		ResourceRepositoryAdapter targetAdapter = targetEntry.getResourceRepository();
		JsonApiResponse response = targetAdapter.findAll(context.createQueryAdapter(idQuerySpec, queryContext)).get();
		Collection<D> results = (Collection<D>) response.getEntity();

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
			handleTarget(bulkResult, result, sourceIdSet, oppositeField, sourceInformation);
		}
		return bulkResult;
	}

	@SuppressWarnings("unchecked")
	private void handleTarget(MultivaluedMap<I, D> bulkResult, D result, Set<I>
			sourceIdSet, ResourceField oppositeField, ResourceInformation sourceInformation) {

		Object property = oppositeField.getAccessor().getValue(result);
		if (property == null) {
			throw new IllegalStateException("field " + oppositeField.getUnderlyingName() + " is null for " + result
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
		} else {
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

}
