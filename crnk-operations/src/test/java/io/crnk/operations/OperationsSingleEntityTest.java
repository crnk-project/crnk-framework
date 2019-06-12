package io.crnk.operations;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.operations.client.OperationsCall;
import io.crnk.operations.client.OperationsClient;
import io.crnk.operations.model.MovieEntity;
import io.crnk.operations.model.VoteEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public class OperationsSingleEntityTest extends AbstractOperationsTest {

	protected ResourceRepository<MovieEntity, UUID> movieRepo;

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

	@Test
	public void testAutoIncrementCrud() {
		ResourceRepository<VoteEntity, Long> voteRepo = client.getRepositoryForType(VoteEntity.class);

		VoteEntity vote = new VoteEntity();
		vote.setNumStars(12);

		// post
		OperationsCall call = operationsClient.createCall();
		call.add(HttpMethod.POST, vote);
		call.execute();

		// read
		ResourceList<VoteEntity> votes = voteRepo.findAll(new QuerySpec(VoteEntity.class));
		Assert.assertEquals(1, votes.size());
		vote = votes.get(0);
		Assert.assertEquals(1, vote.getId().intValue());

		// update
		vote.setNumStars(13);
		call = operationsClient.createCall();
		call.add(HttpMethod.PATCH, vote);
		call.execute();
		vote = call.getResponseObject(0, VoteEntity.class);
		Assert.assertEquals(13, vote.getNumStars());
		Assert.assertEquals(1, vote.getId().intValue());

		// delete
		call = operationsClient.createCall();
		call.add(HttpMethod.DELETE, vote);
		call.execute();

		votes = voteRepo.findAll(new QuerySpec(VoteEntity.class));
		Assert.assertEquals(0, votes.size());
	}
}
