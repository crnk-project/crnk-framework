package io.crnk.client;

import io.crnk.client.http.apache.HttpClientAdapter;
import io.crnk.client.http.apache.HttpClientAdapterListener;
import io.crnk.client.http.okhttp.OkHttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapterListener;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.test.mock.models.Task;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class CharsetTest extends AbstractClientTest {


	@Test
	public void testUTF8isDefaultForOkHttp() throws InstantiationException, IllegalAccessException {
		testUTF8isDefault(true);
	}

	@Test
	public void testUTF8isDefaultForApacheHttpClient() throws InstantiationException, IllegalAccessException {
		testUTF8isDefault(false);
	}

	private String requestContentType;

	private String responseContentType;

	public void testUTF8isDefault(boolean okHttp) {
		requestContentType = null;
		responseContentType = null;
		if (okHttp) {
			OkHttpAdapter adapter = OkHttpAdapter.newInstance();
			adapter.addListener(new OkHttpAdapterListener() {
				@Override
				public void onBuild(OkHttpClient.Builder builder) {
					builder.addInterceptor(new Interceptor() {
						@Override
						public Response intercept(Chain chain) throws IOException {
							requestContentType = chain.request().header(HttpHeaders.HTTP_CONTENT_TYPE);
							Response response = chain.proceed(chain.request());
							responseContentType = response.header(HttpHeaders.HTTP_CONTENT_TYPE);
							return response;
						}
					});
				}
			});
			client.setHttpAdapter(adapter);
		} else {
			HttpClientAdapter adapter = HttpClientAdapter.newInstance();
			adapter.addListener(new HttpClientAdapterListener() {
				@Override
				public void onBuild(HttpClientBuilder builder) {
					builder.addInterceptorFirst(new HttpRequestInterceptor() {
						@Override
						public void process(HttpRequest httpRequest, HttpContext httpContext) {
							Header header = httpRequest.getFirstHeader(HttpHeaders.HTTP_CONTENT_TYPE);
							requestContentType = header != null ? header.getValue() : null;
						}
					});
					builder.addInterceptorFirst(new HttpResponseInterceptor() {
						@Override
						public void process(HttpResponse httpResponse, HttpContext httpContext) {
							Header header = httpResponse.getFirstHeader(HttpHeaders.HTTP_CONTENT_TYPE);
							responseContentType = header != null ? header.getValue() : null;
						}
					});
				}
			});
			client.setHttpAdapter(adapter);


		}
		ResourceRepository<Task, Long> testRepo = client.getRepositoryForType(Task.class);

		Task entity = new Task();
		entity.setId(1L);
		entity.setName("äöüé@¢€");
		testRepo.create(entity);

		Assert.assertEquals(HttpHeaders.JSONAPI_CONTENT_TYPE_AND_CHARSET, requestContentType);
		Assert.assertEquals(HttpHeaders.JSONAPI_CONTENT_TYPE_AND_CHARSET, responseContentType);

		Task savedEntity = testRepo.findOne(1L, new QuerySpec(Task.class));
		Assert.assertEquals(entity.getName(), savedEntity.getName());

		Assert.assertNull(requestContentType);
		Assert.assertEquals(HttpHeaders.JSONAPI_CONTENT_TYPE_AND_CHARSET, responseContentType);
	}

}
