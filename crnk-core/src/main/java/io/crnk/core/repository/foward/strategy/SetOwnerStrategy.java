package io.crnk.core.repository.foward.strategy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;

public class SetOwnerStrategy<T, I extends Serializable, D, J extends Serializable>
		extends ForwardingStrategyBase implements ForwardingSetStrategy<T, I, D, J> {

	@Override
	public void setRelation(T source, J targetId, String fieldName) {
		RegistryEntry sourceEntry = context.getSourceEntry();
		ResourceRepositoryAdapter<T, I> sourceAdapter = sourceEntry.getResourceRepository();
		ResourceInformation sourceInformation = sourceEntry.getResourceInformation();
		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		if (field.hasIdField()) {
			field.getIdAccessor().setValue(source, targetId);
		}
		else {
			RegistryEntry targetEntry = context.getTargetEntry(field);
			D target = context.findOne(targetEntry, targetId);
			field.getAccessor().setValue(source, target);
		}
		sourceAdapter.update(source, context.createSaveQueryAdapter(fieldName));
	}

	@Override
	public void setRelations(T source, Iterable<J> targetIds, String fieldName) {
		RegistryEntry sourceEntry = context.getSourceEntry();
		ResourceRepositoryAdapter<T, I> sourceAdapter = sourceEntry.getResourceRepository();
		ResourceInformation sourceInformation = sourceEntry.getResourceInformation();
		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		if (field.hasIdField()) {
			field.getIdAccessor().setValue(source, targetIds);
		}
		else {
			RegistryEntry targetEntry = context.getTargetEntry(field);
			Iterable<D> targets = context.findAll(targetEntry, targetIds);
			field.getAccessor().setValue(source, targets);
		}
		sourceAdapter.update(source, context.createSaveQueryAdapter(fieldName));
	}

	@Override
	public void addRelations(T source, Iterable<J> targetIds, String fieldName) {
		RegistryEntry sourceEntry = context.getSourceEntry();
		ResourceRepositoryAdapter<T, I> sourceAdapter = sourceEntry.getResourceRepository();
		ResourceInformation sourceInformation = sourceEntry.getResourceInformation();
		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		if (field.hasIdField()) {
			Collection currentIds = (Collection) field.getIdAccessor().getValue(source);
			currentIds.addAll((Collection) targetIds);
		}
		else {
			RegistryEntry targetEntry = context.getTargetEntry(field);
			Iterable<D> targets = context.findAll(targetEntry, targetIds);
			@SuppressWarnings("unchecked")
			Collection<D> currentTargets = getOrCreateCollection(source, field);
			for (D target : targets) {
				currentTargets.add(target);
			}
		}
		sourceAdapter.update(source, context.createSaveQueryAdapter(fieldName));
	}

	@Override
	public void removeRelations(T source, Iterable<J> targetIds, String fieldName) {
		RegistryEntry sourceEntry = context.getSourceEntry();
		ResourceRepositoryAdapter<T, I> sourceAdapter = sourceEntry.getResourceRepository();
		ResourceInformation sourceInformation = sourceEntry.getResourceInformation();
		ResourceField field = sourceInformation.findFieldByUnderlyingName(fieldName);
		if (field.hasIdField()) {
			Collection currentIds = (Collection) field.getIdAccessor().getValue(source);
			currentIds.removeAll((Collection) targetIds);
		}
		else {
			RegistryEntry targetEntry = context.getTargetEntry(field);
			Iterable<D> targets = context.findAll(targetEntry, targetIds);
			@SuppressWarnings("unchecked")
			Collection<D> currentTargets = getOrCreateCollection(source, field);
			for (D target : targets) {
				currentTargets.remove(target);
			}
		}
		sourceAdapter.update(source, context.createSaveQueryAdapter(fieldName));
	}

	private Collection<D> getOrCreateCollection(Object source, ResourceField field) {
		Object property = field.getAccessor().getValue(source);
		if (property == null) {
			Class<?> propertyClass = field.getType();
			boolean isList = List.class.isAssignableFrom(propertyClass);
			property = isList ? new ArrayList() : new HashSet();
			field.getAccessor().setValue(source, property);
		}
		return (Collection<D>) property;
	}
}
