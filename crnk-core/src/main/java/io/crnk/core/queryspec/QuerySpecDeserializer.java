package io.crnk.core.queryspec;

import java.util.Map;
import java.util.Set;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.queryspec.mapper.QuerySpecUrlMapper;

/**
 * @deprecated use {@link QuerySpecUrlMapper}
 */
public interface QuerySpecDeserializer {

	void init(QuerySpecDeserializerContext ctx);

	QuerySpec deserialize(ResourceInformation resourceInformation, Map<String, Set<String>> queryParams);
}
