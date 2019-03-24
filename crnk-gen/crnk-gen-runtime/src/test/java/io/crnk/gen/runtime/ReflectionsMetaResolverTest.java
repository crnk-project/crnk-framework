package io.crnk.gen.runtime;

import io.crnk.gen.base.GeneratorConfig;
import io.crnk.gen.runtime.reflections.ReflectionsMetaResolver;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.resource.MetaResource;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ReflectionsMetaResolverTest {

    @Test
    public void test() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        ReflectionsMetaResolver resolver = new ReflectionsMetaResolver();

        GeneratorConfig config = new GeneratorConfig();
        config.setResourcePackages(Arrays.asList("io.crnk.test.mock"));

        ArgumentCaptor<MetaLookup> captor = ArgumentCaptor.forClass(MetaLookup.class);
        RuntimeContext context = Mockito.mock(RuntimeContext.class);
        Mockito.when(context.getConfig()).thenReturn(config);
        resolver.run(context, classLoader);
        Mockito.verify(context).generate(captor.capture());

        MetaLookup lookup = captor.getValue();
        List<MetaResource> resources = lookup.findElements(MetaResource.class);
        Assert.assertNotEquals(0, resources.stream().filter(it -> it.getName().contains("Task")).count());
    }
}
