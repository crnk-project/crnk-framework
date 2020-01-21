package io.crnk.client.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.client.ResponseBodyException;
import io.crnk.client.internal.proxy.ClientProxyFactory;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.controller.ControllerContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.test.mock.TestModule;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.Serializable;

public class ClientResourceUpsertTest {

	private ClientResourceUpsert upsert;

	private CrnkBoot boot;

	@Before
	public void setup() {
		boot = new CrnkBoot();
		boot.addModule(new TestModule());
		boot.boot();

		ClientProxyFactory proxyFactory = Mockito.mock(ClientProxyFactory.class);
		ControllerContext controllerContext = new ControllerContext(boot.getModuleRegistry(), boot::getDocumentMapper);
		upsert = new ClientResourceUpsert(proxyFactory);
		upsert.init(controllerContext);
	}

	@Test
	public void testUIDComputation() {
		Serializable id = "test";
		RegistryEntry entry = Mockito.mock(RegistryEntry.class);
		ResourceInformation resourceInformation = Mockito.mock(ResourceInformation.class);
		Mockito.when(resourceInformation.getResourceType()).thenReturn("someType");
		Mockito.when(resourceInformation.toIdString(Mockito.eq(id))).thenReturn("someId");
		Mockito.when(entry.getResourceInformation()).thenReturn(resourceInformation);
		String uid = upsert.getUID(entry, id);
		Assert.assertEquals("someType#someId", uid);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testAcceptableNotSupported() {
		upsert.isAcceptable(null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void handleNotSupported() {
		upsert.handle(null, null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testMethodNotSupported() {
		upsert.getHttpMethod();
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
		Assert.assertEquals("linksValue", task.getLinksInformation().value.getHref());
	}
}
