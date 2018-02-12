package io.crnk.core.repository.foward.strategy;

import io.crnk.core.engine.internal.utils.PreconditionUtil;

public class ForwardingStrategyBase {

	protected ForwardingStrategyContext context;

	public void init(ForwardingStrategyContext context) {
		PreconditionUtil.assertNull("this stategy can only be initialized once", this.context);
		this.context = context;
	}

}
