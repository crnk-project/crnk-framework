package io.crnk.rs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.rs.internal.legacy.JaxrsParameterProvider;
import io.crnk.rs.internal.legacy.RequestContextParameterProviderRegistry;
import io.crnk.rs.internal.legacy.RequestContextParameterProviderRegistryBuilder;
import io.crnk.rs.resource.provider.AuthRequest;
import io.crnk.rs.resource.provider.Foo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.CookieParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JaxrsParameterProviderTest {

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private ContainerRequestContext requestContext;

	@Mock
	private UriInfo uriInfo;

	@Mock
	private MultivaluedMap<String, String> queryParams;

	private JaxrsParameterProvider sut;

	private Method testMethod;

	@Before
	public void setUp() throws Exception {
		RequestContextParameterProviderRegistry parameterProviderRegistry = buildParameterProviderRegistry(getServiceDiscovery());
		sut = new JaxrsParameterProvider(objectMapper, requestContext, parameterProviderRegistry);

		for (Method method : TestClass.class.getDeclaredMethods()) {
			if ("testMethod".equals(method.getName())) {
				testMethod = method;
			}
		}
	}

	private ServiceDiscovery getServiceDiscovery() {
		return new ReflectionsServiceDiscovery("io.crnk.rs.resource", new SampleJsonServiceLocator());
	}

	private RequestContextParameterProviderRegistry buildParameterProviderRegistry(ServiceDiscovery serviceDiscovery) {
		RequestContextParameterProviderRegistryBuilder builder = new RequestContextParameterProviderRegistryBuilder();
		return builder.build(serviceDiscovery);
	}

	@Test
	public void onContainerRequestContextParameterShouldReturnThisInstance() throws Exception {
		// WHEN
		Object result = sut.provide(testMethod, 0);

		// THEN
		assertThat(result).isEqualTo(requestContext);
	}

	@Test
	public void onSecurityContextParameterShouldReturnThisInstance() throws Exception {
		// GIVEN
		SecurityContext securityContext = mock(SecurityContext.class);
		when(requestContext.getSecurityContext()).thenReturn(securityContext);

		// WHEN
		Object result = sut.provide(testMethod, 1);

		// THEN
		verify(requestContext).getSecurityContext();
		assertThat(result).isEqualTo(securityContext);
	}

	@Test
	public void onObjectCookieShouldReturnThisInstance() throws Exception {
		// GIVEN
		Cookie cookie = new Cookie("sid", "123");
		when(requestContext.getCookies()).thenReturn(Collections.singletonMap("sid", cookie));

		// WHEN
		Object result = sut.provide(testMethod, 2);

		// THEN
		verify(requestContext).getCookies();
		assertThat(result).isEqualTo(cookie);
	}

	@Test
	public void onStringCookieShouldReturnThisInstance() throws Exception {
		// GIVEN
		when(requestContext.getCookies()).thenReturn(Collections.singletonMap("sid", new Cookie("sid", "123")));

		// WHEN
		Object result = sut.provide(testMethod, 3);

		// THEN
		verify(requestContext).getCookies();
		assertThat(result).isEqualTo("123");
	}

	@Test
	public void onLongCookieShouldReturnThisInstance() throws Exception {
		// GIVEN
		when(requestContext.getCookies()).thenReturn(Collections.singletonMap("sid", new Cookie("sid", "123")));
		when(objectMapper.readValue(any(String.class), any(Class.class))).thenReturn(123L);

		// WHEN
		Object result = sut.provide(testMethod, 4);

		// THEN
		verify(requestContext).getCookies();
		verify(objectMapper).readValue("123", Long.class);
		assertThat(result).isEqualTo(123L);
	}

	@Test
	public void onStringHeaderShouldReturnThisInstance() throws Exception {
		// GIVEN
		UUID uuid = UUID.randomUUID();
		when(requestContext.getHeaderString(anyString())).thenReturn(uuid.toString());

		// WHEN
		Object result = sut.provide(testMethod, 5);

		// THEN
		verify(requestContext).getHeaderString("cid");
		assertThat(result).isEqualTo(uuid.toString());
	}

	@Test
	public void onUuidHeaderShouldReturnThisInstance() throws Exception {
		// GIVEN
		UUID uuid = UUID.randomUUID();
		when(requestContext.getHeaderString(anyString())).thenReturn(uuid.toString());
		when(objectMapper.readValue(any(String.class), any(Class.class))).thenReturn(uuid);

		// WHEN
		Object result = sut.provide(testMethod, 6);

		// THEN
		verify(requestContext).getHeaderString("cid");
		verify(objectMapper).readValue(uuid.toString(), UUID.class);
		assertThat(result).isEqualTo(uuid);
	}

	@Test
	public void onStringFooShouldReturnThisInstance() throws Exception {

		// WHEN
		Object result = sut.provide(testMethod, 7);

		// THEN
		assertThat(result).isEqualTo("foo");
	}

	@Test
	public void onAuthRequestShouldReturnThisInstance() throws Exception {
		// GIVEN
		AuthRequest authRequest = new AuthRequest("Basic", "abc:123");
		when(requestContext.getHeaderString("Authorization")).thenReturn("Basic abc:123");

		// WHEN
		Object result = sut.provide(testMethod, 8);

		// THEN
		verify(requestContext).getHeaderString("Authorization");
		assertThat(result).isEqualTo(authRequest);
	}

	@Test
	public void onSingleValuedQueryParameterShouldReturnThisInstance() throws Exception {
		List<String> singleValue1 = Arrays.asList("singleValue1");
		List<String> singleValue2 = Arrays.asList("singleValue2");

		when(queryParams.get("singleValuedParam1")).thenReturn(singleValue1);
		when(queryParams.get("singleValuedParam2")).thenReturn(singleValue2);
		when(uriInfo.getQueryParameters()).thenReturn(queryParams);
		when(requestContext.getUriInfo()).thenReturn(uriInfo);

		// WHEN
		Object result1 = sut.provide(testMethod, 9);
		Object result2 = sut.provide(testMethod, 10);

		// THEN
		verify(queryParams).get("singleValuedParam1");
		verify(queryParams).get("singleValuedParam2");
		assertThat(result1).isEqualTo(singleValue1.get(0));
		assertThat(result2).isEqualTo(singleValue2.get(0));
	}

	@Test
	public void onMultiValuedQueryParameterShouldReturnThisInstance() throws Exception {
		List<String> values = Arrays.asList("value1", "value2");
		when(queryParams.get("multiValuedParam")).thenReturn(values);
		when(uriInfo.getQueryParameters()).thenReturn(queryParams);
		when(requestContext.getUriInfo()).thenReturn(uriInfo);

		// WHEN
		Object result = sut.provide(testMethod, 11);

		// THEN
		verify(queryParams).get("multiValuedParam");
		assertThat(result).isEqualTo(values);
	}

	public static class TestClass {

		public void testMethod(ContainerRequestContext requestContext, SecurityContext securityContext,
							   @CookieParam("sid") Cookie objectCookie, @CookieParam("sid") String StringCookie,
							   @CookieParam("sid") Long longCookie, @HeaderParam("cid") String StringHeader, @HeaderParam("cid") UUID UuidHeader,
							   @Foo String foo, AuthRequest authRequest, @QueryParam("singleValuedParam1") String paramValue1,
							   @QueryParam("singleValuedParam2") String paramValue2, @QueryParam("multiValuedParam") List<String> paramValues) {
		}
	}
}
