package io.crnk.core.engine.internal.dispatcher.registry;

import io.crnk.core.engine.information.resource.ResourceFieldNameTransformer;
import io.crnk.core.engine.information.resource.ResourceInformationBuilder;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistry;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.internal.information.resource.AnnotationResourceInformationBuilder;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.exception.MethodNotFoundException;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.resource.registry.ResourceRegistryBuilderTest;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.legacy.registry.ResourceRegistryBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static io.crnk.core.resource.registry.ResourceRegistryTest.TEST_MODELS_URL;

public class ControllerRegistryTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	private ResourceRegistry resourceRegistry;

	@Before
	public void prepare() {
		ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(
				new ResourceFieldNameTransformer());
		ModuleRegistry moduleRegistry = new ModuleRegistry();
		ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(moduleRegistry, new SampleJsonServiceLocator(),
				resourceInformationBuilder);
		resourceRegistry = registryBuilder
				.build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, moduleRegistry, new ConstantServiceUrlProvider(TEST_MODELS_URL));
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
