package io.crnk.core.engine.internal.dispatcher.registry;

import io.crnk.core.engine.internal.dispatcher.ControllerRegistry;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistryBuilder;
import io.crnk.core.engine.internal.dispatcher.path.ResourcePath;
import org.junit.Test;

public class ControllerRegistryBuilderTest {

	@Test
	public void onBuildShouldAddAllControllers() throws Exception {
		// GIVEN
		ControllerRegistryBuilder sut = new ControllerRegistryBuilder(null, null, null, null);

		// WHEN
		ControllerRegistry result = sut.build();

		// THEN
		result.getController(new ResourcePath("path"), "GET");
	}
}
