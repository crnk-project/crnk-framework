package io.crnk.core.engine.internal.dispatcher.registry;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistry;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.BadRequestException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ControllerRegistryTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	private ResourceRegistry resourceRegistry;
	private PathBuilder pathBuilder;

	@Before
	public void prepare() {
		CoreTestContainer container = new CoreTestContainer();
		container.setDefaultPackage();
		container.boot();
		resourceRegistry = container.getResourceRegistry();
		pathBuilder = new PathBuilder(resourceRegistry, container.getModuleRegistry().getTypeParser());
	}

	@Test
	public void onUnsupportedRequestRegisterShouldThrowError() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/");
		String requestType = "PATCH";
		ControllerRegistry sut = new ControllerRegistry(null);

		// THEN
		expectedException.expect(BadRequestException.class);

		// WHEN
		sut.getController(jsonPath, requestType);
	}
}
