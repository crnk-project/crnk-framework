package io.crnk.security.internal;

import java.util.Collections;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.utils.Supplier;
import io.crnk.security.DataRoomFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataRoomMatcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataRoomMatcher.class);

	private Supplier<DataRoomFilter> filter;

	private SecurityProvider callerSecurityProvider;

	public DataRoomMatcher(Supplier<DataRoomFilter> filter, SecurityProvider callerSecurityProvider) {
		this.filter = filter;
		this.callerSecurityProvider = callerSecurityProvider;
	}

	public QuerySpec filter(QuerySpec querySpec, HttpMethod method) {
		return filter.get().filter(querySpec, method, callerSecurityProvider);
	}

	public QuerySpec filter(QuerySpec querySpec, HttpMethod method, SecurityProvider securityProvider) {
		return filter.get().filter(querySpec, method, securityProvider);
	}

	public boolean checkMatch(Object resource, HttpMethod method) {
		return checkMatch(resource, method, callerSecurityProvider);
	}

	public boolean checkMatch(Object resource, HttpMethod method, SecurityProvider securityProvider) {
		QuerySpec querySpec = filter(new QuerySpec(resource.getClass()), method, securityProvider);
		DefaultResourceList<Object> list = querySpec.apply(Collections.singleton(resource));
		return !list.isEmpty();
	}

	public void verifyMatch(Object resource, HttpMethod method) {
		verifyMatch(resource, method, callerSecurityProvider);
	}

	public void verifyMatch(Object resource, HttpMethod method, SecurityProvider securityProvider) {
		boolean match = checkMatch(resource, method, securityProvider);
		if (!match) {
			LOGGER.warn("dataroom prevented access to {} for {}", resource, method);
			throw new ForbiddenException("not allowed to access resource");
		}
	}
}
