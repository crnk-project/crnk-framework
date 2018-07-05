package io.crnk.client.internal;

import java.io.Serializable;
import java.util.List;

import io.crnk.client.CrnkClient;
import io.crnk.client.legacy.ResourceRepositoryStub;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.JsonApiUrlBuilder;
import io.crnk.legacy.queryParams.QueryParams;

public class LegacyResourceRepositoryStubImpl<T, I extends Serializable> extends ResourceRepositoryStubImpl<T, I> implements
		ResourceRepositoryStub<T, I> {

	public LegacyResourceRepositoryStubImpl(CrnkClient client, Class<T> resourceClass,
			ResourceInformation resourceInformation,
			JsonApiUrlBuilder urlBuilder) {
		super(client, resourceClass, resourceInformation, urlBuilder);
	}

	@Override
	public T findOne(I id, QueryParams queryParams) {
		String url = urlBuilder.buildUrl(resourceInformation, id, queryParams);
		return findOne(url);
	}

	@Override
	public List<T> findAll(QueryParams queryParams) {
		String url = urlBuilder.buildUrl(resourceInformation, null, queryParams);
		return findAll(url);
	}

	@Override
	public List<T> findAll(Iterable<I> ids, QueryParams queryParams) {
		String url = urlBuilder.buildUrl(resourceInformation, ids, queryParams);
		return findAll(url);
	}
}
