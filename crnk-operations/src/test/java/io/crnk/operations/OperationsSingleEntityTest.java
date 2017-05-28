package io.crnk.operations;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.operations.client.OperationsCall;
import io.crnk.operations.client.OperationsClient;
import io.crnk.operations.model.MovieEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public class OperationsSingleEntityTest extends AbstractOperationsTest {

	protected ResourceRepositoryV2<MovieEntity, UUID> movieRepo;

	private OperationsClient operationsClient;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		movieRepo = client.getRepositoryForType(MovieEntity.class);
		operationsClient = new OperationsClient(client);
	}


	@Test
	public void testSingleEntityCrud() {
		MovieEntity movie = newMovie("test");

		// post
		OperationsCall call = operationsClient.createCall();
		call.add(HttpMethod.POST, movie);
		call.execute();

		// read
		ResourceList<MovieEntity> movies = movieRepo.findAll(new QuerySpec(MovieEntity.class));
		Assert.assertEquals(1, movies.size());
		movie = movies.get(0);


		// update
		movie.setTitle("NewTitle");
		call = operationsClient.createCall();
		call.add(HttpMethod.PATCH, movie);
		call.execute();
		movie = call.getResponseObject(0, MovieEntity.class);
		Assert.assertEquals("NewTitle", movie.getTitle());

		// delete
		call = operationsClient.createCall();
		call.add(HttpMethod.DELETE, movie);
		call.execute();

		movies = movieRepo.findAll(new QuerySpec(MovieEntity.class));
		Assert.assertEquals(0, movies.size());
	}
}
