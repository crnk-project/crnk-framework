package io.crnk.core.queryspec.repository;

import org.junit.Before;

@Deprecated
public class LegacyQuerySpecDeserializerTest extends DefaultQuerySpecDeserializerTestBase {

	@Before
	public void setup() {
		super.setup();
		deserializer.setEnforceDotPathSeparator(false);
	}
}
