package io.crnk.rs;

import java.io.IOException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class CrnkFilterTest {


	@Test
	public void checkExceptionsGetWrappedWithWebApplicationException() throws IOException {
		CrnkFeature feature = Mockito.mock(CrnkFeature.class);
		CrnkFilter filter = new CrnkFilter(feature);

		ContainerRequestContext requestContext = Mockito.mock(ContainerRequestContext.class);
		Mockito.when(requestContext.getUriInfo()).thenThrow(new RuntimeException("test"));
		try {
			filter.filter(requestContext);
			Assert.fail();
		}
		catch (WebApplicationException e) {
			Assert.assertEquals("test", e.getCause().getMessage());
		}
	}

	@Test
	public void checkWebApplicationExceptionDoNotGetWrappedWithWebApplicationException() throws IOException {
		CrnkFeature feature = Mockito.mock(CrnkFeature.class);
		CrnkFilter filter = new CrnkFilter(feature);

		ContainerRequestContext requestContext = Mockito.mock(ContainerRequestContext.class);
		Mockito.when(requestContext.getUriInfo()).thenThrow(new WebApplicationException("test"));
		try {
			filter.filter(requestContext);
			Assert.fail();
		}
		catch (WebApplicationException e) {
			Assert.assertEquals("test", e.getMessage());
			Assert.assertNull(e.getCause());
		}
	}
}
