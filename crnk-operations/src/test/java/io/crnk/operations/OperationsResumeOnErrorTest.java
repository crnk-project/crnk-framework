package io.crnk.operations;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.operations.client.OperationsCall;
import io.crnk.operations.client.OperationsClient;
import io.crnk.operations.model.PersonEntity;
import io.crnk.operations.server.OperationsModule;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class OperationsResumeOnErrorTest extends AbstractOperationsTest {

    protected ResourceRepository<PersonEntity, UUID> personRepo;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        personRepo = client.getRepositoryForType(PersonEntity.class);
    }

    @Override
    protected boolean isTransactional() {
        return false;
    }

    @Test
    public void testDefaultBehaviorResumeOnError() {
        // Create person entities.
        PersonEntity person1 = newPerson("1");
        PersonEntity person2 = newPerson("2");
        PersonEntity person3 = newPerson("3");

        // Cause a validation error.
        person2.setName(null);

        // Create operation and execute it.
        OperationsClient operationsClient = new OperationsClient(client);
        OperationsCall call = operationsClient.createCall();
        call.add(HttpMethod.POST, person1);
        call.add(HttpMethod.POST, person2);
        call.add(HttpMethod.POST, person3);
        call.execute();

        // Only one person should exist since it should not resume on error.
        QuerySpec querySpec = new QuerySpec(PersonEntity.class);
        ResourceList<PersonEntity> persons = personRepo.findAll(querySpec);
        assertEquals(1, persons.size());
    }

    @Test
    public void testActivateResumeOnError() {
        // Activate the setResumeOnError.
        operationsModule.setResumeOnError(true);

        // Create person entities.
        PersonEntity person1 = newPerson("1");
        PersonEntity person2 = newPerson("2");
        PersonEntity person3 = newPerson("3");

        // Cause a validation error.
        person2.setName(null);

        // Create operation and execute it.
        OperationsClient operationsClient = new OperationsClient(client);
        OperationsCall call = operationsClient.createCall();
        call.add(HttpMethod.POST, person1);
        call.add(HttpMethod.POST, person2);
        call.add(HttpMethod.POST, person3);
        call.execute();

        // Only two people should exist since it will resume the rest of the operation.
        QuerySpec querySpec = new QuerySpec(PersonEntity.class);
        ResourceList<PersonEntity> persons = personRepo.findAll(querySpec);
        assertEquals(2, persons.size());

        // Reset the settings for other tests.
        operationsModule = OperationsModule.create();
    }

}
