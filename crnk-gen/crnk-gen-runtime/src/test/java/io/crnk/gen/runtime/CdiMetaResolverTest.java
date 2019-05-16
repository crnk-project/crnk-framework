package io.crnk.gen.runtime;

import io.crnk.gen.base.GeneratorConfig;
import io.crnk.gen.runtime.cdi.CdiMetaResolver;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.resource.MetaResource;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

public class CdiMetaResolverTest {

    @Test
    public void testMetaModuleFound() {
        // test verifies 98% of functionality, a pain to setup
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            CdiMetaResolver resolver = new CdiMetaResolver();

            GeneratorConfig config = new GeneratorConfig();

            ArgumentCaptor<MetaLookup> captor = ArgumentCaptor.forClass(MetaLookup.class);
            RuntimeContext context = Mockito.mock(RuntimeContext.class);
            Mockito.when(context.getConfig()).thenReturn(config);
            Mockito.when(context.getClassLoader()).thenReturn(classLoader);
            resolver.run(context, classLoader);
            Mockito.verify(context).generate(captor.capture());

            MetaLookup lookup = captor.getValue();
            List<MetaResource> resources = lookup.findElements(MetaResource.class);
            Assert.assertNotEquals(0, resources.stream().filter(it -> it.getName().contains("Task")).count());
            Assert.fail();
        } catch (Exception e) {
            Throwable cause = e.getCause().getCause();
            Assert.assertTrue(cause.getMessage(), cause.getMessage().contains("add MetaModule to CDI setup"));
        }
    }
}
