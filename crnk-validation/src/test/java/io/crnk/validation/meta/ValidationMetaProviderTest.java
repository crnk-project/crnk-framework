package io.crnk.validation.meta;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.module.SimpleModule;
import io.crnk.meta.MetaLookupImpl;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.resource.MetaResourceBase;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.rs.internal.JaxrsModule;
import io.crnk.validation.mock.models.Task;
import io.crnk.validation.mock.repository.ProjectRepository;
import io.crnk.validation.mock.repository.ScheduleRepository;
import io.crnk.validation.mock.repository.TaskRepository;
import org.junit.Assert;
import org.junit.Test;

public class ValidationMetaProviderTest {

    private MetaLookupImpl lookup;

    private ResourceMetaProvider resourceMetaProvider;

    private void setup(boolean addValidationProvider) {
        SimpleModule testModule = new SimpleModule("test");
        testModule.addRepository(new ProjectRepository());
        testModule.addRepository(new ScheduleRepository());
        testModule.addRepository(new TaskRepository());

        CrnkBoot boot = new CrnkBoot();
        boot.addModule(new JaxrsModule(null));
        boot.addModule(testModule);
        boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
        boot.boot();

        resourceMetaProvider = new ResourceMetaProvider();
        lookup = new MetaLookupImpl();
        lookup.setModuleContext(boot.getModuleRegistry().getContext());
        lookup.addProvider(resourceMetaProvider);
        if (addValidationProvider) {
            lookup.addProvider(new ValidationMetaProvider());
        }
        lookup.initialize();
    }

    @Test
    public void testNotNullNotDisabledWithoutValidationProvider() {
        setup(false);
        MetaResourceBase meta = resourceMetaProvider.getMeta(Task.class);
        MetaAttribute attr = meta.getAttribute("name");
        Assert.assertTrue(attr.isNullable());
    }

    @Test
    public void testNotNullDisablesNullablity() {
        setup(true);
        MetaResourceBase meta = resourceMetaProvider.getMeta(Task.class);
        MetaAttribute attr = meta.getAttribute("name");
        Assert.assertFalse(attr.isNullable());
    }
}
