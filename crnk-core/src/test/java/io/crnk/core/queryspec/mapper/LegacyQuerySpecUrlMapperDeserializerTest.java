package io.crnk.core.queryspec.mapper;

import org.junit.Before;

public class LegacyQuerySpecUrlMapperDeserializerTest extends DefaultQuerySpecUrlMapperDeserializerTestBase {

	@Before
	public void setup() {
		super.setup();
		urlMapper.setEnforceDotPathSeparator(false);
	}
}
