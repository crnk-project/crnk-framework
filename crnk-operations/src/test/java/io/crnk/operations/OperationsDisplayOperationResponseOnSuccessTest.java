package io.crnk.operations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.operations.client.OperationsCall;
import io.crnk.operations.client.OperationsClient;
import io.crnk.operations.model.PersonEntity;
import io.crnk.operations.server.OperationsModule;

public class OperationsDisplayOperationResponseOnSuccessTest extends AbstractOperationsTest {

    protected ResourceRepository<PersonEntity, UUID> personRepo;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		personRepo = client.getRepositoryForType(PersonEntity.class);
    }
    
    @Test
    public void testDefaultBehaviorDisplayOperationResponseOnSuccessTest() {
        // Create person entities.
        PersonEntity person1 = newPerson("1");
        PersonEntity person2 = newPerson("2");
        PersonEntity person3 = newPerson("3");

        // Create operation and execute it.
        OperationsClient operationsClient = new OperationsClient(client);
		OperationsCall call = operationsClient.createCall();
		call.add(HttpMethod.POST, person1);
        call.add(HttpMethod.POST, person2);
        call.add(HttpMethod.POST, person3);
        call.execute();
        
        OperationResponse response = call.getResponse(0);
		assertTrue(response.getData().isPresent());
    }

    @Test
    public void testDeactivateDisplayOperationResponseOnSuccessTest() {
        // Deactivate the displayOperationResponseOnSuccess property.
        operationsModule.setDisplayOperationResponseOnSuccess(false);

        // Create person entities.
        PersonEntity person1 = newPerson("1");
        PersonEntity person2 = newPerson("2");
        PersonEntity person3 = newPerson("3");

        // Create operation and execute it.
        OperationsClient operationsClient = new OperationsClient(client);
		OperationsCall call = operationsClient.createCall();
		call.add(HttpMethod.POST, person1);
        call.add(HttpMethod.POST, person2);
        call.add(HttpMethod.POST, person3);
        call.execute();
        
        OperationResponse response = call.getResponse(0);
        assertFalse(response.getData().isPresent());
        
        // Reset the operations module...
        operationsModule = OperationsModule.create();
    }

}
