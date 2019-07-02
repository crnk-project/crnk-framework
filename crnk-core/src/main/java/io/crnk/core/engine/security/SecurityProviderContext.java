package io.crnk.core.engine.security;

import io.crnk.core.engine.query.QueryContext;

public interface SecurityProviderContext {
	QueryContext getQueryContext();
}
