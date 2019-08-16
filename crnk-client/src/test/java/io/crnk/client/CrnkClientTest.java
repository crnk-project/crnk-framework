package io.crnk.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.pagingspec.NumberSizePagingBehavior;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.test.mock.models.Schedule;
import org.junit.Assert;
import org.junit.Test;

public class CrnkClientTest extends AbstractClientTest {


    @Override
    protected TestApplication configure() {
        TestApplication app = new TestApplication();
        app.getFeature().addModule(NumberSizePagingBehavior.createModule());
        return app;
    }


    @Test
    public void testSetObjectMapper() {
        ObjectMapper myObjectMapper = new ObjectMapper();
        client = new CrnkClient(getBaseUri().toString()) {
            protected ObjectMapper createDefaultObjectMapper() {
                throw new IllegalStateException("this should not happer");
            }
        };
        client.setObjectMapper(myObjectMapper);
        Assert.assertSame(myObjectMapper, client.getObjectMapper());

        ResourceRepository<Schedule, Object> repository = client.getRepositoryForType(Schedule.class);
        repository.findAll(new QuerySpec(Schedule.class));
    }

}