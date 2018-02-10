package io.crnk.core.queryspec;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.queryspec.paging.PagingSpecDeserializer;

import java.util.Map;
import java.util.Set;

public interface QuerySpecDeserializer {

	void init(QuerySpecDeserializerContext ctx);

	QuerySpec deserialize(ResourceInformation resourceInformation, Map<String, Set<String>> queryParams);

	void setPagingSpecDeserializer(PagingSpecDeserializer pagingSpecDeserializer);

	PagingSpecDeserializer getPagingSpecDeserializer();
}
