package io.crnk.core.queryspec.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;

import io.crnk.core.queryspec.pagingspec.CustomOffsetLimitPagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;

@Deprecated
public class LegacyQuerySpecDeserializerTest extends DefaultQuerySpecDeserializerTestBase {

	@Override
	protected List<PagingBehavior> additionalPagingBehaviors() {
		return new ArrayList<>(Arrays.asList(new CustomOffsetLimitPagingBehavior()));
	}

	@Before
	public void setup() {
		super.setup();
		deserializer.setEnforceDotPathSeparator(false);
	}
}
