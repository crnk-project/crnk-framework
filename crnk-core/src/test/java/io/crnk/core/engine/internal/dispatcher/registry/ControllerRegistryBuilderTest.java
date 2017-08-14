package io.crnk.core.engine.internal.dispatcher.registry;

import io.crnk.core.engine.filter.FilterBehaviorDirectory;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistry;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistryBuilder;
import io.crnk.core.engine.internal.dispatcher.path.ResourcePath;
import org.junit.Test;
import org.mockito.Mockito;

public class ControllerRegistryBuilderTest {

	@Test
	public void onBuildShouldAddAllControllers() throws Exception {
		// GIVEN
		FilterBehaviorDirectory filterBehaviorDirectory = Mockito.mock(FilterBehaviorDirectory.class);
		ControllerRegistryBuilder sut = new ControllerRegistryBuilder(null, null, null, null, filterBehaviorDirectory);

		// WHEN
		ControllerRegistry result = sut.build();

		// THEN
		result.getController(new ResourcePath("path"), "GET");
	}
}
