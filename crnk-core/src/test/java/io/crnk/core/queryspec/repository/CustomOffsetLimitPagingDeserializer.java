package io.crnk.core.queryspec.repository;

import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpecDeserializer;
import io.crnk.core.queryspec.pagingspec.PagingSpecDeserializer;

import java.util.Set;

public class CustomOffsetLimitPagingDeserializer implements PagingSpecDeserializer<OffsetLimitPagingSpec> {

	private OffsetLimitPagingSpecDeserializer deserializer;

	public CustomOffsetLimitPagingDeserializer() {
		deserializer = new OffsetLimitPagingSpecDeserializer();
		deserializer.setDefaultOffset(1);
		deserializer.setDefaultLimit(10L);
		deserializer.setMaxPageLimit(20L);
	}

	@Override
	public OffsetLimitPagingSpec init() {
		return deserializer.init();
	}

	@Override
	public void deserialize(final OffsetLimitPagingSpec pagingSpec, final String name, final Set<String> values) {
		deserializer.deserialize(pagingSpec, name, values);
	}
}