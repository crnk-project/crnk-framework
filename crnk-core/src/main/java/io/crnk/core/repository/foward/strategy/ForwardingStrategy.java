package io.crnk.core.repository.foward.strategy;

import java.io.Serializable;

public interface ForwardingStrategy<T, I , D, J > {

	void init(ForwardingStrategyContext context);

}
