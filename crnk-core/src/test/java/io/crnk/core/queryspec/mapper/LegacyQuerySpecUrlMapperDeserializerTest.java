package io.crnk.core.queryspec.mapper;

import io.crnk.core.queryspec.pagingspec.CustomOffsetLimitPagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LegacyQuerySpecUrlMapperDeserializerTest extends DefaultQuerySpecUrlMapperDeserializerTestBase {

	@Override
	protected List<PagingBehavior> additionalPagingBehaviors() {
		return new ArrayList<>(Arrays.asList(new CustomOffsetLimitPagingBehavior()));
	}

	@Before
	public void setup() {
		super.setup();
		urlMapper.setEnforceDotPathSeparator(false);
	}
}
