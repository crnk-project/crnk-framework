package io.crnk.client;

import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

public class ServiceDiscoveryClientTest {

	@Test
	public void notUsedInConstructor() throws NoSuchFieldException, IllegalAccessException {
		CrnkClient client = new CrnkClient("");
		Field field = CrnkClient.class.getDeclaredField("serviceDiscovery");
		field.setAccessible(true);
		Object serviceDiscovery = field.get(client);
		Assert.assertNull(serviceDiscovery);
		Assert.assertNotNull(client.getServiceDiscovery());
	}

	@Test
	public void allowOverrideServiceDiscovery() throws NoSuchFieldException, IllegalAccessException {
		ServiceDiscovery mock = Mockito.mock(ServiceDiscovery.class);
		CrnkClient client = new CrnkClient("");
		client.setServiceDiscovery(mock);
		Assert.assertSame(mock, client.getServiceDiscovery());
		client.getRepositoryForType(Task.class);
		Assert.assertSame(mock, client.getServiceDiscovery());
	}
}
