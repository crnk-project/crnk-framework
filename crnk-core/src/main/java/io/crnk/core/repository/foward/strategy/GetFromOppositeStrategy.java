package io.crnk.core.repository.foward.strategy;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
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
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.links.PagedLinksInformation;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
    public MultivaluedMap<I, D> findTargets(Collection<I> sourceIds, String fieldName, QuerySpec querySpec,
                                            QueryContext queryContext) {
        RegistryEntry sourceEntry = context.getSourceEntry();
        ResourceInformation sourceInformation = sourceEntry.getResourceInformation();

        ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
        RegistryEntry targetEntry = context.getTargetEntry(field);
        ResourceInformation targetInformation = targetEntry.getResourceInformation();
        ResourceField oppositeField =
                Objects.requireNonNull(targetInformation.findFieldByUnderlyingName(field.getOppositeName()));

        QuerySpec idQuerySpec = querySpec.clone();
        idQuerySpec.addFilter(
                new FilterSpec(
                        Arrays.asList(oppositeField.getUnderlyingName(), sourceInformation.getIdField().getUnderlyingName()),
                        FilterOperator.EQ, sourceIds));
        idQuerySpec.includeRelation(Arrays.asList(oppositeField.getUnderlyingName()));

        ResourceRepositoryAdapter targetAdapter = targetEntry.getResourceRepository();
        JsonApiResponse response = targetAdapter.findAll(context.createQueryAdapter(idQuerySpec, queryContext)).get();
        Collection<D> results = (Collection<D>) response.getEntity();

        Iterator<I> iterator = sourceIds.iterator();
        if (iterator.hasNext()) {
            I sourceId = iterator.next();
            if (!iterator.hasNext()) {
                // query for single source, maintain links and meta information
                MultivaluedMap<I, D> bulkResult = new MultivaluedMap<I, D>();
                if (results instanceof ResourceList) {
                    bulkResult.set(sourceId, (List<D>) results);
                    ResourceList resourceList = (ResourceList) results;
                    LinksInformation links = resourceList.getLinks();
                    if (links instanceof PagedLinksInformation) {
                        PagedLinksInformation pagedLinksInformation = (PagedLinksInformation) links;
                        pagedLinksInformation.setFirst(null);
                        pagedLinksInformation.setLast(null);
                        pagedLinksInformation.setNext(null);
                        pagedLinksInformation.setPrev(null);
                    }
                    return bulkResult;
                }
            }
        }

        // multiple sources, page and links information will be lost
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


        if (Collection.class.isAssignableFrom(oppositeField.getType())) {
            handleCollectionTarget(bulkResult, result, sourceIdSet, oppositeField, sourceInformation);
        } else {
            handleSingleTarget(bulkResult, result, sourceIdSet, oppositeField, sourceInformation);
        }
    }

    private void handleSingleTarget(MultivaluedMap<I, D> bulkResult, D result, Set<I> sourceIdSet, ResourceField oppositeField,
                                    ResourceInformation sourceInformation) {
        I sourceId;
        if (oppositeField.hasIdField()) {
            ResourceFieldAccessor idAccessor = oppositeField.getIdAccessor();
            sourceId = (I) idAccessor.getValue(result);
            if (sourceId == null) {
                throw new IllegalStateException("field " + oppositeField.getIdName() + "Id is null for " + result
                        + ". To make use of opposite forwarding behavior for resource lookup, the opposite resource "
                        + "repository "
                        + "must "
                        + "return the relationship or its identifier based "
                        + "on @JsonApiRelationId ");
            }
        } else {
            Object source = oppositeField.getAccessor().getValue(result);
            if (source == null) {
                throw new IllegalStateException("field " + oppositeField.getUnderlyingName() + "Id is null for " + result
                        + ". To make use of opposite forwarding behavior for resource lookup, the opposite resource "
                        + "repository "
                        + "must "
                        + "return the relationship or its identifier based "
                        + "on @JsonApiRelationId ");
            }
            sourceId = (I) sourceInformation.getId(source);
            if (sourceId == null) {
                throw new IllegalStateException("id must not be null for resource " + source);
            }
        }

        PreconditionUtil.verify(sourceIdSet.contains(sourceId),
                "filtering not properly implemented in resource repository, expected sourceId=%s to be contained in %d",
                sourceId, sourceIdSet);

        bulkResult.add(sourceId, result);
    }

    private void handleCollectionTarget(MultivaluedMap<I, D> bulkResult, D result, Set<I> sourceIdSet,
                                        ResourceField oppositeField,
                                        ResourceInformation sourceInformation) {
        Collection<I> sourceIds;
        if (oppositeField.hasIdField()) {
            ResourceFieldAccessor idAccessor = oppositeField.getIdAccessor();
            sourceIds = (Collection<I>) idAccessor.getValue(result);
            if (sourceIds == null) {
                throw new IllegalStateException("field " + oppositeField.getIdName() + "Id is null for " + result
                        + ". To make use of opposite forwarding behavior for resource lookup, the opposite resource "
                        + "repository "
                        + "must "
                        + "return the relationship or its identifier based "
                        + "on @JsonApiRelationId ");
            }
        } else {
            sourceIds = new ArrayList<>();
            Collection property = (Collection) oppositeField.getAccessor().getValue(result);
            if (property == null) {
                throw new IllegalStateException("field " + oppositeField.getUnderlyingName() + "Id is null for " + result
                        + ". To make use of opposite forwarding behavior for resource lookup, the opposite resource "
                        + "repository "
                        + "must "
                        + "return the relationship or its identifier based "
                        + "on @JsonApiRelationId ");
            }

            for (T potentialSource : (Collection<T>) property) {
                I sourceId = (I) sourceInformation.getId(potentialSource);
                if (sourceId == null) {
                    throw new IllegalStateException("id is null for " + potentialSource);
                }
                sourceIds.add(sourceId);
            }
        }

        for (I sourceId : sourceIds) {
            // for to-many relations we have to assigned the found resource
            // to all matching sources
            if (sourceIdSet.contains(sourceId)) {
                bulkResult.add(sourceId, result);
            }
        }
    }

}
