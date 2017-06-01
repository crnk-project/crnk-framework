package io.crnk.client.http;

import java.util.concurrent.TimeUnit;

import io.crnk.client.http.okhttp.OkHttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapterListener;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class OkHttpAdapterTest {


	@Test
	public void testCannotAddListenersAfterInitialization() {
		OkHttpAdapter adapter = new OkHttpAdapter();
		adapter.getImplementation();

		try {
			adapter.addListener(Mockito.mock(OkHttpAdapterListener.class));
			Assert.fail();
		} catch (IllegalStateException e) {
			// ok
		}
	}

	@Test
	public void testCannotSetTimeoutAfterInitialization() {
		OkHttpAdapter adapter = new OkHttpAdapter();
		adapter.getImplementation();

		try {
			adapter.setReceiveTimeout(0, TimeUnit.DAYS);
			Assert.fail();
		} catch (IllegalStateException e) {
			// ok
		}
	}
}
