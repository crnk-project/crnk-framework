package io.crnk.core.engine.internal.dispatcher.registry;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistry;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.exception.MethodNotFoundException;
import io.crnk.core.mock.MockConstants;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.core.resource.registry.ResourceRegistryTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ControllerRegistryTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	private ResourceRegistry resourceRegistry;

	@Before
	public void prepare() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery(MockConstants.TEST_MODELS_PACKAGE));
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL));
		boot.boot();
		resourceRegistry = boot.getResourceRegistry();
	}

	@Test
	public void onUnsupportedRequestRegisterShouldThrowError() {
		// GIVEN
		PathBuilder pathBuilder = new PathBuilder(resourceRegistry);
		JsonPath jsonPath = pathBuilder.build("/tasks/");
		String requestType = "PATCH";
		ControllerRegistry sut = new ControllerRegistry(null);

		// THEN
		expectedException.expect(MethodNotFoundException.class);

		// WHEN
		sut.getController(jsonPath, requestType);
	}
}
