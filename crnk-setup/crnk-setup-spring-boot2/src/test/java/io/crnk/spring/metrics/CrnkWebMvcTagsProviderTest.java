package io.crnk.spring.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Iterators;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.spring.setup.boot.monitor.CrnkWebMvcTagsProvider;
import io.crnk.test.mock.TestModule;
import io.micrometer.core.instrument.Tag;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(JUnitParamsRunner.class)
public class CrnkWebMvcTagsProviderTest {

	private ResourceRegistry resourceRegistry;

	private CrnkBoot boot;

	private CrnkWebMvcTagsProvider compositeTagsProvider;

	@Before
	public void setup() {
		boot = new CrnkBoot();
		boot.addModule(new TestModule());
		boot.boot();
		compositeTagsProvider = new CrnkWebMvcTagsProvider(boot);
	}

	@Test
	public void useFallbackIfNotCrnkResource() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingPattern", "/any");

		Iterable<Tag> tags = compositeTagsProvider.getTags(request, new MockHttpServletResponse(), mock(Object.class), mock(Throwable.class));

		assertEquals("/any", Iterators.get(tags.iterator(), 1).getValue());
	}

	@SuppressWarnings("unused")
	private Object[] handleCrnkResourceParameters() {
		String id = "124";

		return new Object[] {
				new Object[] {
						"/tasks",
						"/tasks"
				},
				new Object[] {
						"/tasks/" + id,
						"/tasks/{id}"
				},
				new Object[] {
						"/tasks/" + id + "/name",
						"/tasks/{id}/name"
				},
				new Object[] {
						"/tasks/" + id + "/relationships/project",
						"/tasks/{id}/relationships/project"
				}
		};
	}

	@Test
	@Parameters(method = "handleCrnkResourceParameters")
	public void handleCrnkResource(final String requestUrl, final String expected) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI(requestUrl);

		Iterable<Tag> tags = compositeTagsProvider.getTags(request, new MockHttpServletResponse(), mock(Object.class), mock(Throwable.class));

		assertEquals(expected, Iterators.get(tags.iterator(), 1).getValue());
	}
}
