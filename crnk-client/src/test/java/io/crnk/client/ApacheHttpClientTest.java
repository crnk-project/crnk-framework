package io.crnk.client;

import io.crnk.client.http.apache.HttpClientAdapter;
import io.crnk.client.http.apache.HttpClientAdapterListener;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.test.mock.models.Task;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

public class ApacheHttpClientTest extends QuerySpecClientTest {

	private HttpClientAdapterListener listener;

	@Override
	protected void setupClient(CrnkClient client) {
		super.setupClient(client);

		HttpClientAdapter adapter = HttpClientAdapter.newInstance();
		adapter.setReceiveTimeout(30000, TimeUnit.MILLISECONDS);
		listener = Mockito.mock(HttpClientAdapterListener.class);
		adapter.addListener(listener);
		client.setHttpAdapter(adapter);
	}

	@Test
	public void testListenerInvoked() {
		taskRepo.findAll(new QuerySpec(Task.class));

		ArgumentCaptor<HttpClientBuilder> captor = ArgumentCaptor.forClass(HttpClientBuilder.class);
		Mockito.verify(listener, Mockito.times(1)).onBuild(captor.capture());
	}
}
