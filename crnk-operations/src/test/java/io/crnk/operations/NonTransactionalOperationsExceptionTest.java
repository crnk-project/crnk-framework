package io.crnk.operations;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.operations.client.OperationsCall;
import io.crnk.operations.client.OperationsClient;
import io.crnk.operations.model.PersonEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

public class NonTransactionalOperationsExceptionTest extends AbstractOperationsTest {

    private OperationsClient operationsClient;

    @Override
    protected boolean isTransactional() {
        return false;
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        operationsClient = new OperationsClient(client);
    }

    @Test
    public void testResumeOnError() {
        operationsModule.setResumeOnError(true);

        PersonEntity person1 = newPerson("1");
        PersonEntity person2 = newPerson("2");

        person1.setName(null); // trigger validation error

        OperationsCall insertCall = operationsClient.createCall();
        insertCall.add(HttpMethod.POST, person1);
        insertCall.add(HttpMethod.POST, person2);
        insertCall.execute();

        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY_422, insertCall.getResponse(0).getStatus());
        Assert.assertEquals(HttpStatus.CREATED_201, insertCall.getResponse(1).getStatus());

        QuerySpec querySpec = new QuerySpec(PersonEntity.class);
        ResourceRepository<PersonEntity, UUID> personRepo = client.getRepositoryForType(PersonEntity.class);
        List<PersonEntity> list = personRepo.findAll(querySpec);
        Assert.assertEquals(1, list.size());
    }
}
