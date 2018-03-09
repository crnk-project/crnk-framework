package io.crnk.core.repository.foward.strategy;

import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.queryspec.QuerySpec;

import java.io.Serializable;

public interface ForwardingGetStrategy<T, I extends Serializable, D, J extends Serializable>
		extends ForwardingStrategy<T, I, D, J> {


	MultivaluedMap<I, D> findTargets(Iterable<I> sourceIds, String fieldName, QuerySpec querySpec, QueryContext queryContext);
}
