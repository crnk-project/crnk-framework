package io.crnk.operations;

import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.operations.client.OperationsCall;
import io.crnk.operations.client.OperationsClient;
import io.crnk.operations.model.MovieEntity;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public class OperationsExceptionTest extends AbstractOperationsTest {

	protected ResourceRepositoryV2<MovieEntity, UUID> movieRepo;

	private OperationsClient operationsClient;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		movieRepo = client.getRepositoryForType(MovieEntity.class);
		operationsClient = new OperationsClient(client);
	}

	@Test(expected = IllegalStateException.class)
	public void testEarlygetResponseMethodAccessShouldThrowException() {
		OperationsCall call = operationsClient.createCall();
		call.getResponse(0);
	}


	@Test(expected = IllegalStateException.class)
	public void testEarlygetResponseObjectMethodAccessShouldThrowException() {
		OperationsCall call = operationsClient.createCall();
		call.getResponseObject(0, Object.class);
	}

}
