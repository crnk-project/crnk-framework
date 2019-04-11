package io.crnk.rs;

import io.crnk.core.engine.http.HttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.util.Arrays;

public class JaxrsRequestContextTest {


    private CrnkFeature feature;

    private ContainerRequestContext requestContext;

    private JaxrsRequestContext context;

    private javax.ws.rs.core.UriInfo uriInfo;

    @Before
    public void setup() {
        FeatureContext featureContext = Mockito.mock(FeatureContext.class);
        Mockito.when(featureContext.getConfiguration()).thenReturn(Mockito.mock(Configuration.class));

        feature = new CrnkFeature();
        feature.configure(featureContext);

        uriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfo.getQueryParameters()).thenReturn(Mockito.mock(MultivaluedMap.class));

        requestContext = Mockito.mock(ContainerRequestContext.class);
        Mockito.when(requestContext.getUriInfo()).thenReturn(uriInfo);
        context = new JaxrsRequestContext(requestContext, feature);
    }

    @Test
    public void testGetter() {
        HttpResponse response = new HttpResponse();
        response.setHeader("a", "b");
        context.setResponse(response);
        Assert.assertEquals("b", context.getResponse().getHeader("a"));
    }

    @Test
    public void testRepeatedGetBody() {
        byte[] body = "Hello World".getBytes();
        Mockito.when(requestContext.getEntityStream()).thenReturn(new ByteArrayInputStream(body));

        byte[] copy1 = context.getRequestBody();
        byte[] copy2 = context.getRequestBody();
        Assert.assertSame(copy1, copy2);
        Assert.assertTrue(Arrays.equals(copy1, body));
    }


}
