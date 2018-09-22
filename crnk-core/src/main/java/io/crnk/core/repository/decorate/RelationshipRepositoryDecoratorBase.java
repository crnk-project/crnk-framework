package io.crnk.core.repository.decorate;

import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryAware;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.resource.list.ResourceList;

import java.io.Serializable;

public abstract class RelationshipRepositoryDecoratorBase<T, I extends Serializable, D, J extends Serializable>
		implements RelationshipRepositoryDecorator<T, I, D, J>, ResourceRegistryAware {

	private RelationshipRepositoryV2<T, I, D, J> decoratedObject;

	@Override
	public Class<T> getSourceResourceClass() {
		return decoratedObject.getSourceResourceClass();
	}

	@Override
	public Class<D> getTargetResourceClass() {
		return decoratedObject.getTargetResourceClass();
	}

	@Override
	public RelationshipMatcher getMatcher() {
		return decoratedObject.getMatcher();
	}

	@Override
	public void setRelation(T source, J targetId, String fieldName) {
		decoratedObject.setRelation(source, targetId, fieldName);
	}

	@Override
	public void setRelations(T source, Iterable<J> targetIds, String fieldName) {
		decoratedObject.setRelations(source, targetIds, fieldName);
	}

	@Override
	public void addRelations(T source, Iterable<J> targetIds, String fieldName) {
		decoratedObject.addRelations(source, targetIds, fieldName);
	}

	@Override
	public void removeRelations(T source, Iterable<J> targetIds, String fieldName) {
		decoratedObject.removeRelations(source, targetIds, fieldName);
	}

	@Override
	public D findOneTarget(I sourceId, String fieldName, QuerySpec querySpec) {
		return decoratedObject.findOneTarget(sourceId, fieldName, querySpec);
	}

	@Override
	public ResourceList<D> findManyTargets(I sourceId, String fieldName, QuerySpec querySpec) {
		return decoratedObject.findManyTargets(sourceId, fieldName, querySpec);
	}

	@Override
	public void setDecoratedObject(RelationshipRepositoryV2<T, I, D, J> decoratedObject) {
		this.decoratedObject = decoratedObject;
	}

	@Override
	public void setResourceRegistry(ResourceRegistry resourceRegistry) {
		if (decoratedObject instanceof ResourceRegistryAware) {
			((ResourceRegistryAware) decoratedObject).setResourceRegistry(resourceRegistry);
		}
	}
}
