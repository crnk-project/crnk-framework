package io.crnk.core.queryspec;

import io.crnk.core.engine.internal.registry.ResourceRegistryImpl;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.DefaultResourceRegistryPart;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import org.junit.Assert;
import org.junit.Test;

public class QuerySpecAdapterTest {


    @Test
    public void testSelfLink() {
        ModuleRegistry moduleRegistry = new ModuleRegistry();
        ResourceRegistry resourceRegistry = new ResourceRegistryImpl(new DefaultResourceRegistryPart(), moduleRegistry);

        QueryContext queryContext = new QueryContext();
        queryContext.setRequestPath("/relationships/any");

        QuerySpecAdapter adapter = new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry, queryContext);
        Assert.assertTrue(adapter.isSelfLink());

        queryContext.setRequestPath("/any");
        adapter = new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry, queryContext);
        Assert.assertFalse(adapter.isSelfLink());
    }
}
