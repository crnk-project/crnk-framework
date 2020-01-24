package io.crnk.core.queryspec.mapper;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;

public class LegacyQuerySpecUrlMapperDeserializerTest extends DefaultQuerySpecUrlMapperDeserializerTestBase {

	@Before
	public void setup() {
		super.setup();
		urlMapper.setEnforceDotPathSeparator(false);
	}

	@Test
	@Disabled // not support on old filter
	@Ignore
	public void testFilterOnRelatedWithJson() {
		super.testFilterOnRelatedWithJson();
	}
}
