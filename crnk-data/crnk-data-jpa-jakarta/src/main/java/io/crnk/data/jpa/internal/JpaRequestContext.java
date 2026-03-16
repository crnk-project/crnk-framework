package io.crnk.data.jpa.internal;

import io.crnk.core.queryspec.QuerySpec;

public class JpaRequestContext {

	private Object repository;

	private QuerySpec querySpec;

	public JpaRequestContext(Object repository, QuerySpec querySpec) {
		super();
		this.repository = repository;
		this.querySpec = querySpec;
	}

	public Object getRepository() {
		return repository;
	}

	public QuerySpec getQuerySpec() {
		return querySpec;
	}

}
