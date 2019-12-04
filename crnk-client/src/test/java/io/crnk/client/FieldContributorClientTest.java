package io.crnk.client;

import java.util.Arrays;

import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.SimpleModule;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Test;

public class FieldContributorClientTest extends AbstractClientTest {


	protected void setupClient(CrnkClient client) {
		SimpleModule module = new SimpleModule("contributor");
		module.addResourceFieldContributor(context -> {
			InformationBuilder.FieldInformationBuilder builder = context.getInformationBuilder().createResourceField();
			builder.name("test");
			builder.fieldType(ResourceFieldType.ATTRIBUTE);
			builder.type(String.class);
			return Arrays.asList(builder.build());
		});
		client.addModule(module);
	}

	@Test
	public void checkFieldAdded() {
		// create repository
		client.getRepositoryForType(Task.class);

		ResourceRegistry resourceRegistry = client.getModuleRegistry().getResourceRegistry();
		RegistryEntry entry = resourceRegistry.findEntry(Task.class);
		ResourceInformation resourceInformation = entry.getResourceInformation();
		Assert.assertNotNull(resourceInformation.findFieldByName("test"));
	}
}