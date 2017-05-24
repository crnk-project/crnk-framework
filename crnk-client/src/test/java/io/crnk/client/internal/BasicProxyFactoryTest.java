package io.crnk.client.internal;

import io.crnk.client.internal.proxy.BasicProxyFactory;
import io.crnk.client.internal.proxy.ClientProxyFactoryContext;
import io.crnk.client.internal.proxy.ObjectProxy;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.ScheduleRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collection;

public class BasicProxyFactoryTest {

	private BasicProxyFactory factory;

	@Before
	public void setup() {
		factory = new BasicProxyFactory();
		ClientProxyFactoryContext context = Mockito.mock(ClientProxyFactoryContext.class);
		factory.init(context);
	}

	@Test
	public void testCollectionProxy() {
		Collection<Task> col = factory.createCollectionProxy(Task.class, ResourceList.class, "http://127.0.0.1:99999");
		Assert.assertTrue(col instanceof ResourceList);
		Assert.assertTrue(col instanceof ObjectProxy);
		ObjectProxy proxy = (ObjectProxy) col;
		Assert.assertFalse(proxy.isLoaded());
		Assert.assertEquals("http://127.0.0.1:99999", proxy.getUrl());
	}

	@Test
	public void testWrappedCollectionProxy() {
		Collection<Schedule> col = factory.createCollectionProxy(Schedule.class, ScheduleRepository.ScheduleList.class, "http://127.0.0.1:99999");
		Assert.assertTrue(col instanceof ScheduleRepository.ScheduleList);

		ScheduleRepository.ScheduleList list = (ScheduleRepository.ScheduleList) col;
		Assert.assertTrue(list.getWrappedList() instanceof ObjectProxy);
		ObjectProxy proxy = (ObjectProxy) list.getWrappedList();
		Assert.assertFalse(proxy.isLoaded());
		Assert.assertEquals("http://127.0.0.1:99999", proxy.getUrl());
	}


	@Test(expected = IllegalStateException.class)
	public void testInvalidList() {
		factory.createCollectionProxy(Task.class, InvalidList.class, "http://127.0.0.1:99999");
	}

	public static class InvalidList extends DefaultResourceList {

		public InvalidList(String invalidParameter) { // NOSONAR
		}
	}
}
