package io.crnk.client.http;

import io.crnk.client.CrnkClient;
import io.crnk.client.http.okhttp.OkHttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapterListener;
import io.crnk.client.http.okhttp.OkHttpAdapterListenerBase;
import io.crnk.client.suite.RepositoryAccessClientTest;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.test.mock.models.Task;
import okhttp3.OkHttpClient;
import org.junit.Test;
import org.mockito.Mockito;

public class OkHttpClientTest extends RepositoryAccessClientTest {

	private OkHttpAdapterListener listener;

	@Override
	protected void setupClient(CrnkClient client) {
		super.setupClient(client);

		listener = Mockito.mock(OkHttpAdapterListener.class);

		OkHttpAdapter httpAdapter = OkHttpAdapter.newInstance();
		httpAdapter.addListener(new OkHttpAdapterListenerBase());
		httpAdapter.addListener(listener);
		client.setHttpAdapter(httpAdapter);
	}


	@Test
	public void testListenerInvoked() {
		taskRepo.findAll(new QuerySpec(Task.class));
		Mockito.verify(listener, Mockito.times(1)).onBuild(Mockito.any(OkHttpClient.Builder.class));
	}
}
