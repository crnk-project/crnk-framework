package io.crnk.core.repository.foward.strategy;

import java.io.Serializable;

public interface ForwardingStrategy<T, I extends Serializable, D, J extends Serializable> {

	void init(ForwardingStrategyContext context);

}
