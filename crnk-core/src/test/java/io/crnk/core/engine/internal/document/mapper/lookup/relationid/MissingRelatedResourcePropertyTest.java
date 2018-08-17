package io.crnk.core.engine.internal.document.mapper.lookup.relationid;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.properties.PropertiesProvider;
import org.junit.Test;

public class MissingRelatedResourcePropertyTest extends MissingRelatedResourceTest {

	@Override
	protected PropertiesProvider getPropertiesProvider() {
		return key -> {
			if (CrnkProperties.EXCEPTION_ON_MISSING_RELATED_RESOURCE.equals(key)) {
				return "false";
			}
			return null;
		};
	}

	@Override
	@Test
	public void provokeResourceNotFound() {
		super.provokeResourceNotFound();
	}
}
