package io.crnk.core.queryspec.internal;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.queryspec.DefaultQuerySpecDeserializer;
import io.crnk.core.queryspec.DefaultQuerySpecSerializer;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.QuerySpecDeserializer;
import io.crnk.core.queryspec.QuerySpecSerializer;
import io.crnk.core.queryspec.mapper.QuerySpecUrlContext;
import io.crnk.core.queryspec.mapper.QuerySpecUrlMapper;
import io.crnk.core.queryspec.mapper.UnkonwnMappingAware;

import java.util.Map;
import java.util.Set;

@Deprecated
public class UrlMapperAdapter implements QuerySpecUrlMapper, UnkonwnMappingAware {

	private QuerySpecDeserializer deserializer;

	private QuerySpecSerializer serializer;

	private QuerySpecUrlContext ctx;

	public UrlMapperAdapter(QuerySpecDeserializer deserializer) {
		this.deserializer = deserializer;
	}

	@Override
	public void init(QuerySpecUrlContext ctx) {
		this.ctx = ctx;
		deserializer.init(ctx);
	}

	@Override
	public Map<String, Set<String>> serialize(QuerySpec querySpec) {
		if (serializer == null) {
			serializer = new DefaultQuerySpecSerializer(ctx.getResourceRegistry());
		}
		return serializer.serialize(querySpec);
	}

	@Override
	public QuerySpec deserialize(ResourceInformation resourceInformation, Map<String, Set<String>> urlParameters) {
		return deserializer.deserialize(resourceInformation, urlParameters);
	}

	@Override
	public boolean getAllowUnknownAttributes() {
		if (deserializer instanceof DefaultQuerySpecDeserializer) {
			return ((DefaultQuerySpecDeserializer) deserializer).getAllowUnknownAttributes();
		}
		return false;
	}

	@Override
	public void setAllowUnknownAttributes(boolean allowUnknownAttributes) {
		if (deserializer instanceof DefaultQuerySpecDeserializer) {
			((DefaultQuerySpecDeserializer) deserializer).setAllowUnknownAttributes(allowUnknownAttributes);
		}
	}

	@Override
	public boolean isAllowUnknownParameters() {
		if (deserializer instanceof DefaultQuerySpecDeserializer) {
			return ((DefaultQuerySpecDeserializer) deserializer).isAllowUnknownParameters();
		}
		return false;
	}

	@Override
	public void setAllowUnknownParameters(boolean allowUnknownParameters) {
		if (deserializer instanceof DefaultQuerySpecDeserializer) {
			((DefaultQuerySpecDeserializer) deserializer).setAllowUnknownParameters(allowUnknownParameters);
		}
	}

	public void setSerializer(QuerySpecSerializer serializer) {
		this.serializer = serializer;
	}

	public QuerySpecDeserializer getDeserializer() {
		return deserializer;
	}
}