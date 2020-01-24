package io.crnk.operations;

import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilter;
import io.crnk.core.engine.filter.ResourceFilterContext;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.module.SimpleModule;
import io.crnk.operations.client.OperationsCall;
import io.crnk.operations.client.OperationsClient;
import io.crnk.operations.model.PersonEntity;
import io.crnk.rs.CrnkFeature;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class OperationsSecurityTest extends AbstractOperationsTest {


    private OperationsClient operationsClient;

    private boolean active = false;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        operationsClient = new OperationsClient(client);
    }

    @Override
    protected void setupServer(CrnkFeature feature) {
        SimpleModule module = new SimpleModule("security");
        module.addResourceFilter(new ResourceFilter() {
            @Override
            public FilterBehavior filterResource(ResourceFilterContext context, ResourceInformation resourceInformation, HttpMethod method) {
                return active ? FilterBehavior.FORBIDDEN : FilterBehavior.NONE;
            }

            @Override
            public FilterBehavior filterField(ResourceFilterContext context, ResourceField field, HttpMethod method) {
                return FilterBehavior.NONE;
            }
        });
        feature.addModule(module);
    }

    @Test
    public void checkForbidden() {
        active = true;
        PersonEntity person1 = newPerson("1");
        PersonEntity person2 = newPerson("2");

        OperationsCall insertCall = operationsClient.createCall();
        insertCall.add(HttpMethod.POST, person1);
        insertCall.add(HttpMethod.POST, person2);
        try {
            insertCall.execute();
            Assert.fail();
        } catch (ForbiddenException e) {
            // ok
        }
    }


    @Test
    public void checkAllowed() {
        active = false;
        PersonEntity person1 = newPerson("1");
        PersonEntity person2 = newPerson("2");

        OperationsCall insertCall = operationsClient.createCall();
        insertCall.add(HttpMethod.POST, person1);
        insertCall.add(HttpMethod.POST, person2);
        insertCall.execute();
    }
}
