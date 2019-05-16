package io.crnk.core.repository.foward.strategy;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

public class SetOppositeStrategy<T, I , D, J >
        extends ForwardingStrategyBase implements ForwardingSetStrategy<T, I, D, J> {


    private SetOwnerStrategy oppositeSetStrategy = new SetOwnerStrategy();

    @Override
    public void init(ForwardingStrategyContext context) {
        super.init(context);
        oppositeSetStrategy.init(context);
    }

    @Override
    public void setRelation(T source, J relatedId, String fieldName, QueryContext queryContext) {
        J targetId = relatedId;
        boolean add = relatedId != null;
        boolean exists = true;
        if (!add) {
            ResourceInformation resourceInformation = context.getSourceEntry().getResourceInformation();
            ResourceField field = resourceInformation.findFieldByUnderlyingName(fieldName);
            if (field.hasIdField()) {
                targetId = (J) field.getIdAccessor().getValue(source);
            } else {
                Object target = field.getAccessor().getValue(source);
                exists = target != null;
                if(exists) {
                    ResourceInformation targetInformation = context.getTargetEntry(field).getResourceInformation();
                    targetId = (J) targetInformation.getId(target);
                }
            }
        }
        if (exists) {
            updateRelations(source, Arrays.asList(targetId), fieldName, queryContext, add);
        }
    }

    @Override
    public void setRelations(T source, Collection<J> targetIds, String fieldName, QueryContext queryContext) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void addRelations(T source, Collection<J> targetIds, String fieldName, QueryContext queryContext) {
        updateRelations(source, targetIds, fieldName, queryContext, true);
    }

    @Override
    public void removeRelations(T source, Collection<J> targetIds, String fieldName, QueryContext queryContext) {
        updateRelations(source, targetIds, fieldName, queryContext, false);
    }


    private void updateRelations(T source, Collection<J> targetIds, String fieldName, QueryContext queryContext, boolean add) {
        ResourceInformation sourceInformation = context.getSourceEntry().getResourceInformation();

        ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
        PreconditionUtil.verify(field != null, "field not found: %s.%s", sourceInformation.getResourceType(), fieldName);
        RegistryEntry targetEntry = context.getTargetEntry(field);
        ResourceField oppositeField = getOppositeField(field);
        ResourceFieldAccessor oppositeAccessor = oppositeField.getAccessor();
        ResourceRepositoryAdapter targetRepository = targetEntry.getResourceRepository();

        QueryAdapter queryAdapter = context.createEmptyQueryAdapter(targetEntry, queryContext);

        Collection<Object> targets = context.findAll(targetEntry, targetIds, queryContext);
        for (Object target : targets) {
            // in contract to SetOwnerStrategy no need to honor idAccessor since source already loaded
            if (oppositeField.isCollection()) {
                Collection oppositeElements = (Collection) oppositeAccessor.getValue(target);
                if (add) {
                    oppositeElements.add(source);
                } else {
                    oppositeElements.remove(source);
                }
                targetRepository.update(target, queryAdapter);
            } else {
                oppositeAccessor.setValue(target, add ? source : null);
                targetRepository.update(target, queryAdapter);
            }
        }
    }

    private ResourceField getOppositeField(ResourceField field) {
        String oppositeName = field.getOppositeName();
        PreconditionUtil.verify(oppositeName != null, "no opposite field set for %s.%s", field.getParentResourceInformation().getResourceType(), field.getUnderlyingName());
        RegistryEntry targetEntry = context.getTargetEntry(field);
        return targetEntry.getResourceInformation().findFieldByUnderlyingName(oppositeName);
    }
}
