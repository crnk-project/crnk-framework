package io.crnk.core.queryspec.mapper;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class LegacyQuerySpecUrlMapperDeserializerTest extends DefaultQuerySpecUrlMapperDeserializerTestBase {

	@Before
	public void setup() {
		super.setup();
		urlMapper.setEnforceDotPathSeparator(false);
	}

	@Ignore
	@Test
	public void testFilterWithJson() {
		// not supported in legacy mode
	}
}
