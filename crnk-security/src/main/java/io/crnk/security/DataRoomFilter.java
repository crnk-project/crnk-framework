package io.crnk.security;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.queryspec.QuerySpec;

public interface DataRoomFilter {

	QuerySpec filter(QuerySpec querySpec, HttpMethod method, SecurityProvider securityProvider);

}
