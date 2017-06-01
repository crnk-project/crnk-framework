package io.crnk.client.http;

import java.util.concurrent.TimeUnit;

import io.crnk.client.http.apache.HttpClientAdapter;
import io.crnk.client.http.apache.HttpClientAdapterListener;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class HttpClientAdapterTest {

	@Test
	public void testCannotAddListenersAfterInitialization() {
		HttpClientAdapter adapter = new HttpClientAdapter();
		adapter.getImplementation();

		try {
			adapter.addListener(Mockito.mock(HttpClientAdapterListener.class));
			Assert.fail();
		} catch (IllegalStateException e) {
			// ok
		}
	}

	@Test
	public void testCannotSetTimeoutAfterInitialization() {
		HttpClientAdapter adapter = new HttpClientAdapter();
		adapter.getImplementation();

		try {
			adapter.setReceiveTimeout(0, TimeUnit.DAYS);
			Assert.fail();
		} catch (IllegalStateException e) {
			// ok
		}
	}
}
