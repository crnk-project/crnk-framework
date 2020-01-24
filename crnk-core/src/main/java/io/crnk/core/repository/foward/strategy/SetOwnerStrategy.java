package io.crnk.core.repository.foward.strategy;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;

import java.util.Collection;

public class SetOwnerStrategy<T, I, D, J>
        extends ForwardingStrategyBase implements ForwardingSetStrategy<T, I, D, J> {

    @Override
    public void setRelation(T source, J targetId, String fieldName, QueryContext queryContext) {
        RegistryEntry sourceEntry = context.getSourceEntry();
        ResourceRepositoryAdapter sourceAdapter = sourceEntry.getResourceRepository();
        ResourceInformation sourceInformation = sourceEntry.getResourceInformation();
        ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
        if (field.hasIdField()) {
            field.getIdAccessor().setValue(source, targetId);
        } else {
            RegistryEntry targetEntry = context.getTargetEntry(field);
            D target = context.findOne(targetEntry, targetId, queryContext);
            field.getAccessor().setValue(source, target);
        }
        sourceAdapter.update(source, context.createSaveQueryAdapter(fieldName, queryContext));
    }

    @Override
    public void setRelations(T source, Collection<J> targetIds, String fieldName, QueryContext queryContext) {
        RegistryEntry sourceEntry = context.getSourceEntry();
        ResourceRepositoryAdapter sourceAdapter = sourceEntry.getResourceRepository();
        ResourceInformation sourceInformation = sourceEntry.getResourceInformation();
        ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
        if (field.hasIdField()) {
            field.getIdAccessor().setValue(source, targetIds);
        } else {
            RegistryEntry targetEntry = context.getTargetEntry(field);
            Collection<D> targets = context.findAll(targetEntry, targetIds, queryContext);
            field.getAccessor().setValue(source, targets);
        }
        sourceAdapter.update(source, context.createSaveQueryAdapter(fieldName, queryContext));
    }

    @Override
    public void addRelations(T source, Collection<J> targetIds, String fieldName, QueryContext queryContext) {
        RegistryEntry sourceEntry = context.getSourceEntry();
        ResourceRepositoryAdapter sourceAdapter = sourceEntry.getResourceRepository();
        ResourceInformation sourceInformation = sourceEntry.getResourceInformation();
        ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
        if (field.hasIdField()) {
            Collection currentIds = (Collection) field.getIdAccessor().getValue(source);
            currentIds.addAll(targetIds);
        } else {
            RegistryEntry targetEntry = context.getTargetEntry(field);
            Collection<D> targets = context.findAll(targetEntry, targetIds, queryContext);
            @SuppressWarnings("unchecked")
            Collection<D> currentTargets = getOrCreateCollection(source, field);
            for (D target : targets) {
                currentTargets.add(target);
            }
        }
        sourceAdapter.update(source, context.createSaveQueryAdapter(fieldName, queryContext));
    }

    @Override
    public void removeRelations(T source, Collection<J> targetIds, String fieldName, QueryContext queryContext) {
        RegistryEntry sourceEntry = context.getSourceEntry();
        ResourceRepositoryAdapter sourceAdapter = sourceEntry.getResourceRepository();
        ResourceInformation sourceInformation = sourceEntry.getResourceInformation();
        ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
        if (field.hasIdField()) {
            Collection currentIds = (Collection) field.getIdAccessor().getValue(source);
            currentIds.removeAll(targetIds);
        } else {
            RegistryEntry targetEntry = context.getTargetEntry(field);
            Collection<D> targets = context.findAll(targetEntry, targetIds, queryContext);
            @SuppressWarnings("unchecked")
            Collection<D> currentTargets = getOrCreateCollection(source, field);
            for (D target : targets) {
                currentTargets.remove(target);
            }
        }
        sourceAdapter.update(source, context.createSaveQueryAdapter(fieldName, queryContext));
    }

}
