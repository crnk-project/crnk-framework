package io.crnk.client.internal;

import io.crnk.core.engine.result.ImmediateResult;
import io.crnk.core.engine.result.ImmediateResultFactory;
import io.crnk.core.engine.result.Result;

public class SingletonResultFactory extends ImmediateResultFactory {

	private Object context;

	@Override
	public Object getThreadContext() {
		return context;
	}

	@Override
	public Result<Object> getContext() {
		return new ImmediateResult(context);
	}

	@Override
	public void setThreadContext(Object context) {
		this.context = context;
	}

	@Override
	public void clearContext() {
		this.context = null;
	}

	@Override
	public boolean hasThreadContext() {
		return context != null;
	}
}
