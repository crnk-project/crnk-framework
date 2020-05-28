package io.crnk.gen.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.gen.base.GeneratorConfig;
import io.crnk.gen.runtime.spring.SpringMetaResolver;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.MetaLookupImpl;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.spring.setup.boot.core.CrnkCoreAutoConfiguration;
import io.crnk.test.mock.TestModule;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.util.List;

public class SpringMetaResolverTest {

    @Test
    public void test() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        SpringMetaResolver resolver = new SpringMetaResolver();

        GeneratorConfig config = new GeneratorConfig();
        config.getRuntime().getSpring().setConfiguration(TestConfiguration.class.getName());

        ArgumentCaptor<MetaLookup> captor = ArgumentCaptor.forClass(MetaLookup.class);
        RuntimeContext context = Mockito.mock(RuntimeContext.class);
        Mockito.when(context.getConfig()).thenReturn(config);
        resolver.run(context, classLoader);
        Mockito.verify(context).generate(captor.capture());

        MetaLookup lookup = captor.getValue();
        List<MetaResource> resources = lookup.findElements(MetaResource.class);
        Assert.assertNotEquals(0, resources.stream().filter(it -> it.getName().contains("Task")).count());
    }

    @Configuration
    @Import(CrnkCoreAutoConfiguration.class)
    public static class TestConfiguration {

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public TestModule module() {
            return new TestModule();
        }

    }
}
