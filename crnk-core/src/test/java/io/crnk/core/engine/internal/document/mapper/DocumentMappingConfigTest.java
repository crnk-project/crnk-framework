package io.crnk.core.engine.internal.document.mapper;

import org.junit.Assert;
import org.junit.Test;

public class DocumentMappingConfigTest {

	@Test
	public void checkAccessors() {
		DocumentMappingConfig config = new DocumentMappingConfig();
		Assert.assertNotNull(config.getResourceMapping());

		ResourceMappingConfig resourceMappingConfig = new ResourceMappingConfig();
		config.setResourceMapping(resourceMappingConfig);
		Assert.assertSame(resourceMappingConfig, config.getResourceMapping());
	}
}
