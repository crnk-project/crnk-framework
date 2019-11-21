package io.crnk.operations;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.operations.client.OperationsCall;
import io.crnk.operations.client.OperationsClient;
import io.crnk.operations.model.MovieEntity;
import io.crnk.operations.model.PersonEntity;
import io.crnk.rs.CrnkFeature;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.models.TaskStatus;
import io.crnk.test.mock.repository.BulkInMemoryRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class OperationsPatchTest extends AbstractOperationsTest {

    protected ResourceRepository<MovieEntity, UUID> movieRepo;

	private BulkInMemoryRepository bulkRepository;


    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        movieRepo = client.getRepositoryForType(MovieEntity.class);
	}

	@Override
	public void setupServer(CrnkFeature feature) {
		super.setupServer(feature);

		bulkRepository = Mockito.spy(new BulkInMemoryRepository(Task.class));
		SimpleModule testModule = new SimpleModule("test");
		testModule.addRepository(bulkRepository);
		testModule.addRepository(new InMemoryResourceRepository<>(Project.class));
		testModule.addRepository(new InMemoryResourceRepository<>(Schedule.class));
		feature.addModule(testModule);

		operationsModule.setIncludeChangedRelationships(false);
    }

    @Test
	public void checkPatch() {
        ResourceRepository<PersonEntity, UUID> personRepo = client.getRepositoryForType(PersonEntity.class);

        PersonEntity person1 = newPerson("1");
        PersonEntity person2 = newPerson("2");
        MovieEntity movie = newMovie("test");
        movie.setDirectors(new HashSet<>(Arrays.asList(person1, person2)));

        // tag::client[]
        OperationsClient operationsClient = new OperationsClient(client);
        OperationsCall call = operationsClient.createCall();
        call.add(HttpMethod.POST, movie);
        call.add(HttpMethod.POST, person1);
        call.add(HttpMethod.POST, person2);
        call.execute();
        // end::client[]


        QuerySpec querySpec = new QuerySpec(PersonEntity.class);
        ResourceList<PersonEntity> persons = personRepo.findAll(querySpec);
        Assert.assertEquals(2, persons.size());

        querySpec = new QuerySpec(MovieEntity.class);
        querySpec.includeRelation(Arrays.asList("directors"));
        ResourceList<MovieEntity> movies = movieRepo.findAll(querySpec);
        Assert.assertEquals(1, movies.size());
        movie = movies.get(0);
        Assert.assertEquals(2, movie.getDirectors().size());
    }

    @Test
    public void verifyAtomicUponPatchFailure() {
        ResourceRepository<PersonEntity, UUID> personRepo = client.getRepositoryForType(PersonEntity.class);

        PersonEntity person1 = newPerson("1");
        PersonEntity person2 = newPerson("2");
        person2.setName(null);

        OperationsClient operationsClient = new OperationsClient(client);
        OperationsCall call = operationsClient.createCall();
        call.add(HttpMethod.POST, person1);
        call.add(HttpMethod.POST, person2);
        call.execute();
        Assert.assertEquals(HttpStatus.CREATED_201, call.getResponse(0).getStatus());
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY_422, call.getResponse(1).getStatus());

        QuerySpec querySpec = new QuerySpec(PersonEntity.class);
        ResourceList<PersonEntity> persons = personRepo.findAll(querySpec);
        Assert.assertEquals(0, persons.size());
    }

	@Test
	public void checkExperimentalBulkPatch() {
		Task task1 = newTask("1");
		Task task2 = newTask("2");
		task1.setStatus(TaskStatus.OPEN);

		OperationsClient operationsClient = new OperationsClient(client);
		OperationsCall call = operationsClient.createCall();
		call.add(HttpMethod.POST, task1);
		call.add(HttpMethod.POST, task2);
		call.execute();

		task1.setName("new 1");
    task2.setName("new 2");
    call = operationsClient.createCall();
    call.add(HttpMethod.PATCH, task1);
		call.add(HttpMethod.PATCH, task2);
		call.execute();


		QuerySpec querySpec = new QuerySpec(Task.class);
		ResourceList<Task> tasks = bulkRepository.findAll(querySpec);
		Assert.assertEquals(2, tasks.size());

		ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.verify(bulkRepository, Mockito.times(1)).create(argumentCaptor.capture());
		List capture = argumentCaptor.getValue();
		Assert.assertEquals(2, capture.size());
	}
}
