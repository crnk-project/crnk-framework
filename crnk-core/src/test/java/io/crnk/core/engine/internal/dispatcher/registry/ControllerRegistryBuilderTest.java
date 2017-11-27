package io.crnk.core.engine.internal.dispatcher.registry;

import java.util.Collections;
import java.util.List;

import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistry;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistryBuilder;
import io.crnk.core.engine.internal.dispatcher.path.ResourcePath;
import io.crnk.core.engine.properties.PropertiesProvider;
import org.junit.Test;
import org.mockito.Mockito;

public class ControllerRegistryBuilderTest {

	@Test
	public void onBuildShouldAddAllControllers() throws Exception {
		// GIVEN
		PropertiesProvider propertiesProvider = Mockito.mock(PropertiesProvider.class);
		ResourceFilterDirectory resourceFilterDirectory = Mockito.mock(ResourceFilterDirectory.class);
		ControllerRegistryBuilder sut = new ControllerRegistryBuilder(null, null, null, propertiesProvider, resourceFilterDirectory, (List) Collections.emptyList());

		// WHEN
		ControllerRegistry result = sut.build();

		// THEN
		result.getController(new ResourcePath("path"), "GET");
	}
}
