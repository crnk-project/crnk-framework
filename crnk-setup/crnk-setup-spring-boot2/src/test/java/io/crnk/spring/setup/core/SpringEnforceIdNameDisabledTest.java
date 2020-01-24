package io.crnk.spring.setup.core;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.spring.app.BasicSpringBoot2Application;
import io.crnk.test.mock.models.RenamedIdResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BasicSpringBoot2Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@TestPropertySource(properties = {"crnk.enforceIdName=false"})
public class SpringEnforceIdNameDisabledTest {

	@Autowired
	private ObjectProvider<ResourceRegistry> resourceRegistry;

	@Test
	public void check() {
		RegistryEntry entry = resourceRegistry.getIfAvailable().getEntry(RenamedIdResource.class);
		ResourceField idField = entry.getResourceInformation().getIdField();
		Assert.assertEquals("notId", idField.getJsonName());
	}
}
