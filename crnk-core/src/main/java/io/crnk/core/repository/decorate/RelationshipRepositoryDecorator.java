package io.crnk.core.repository.decorate;

import io.crnk.core.engine.internal.utils.Decorator;
import io.crnk.core.repository.RelationshipRepositoryV2;

import java.io.Serializable;

public interface RelationshipRepositoryDecorator<T, I extends Serializable, D, J extends Serializable>
		extends RelationshipRepositoryV2<T, I, D, J>, Decorator<RelationshipRepositoryV2<T, I, D, J>> {

	@Override
	void setDecoratedObject(RelationshipRepositoryV2<T, I, D, J> decoratedObject);
}
