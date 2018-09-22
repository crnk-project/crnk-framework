package io.crnk.core.engine.http;

import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class HttpRequestContextBaseAdapterTest {

	private HttpRequestContextBaseAdapter adapter;

	private HttpRequestContextBase base;

	@Before
	public void setup() {
		base = Mockito.mock(HttpRequestContextBase.class);
		adapter = new HttpRequestContextBaseAdapter(base);
	}

	@Test
	public void testHasResponse() throws IOException {
		Assert.assertFalse(adapter.hasResponse());
		byte[] data = new byte[0];
		adapter.setResponse(12, data);
		Mockito.verify(base, Mockito.times(1)).setResponse(Mockito.eq(12), Mockito.eq(data));
		Assert.assertTrue(adapter.hasResponse());
	}

	@Test
	public void testAccepts() throws IOException {
		Mockito.when(base.getRequestHeader(Mockito.eq(HttpHeaders.HTTP_HEADER_ACCEPT))).thenReturn("text/html,application/json");
		Assert.assertTrue(adapter.accepts("text/html"));
		Assert.assertFalse(adapter.accepts("application/xy"));
		Assert.assertTrue(adapter.accepts("application/json"));
	}

	@Test
	public void testAcceptsAny() throws IOException {
		Mockito.when(base.getRequestHeader(Mockito.eq(HttpHeaders.HTTP_HEADER_ACCEPT))).thenReturn("text/html,application/json");
		Assert.assertFalse(adapter.acceptsAny());
		Mockito.when(base.getRequestHeader(Mockito.eq(HttpHeaders.HTTP_HEADER_ACCEPT))).thenReturn("application/json,*");
		Assert.assertTrue(adapter.acceptsAny());
		Mockito.when(base.getRequestHeader(Mockito.eq(HttpHeaders.HTTP_HEADER_ACCEPT))).thenReturn("application/json,*/*");
		Assert.assertTrue(adapter.acceptsAny());
	}

	@Test
	public void testAcceptsAllIfNoHeader() throws IOException {
		Mockito.when(base.getRequestHeader(Mockito.eq(HttpHeaders.HTTP_HEADER_ACCEPT))).thenReturn(null);
		Assert.assertFalse(adapter.accepts("text/html"));
		Assert.assertFalse(adapter.accepts("application/json"));
		Assert.assertTrue(adapter.acceptsAny());
	}

	@Test
	public void setContentType() throws IOException {
		adapter.setContentType("test");
		Mockito.verify(base, Mockito.times(1)).setResponseHeader(Mockito.eq(HttpHeaders.HTTP_CONTENT_TYPE), Mockito.eq("test"));
	}

	@Test
	public void getRequestHeader() throws IOException {
		adapter.getRequestHeader("a");
		Mockito.verify(base, Mockito.times(1)).getRequestHeader(Mockito.eq("a"));
	}

	@Test
	public void getRequestParameters() throws IOException {
		adapter.getRequestParameters();
		Mockito.verify(base, Mockito.times(1)).getRequestParameters();
	}

	@Test
	public void getPath() throws IOException {
		adapter.getPath();
		Mockito.verify(base, Mockito.times(1)).getPath();
	}

	@Test
	public void getBaseUrl() throws IOException {
		adapter.getBaseUrl();
		Mockito.verify(base, Mockito.times(2)).getBaseUrl();
	}

	@Test
	public void getRequestBody() throws IOException {
		adapter.getRequestBody();
		Mockito.verify(base, Mockito.times(1)).getRequestBody();
	}

	@Test
	public void getMethod() throws IOException {
		adapter.getMethod();
		Mockito.verify(base, Mockito.times(1)).getMethod();
	}

	@Test
	public void getResponseHeader() throws IOException {
		adapter.getResponseHeader("a");
		Mockito.verify(base, Mockito.times(1)).getResponseHeader(Mockito.eq("a"));
	}


	@Test
	public void setResponseHeader() throws IOException {
		adapter.setResponseHeader("a", "b");
		Mockito.verify(base, Mockito.times(1)).setResponseHeader(Mockito.eq("a"), Mockito.eq("b"));
	}

	@Test
	public void unwrap() throws IOException {
		Assert.assertSame(adapter, adapter.unwrap(HttpRequestContextBaseAdapter.class));
		Assert.assertSame(base, adapter.unwrap(base.getClass()));
		Assert.assertNull(adapter.unwrap(String.class));
	}
}