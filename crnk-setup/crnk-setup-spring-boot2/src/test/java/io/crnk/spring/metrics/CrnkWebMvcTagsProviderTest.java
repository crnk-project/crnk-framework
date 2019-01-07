package io.crnk.spring.metrics;

import com.google.common.collect.Iterators;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.spring.setup.boot.metrics.CrnkWebMvcTagsProvider;
import io.micrometer.core.instrument.Tag;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnitParamsRunner.class)
public class CrnkWebMvcTagsProviderTest {

    private ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);

    private CrnkWebMvcTagsProvider compositeTagsProvider = new CrnkWebMvcTagsProvider(resourceRegistry);

    @Test
    public void useFallbackIfNotCrnkResource() {
        when(resourceRegistry.getEntryByPath(anyString())).thenReturn(null);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingPattern", "/any");

        Iterable<Tag> tags = compositeTagsProvider.getTags(request, new MockHttpServletResponse(), mock(Object.class), mock(Throwable.class));

        assertEquals("/any", Iterators.get(tags.iterator(), 1).getValue());
    }

    @SuppressWarnings("unused")
    private Object[] handleCrnkResourceParameters() {
    	String id = UUID.randomUUID().toString();

        return new Object[] {
            new Object[] {
                "/users",
                "/users"
            },
            new Object[] {
                "/users/" + id,
                "/users/{id}"
            },
            new Object[] {
                "/users/" + id + "/attribute",
                "/users/{id}/attribute"
            },
            new Object[] {
                "/users/" + id + "/relationships/relation",
                "/users/{id}/relationships/relation"
            }
        };
    }

    @Test
    @Parameters(method = "handleCrnkResourceParameters")
    public void handleCrnkResource(final String requestUrl, final String expected) {
        when(resourceRegistry.getEntryByPath(anyString())).thenReturn(mock(RegistryEntry.class));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(requestUrl);

        Iterable<Tag> tags = compositeTagsProvider.getTags(request, new MockHttpServletResponse(), mock(Object.class), mock(Throwable.class));

        assertEquals(expected, Iterators.get(tags.iterator(), 1).getValue());
    }
}
