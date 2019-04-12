package io.crnk.core.repository.decorate;

import io.crnk.core.engine.internal.utils.Decorator;
import io.crnk.core.repository.RelationshipRepository;

import java.io.Serializable;

public interface RelationshipRepositoryDecorator<T, I , D, J >
		extends RelationshipRepository<T, I, D, J>, Decorator<RelationshipRepository<T, I, D, J>> {

	@Override
	void setDecoratedObject(RelationshipRepository<T, I, D, J> decoratedObject);
}
