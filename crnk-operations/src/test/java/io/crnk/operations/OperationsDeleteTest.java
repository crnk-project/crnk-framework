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
import io.crnk.test.mock.repository.BulkInMemoryRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class OperationsDeleteTest extends AbstractOperationsTest {

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
  public void checkDelete() {
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

    call = operationsClient.createCall();
    call.add(HttpMethod.DELETE, movie);
    call.add(HttpMethod.DELETE, person1);
    call.add(HttpMethod.DELETE, person2);
    call.execute();

    QuerySpec querySpec = new QuerySpec(PersonEntity.class);
    ResourceList<PersonEntity> persons = personRepo.findAll(querySpec);
    Assert.assertEquals(0, persons.size());

    querySpec = new QuerySpec(MovieEntity.class);
    querySpec.includeRelation(Arrays.asList("directors"));
    ResourceList<MovieEntity> movies = movieRepo.findAll(querySpec);
    Assert.assertEquals(0, movies.size());
  }

  @Test
  public void checkDeleteByPath() {
    ResourceRepository<PersonEntity, UUID> personRepo = client.getRepositoryForType(PersonEntity.class);

    PersonEntity person1 = newPerson("1");
    PersonEntity person2 = newPerson("2");

    OperationsClient operationsClient = new OperationsClient(client);
    OperationsCall call = operationsClient.createCall();
    call.add(HttpMethod.POST, person1);
    call.add(HttpMethod.POST, person2);
    call.execute();

    QuerySpec querySpecBeforeDelete = new QuerySpec(PersonEntity.class);
    ResourceList<PersonEntity> personsBeforeDelete = personRepo.findAll(querySpecBeforeDelete);
    Assert.assertEquals(2, personsBeforeDelete.size());

    call = operationsClient.createCall();
    call.add(HttpMethod.DELETE, "person/" + person1.getId());
    call.add(HttpMethod.DELETE, "person/" + person2.getId());
    call.execute();

    QuerySpec querySpecAfterDelete = new QuerySpec(PersonEntity.class);
    ResourceList<PersonEntity> personsAfterDelete = personRepo.findAll(querySpecAfterDelete);
    Assert.assertEquals(0, personsAfterDelete.size());
  }

  @Test
  public void checkExperimentalBulkDelete() {
    Task task1 = newTask("1");
    Task task2 = newTask("2");

    OperationsClient operationsClient = new OperationsClient(client);
    OperationsCall call = operationsClient.createCall();
    call.add(HttpMethod.POST, task1);
    call.add(HttpMethod.POST, task2);
    call.execute();

    call = operationsClient.createCall();
    call.add(HttpMethod.DELETE, task1);
    call.add(HttpMethod.DELETE, task2);
    call.execute();

    Assert.assertEquals(HttpStatus.NO_CONTENT_204, call.getResponse(0).getStatus());

    QuerySpec querySpec = new QuerySpec(Task.class);
    ResourceList<Task> tasks = bulkRepository.findAll(querySpec);
    Assert.assertEquals(0, tasks.size());
    Mockito.verify(bulkRepository, Mockito.times(1)).delete(Arrays.asList(task1.getId(), task2.getId()));
  }
}
