package io.crnk.client.internal;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.client.ResponseBodyException;
import io.crnk.client.internal.proxy.ClientProxyFactory;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.test.mock.TestModule;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ClientResourceUpsertTest {

	private ClientResourceUpsert upsert;

	private CrnkBoot boot;

	@Before
	public void setup() {
		boot = new CrnkBoot();
		boot.addModule(new TestModule());
		boot.boot();

		PropertiesProvider propertiesProvider = new NullPropertiesProvider();
		ClientProxyFactory proxyFactory = Mockito.mock(ClientProxyFactory.class);

		upsert = new ClientResourceUpsert(boot.getResourceRegistry(), propertiesProvider, boot.getModuleRegistry()
				.getTypeParser(),
				boot.getObjectMapper(),
				boot.getDocumentMapper(),
				proxyFactory);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testAcceptableNotSupported() {
		upsert.isAcceptable(null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void handleNotSupported() {
		upsert.handle(null, null, null, null);
	}

	@Test(expected = ResponseBodyException.class)
	public void setInvalidMetaThrowsException() throws IOException {
		Resource resource = new Resource();
		JsonNode invalidMeta = boot.getObjectMapper().reader().readTree("{\"invalidAttr\": 1}");
		resource.setMeta((ObjectNode) invalidMeta);

		Task task = new Task();
		ResourceInformation resourceInformation = boot.getResourceRegistry().getEntry(Task.class).getResourceInformation();
		upsert.setMeta(resource, task, resourceInformation);
	}

	@Test
	public void setMeta() throws IOException {
		Resource resource = new Resource();
		JsonNode meta = boot.getObjectMapper().reader().readTree("{\"value\": \"metaValue\"}");
		resource.setMeta((ObjectNode) meta);

		Task task = new Task();
		ResourceInformation resourceInformation = boot.getResourceRegistry().getEntry(Task.class).getResourceInformation();
		upsert.setMeta(resource, task, resourceInformation);
		Assert.assertEquals("metaValue", task.getMetaInformation().value);
	}

	@Test(expected = ResponseBodyException.class)
	public void setInvalidLinksThrowsException() throws IOException {
		Resource resource = new Resource();
		JsonNode invalidLinks = boot.getObjectMapper().reader().readTree("{\"invalidAttr\": 1}");
		resource.setLinks((ObjectNode) invalidLinks);

		Task task = new Task();
		ResourceInformation resourceInformation = boot.getResourceRegistry().getEntry(Task.class).getResourceInformation();
		upsert.setLinks(resource, task, resourceInformation);
	}

	@Test
	public void setLinks() throws IOException {
		Resource resource = new Resource();
		JsonNode links = boot.getObjectMapper().reader().readTree("{\"value\": \"linksValue\"}");
		resource.setLinks((ObjectNode) links);

		Task task = new Task();
		ResourceInformation resourceInformation = boot.getResourceRegistry().getEntry(Task.class).getResourceInformation();
		upsert.setLinks(resource, task, resourceInformation);
		Assert.assertEquals("linksValue", task.getLinksInformation().value);
	}
}
