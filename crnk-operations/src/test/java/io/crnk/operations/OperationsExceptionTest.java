package io.crnk.operations;

import java.util.List;
import java.util.UUID;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.exception.InternalServerErrorException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.operations.client.OperationsCall;
import io.crnk.operations.client.OperationsClient;
import io.crnk.operations.model.MovieEntity;
import io.crnk.operations.model.PersonEntity;
import io.crnk.operations.server.OperationFilter;
import io.crnk.operations.server.OperationFilterChain;
import io.crnk.operations.server.OperationFilterContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class OperationsExceptionTest extends AbstractOperationsTest {


	private OperationsClient operationsClient;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		operationsClient = new OperationsClient(client);
	}

	@Test
	public void testValidationErrorCancelsPatch() {
		PersonEntity person1 = newPerson("1");
		PersonEntity person2 = newPerson("2");

		person1.setName(null); // trigger validation error

		OperationsCall insertCall = operationsClient.createCall();
		insertCall.add(HttpMethod.POST, person1);
		insertCall.add(HttpMethod.POST, person2);
		insertCall.execute();

		Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY_422, insertCall.getResponse(0).getStatus());
		Assert.assertEquals(HttpStatus.PRECONDITION_FAILED_412, insertCall.getResponse(1).getStatus());

		QuerySpec querySpec = new QuerySpec(PersonEntity.class);
		ResourceRepositoryV2<PersonEntity, UUID> personRepo = client.getRepositoryForType(PersonEntity.class);
		List<PersonEntity> list = personRepo.findAll(querySpec);
		Assert.assertEquals(0, list.size());
	}

	@Test
	public void testUnknownErrorTriggerInternalError() {
		operationsModule.addFilter(new OperationFilter() {
			@Override
			public List<OperationResponse> filter(OperationFilterContext context, OperationFilterChain chain) {
				throw new IllegalStateException("test");
			}
		});

		MovieEntity movie1 = newMovie("1");
		MovieEntity movie2 = newMovie("2");

		OperationsCall insertCall = operationsClient.createCall();
		insertCall.add(HttpMethod.POST, movie1);
		insertCall.add(HttpMethod.POST, movie2);

		try {
			insertCall.execute();
			Assert.fail();
		}
		catch (InternalServerErrorException e) {
			// ok
		}
	}

}
