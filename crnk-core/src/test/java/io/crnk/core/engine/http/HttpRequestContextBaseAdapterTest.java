package io.crnk.core.engine.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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

        HttpResponse response = new HttpResponse();
        response.setBody(data);
        response.setStatusCode(12);
        adapter.setResponse(response);
        Assert.assertTrue(adapter.hasResponse());
    }

    @Test
    public void testAccepts() {
        Mockito.when(base.getRequestHeader(Mockito.eq(HttpHeaders.HTTP_HEADER_ACCEPT))).thenReturn("text/html,application/json");
        Assert.assertTrue(adapter.accepts("text/html"));
        Assert.assertFalse(adapter.accepts("application/xy"));
        Assert.assertTrue(adapter.accepts("application/json"));
        Assert.assertEquals(-1, adapter.getQueryContext().getRequestVersion());
    }


    @Test
    public void testRequestVersionInAcceptHeader() {
        Mockito.when(base.getRequestHeader(Mockito.eq(HttpHeaders.HTTP_HEADER_ACCEPT))).thenReturn("text/html,application/json;version=3");

        adapter = new HttpRequestContextBaseAdapter(base);
        Assert.assertFalse(adapter.accepts("application/xy"));
        Assert.assertTrue(adapter.accepts("application/json"));
        Assert.assertEquals(3, adapter.getQueryContext().getRequestVersion());
    }

	@Test
	public void testRequestVersionInAcceptHeaderInMiddle() {
		Mockito.when(base.getRequestHeader(Mockito.eq(HttpHeaders.HTTP_HEADER_ACCEPT))).thenReturn("application/json;version=3,text/html");

		adapter = new HttpRequestContextBaseAdapter(base);
		Assert.assertFalse(adapter.accepts("application/xy"));
		Assert.assertTrue(adapter.accepts("application/json"));
		Assert.assertEquals(3, adapter.getQueryContext().getRequestVersion());
	}

    @Test
    public void testRequestVersionAsParameter() {
        Map<String, Set<String>> parameters = new HashMap<>();
        parameters.put(HttpHeaders.VERSION_ACCEPT_PARAMETER, Sets.newHashSet("3"));
        Mockito.when(base.getRequestHeader(Mockito.eq(HttpHeaders.HTTP_HEADER_ACCEPT))).thenReturn("text/html,application/json");
        Mockito.when(base.getRequestParameters()).thenReturn(parameters);

        adapter = new HttpRequestContextBaseAdapter(base);
        Assert.assertFalse(adapter.accepts("application/xy"));
        Assert.assertTrue(adapter.accepts("application/json"));
        Assert.assertEquals(3, adapter.getQueryContext().getRequestVersion());
    }

    @Test
    public void testAcceptsAny() {
        Mockito.when(base.getRequestHeader(Mockito.eq(HttpHeaders.HTTP_HEADER_ACCEPT))).thenReturn("text/html,application/json");
        Assert.assertFalse(adapter.acceptsAny());
        Mockito.when(base.getRequestHeader(Mockito.eq(HttpHeaders.HTTP_HEADER_ACCEPT))).thenReturn("application/json,*");
        Assert.assertTrue(adapter.acceptsAny());
        Mockito.when(base.getRequestHeader(Mockito.eq(HttpHeaders.HTTP_HEADER_ACCEPT))).thenReturn("application/json,*/*");
        Assert.assertTrue(adapter.acceptsAny());
    }

    @Test
    public void testAcceptsAllIfNoHeader() {
        Mockito.when(base.getRequestHeader(Mockito.eq(HttpHeaders.HTTP_HEADER_ACCEPT))).thenReturn(null);
        Assert.assertFalse(adapter.accepts("text/html"));
        Assert.assertFalse(adapter.accepts("application/json"));
        Assert.assertTrue(adapter.acceptsAny());
    }

    @Test
    public void getRequestHeader() {
        adapter.getRequestHeader("a");
        Mockito.verify(base, Mockito.times(1)).getRequestHeader(Mockito.eq("a"));
    }

    @Test
    public void getRequestParameters() {
        adapter.getRequestParameters();
        Mockito.verify(base, Mockito.times(2)).getRequestParameters(); // version + this request
    }

    @Test
    public void getPath() {
        adapter.getPath();
        Mockito.verify(base, Mockito.times(2)).getPath();
    }

    @Test
    public void getBaseUrl() {
        adapter.getBaseUrl();
        Mockito.verify(base, Mockito.times(2)).getBaseUrl();
    }

    @Test
    public void getRequestBody() {
        adapter.getRequestBody();
        Mockito.verify(base, Mockito.times(1)).getRequestBody();
    }

    @Test
    public void getMethod() {
        adapter.getMethod();
        Mockito.verify(base, Mockito.times(1)).getMethod();
    }

    @Test
    public void unwrap() {
        Assert.assertSame(adapter, adapter.unwrap(HttpRequestContextBaseAdapter.class));
        Assert.assertSame(base, adapter.unwrap(base.getClass()));
        Assert.assertNull(adapter.unwrap(String.class));
    }
}