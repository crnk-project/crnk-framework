package io.crnk.operations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.operations.client.OperationsCall;
import io.crnk.operations.client.OperationsClient;
import io.crnk.operations.model.MovieEntity;
import io.crnk.operations.model.PersonEntity;
import io.crnk.spring.jpa.SpringTransactionRunner;

public class OperationsGetTest extends AbstractOperationsTest {

  protected ResourceRepository<MovieEntity, UUID> movieRepo;

  PersonEntity person1;
  PersonEntity person2;

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    movieRepo = client.getRepositoryForType(MovieEntity.class);

    SpringTransactionRunner transactionRunner = context.getBean(SpringTransactionRunner.class);
    transactionRunner.doInTransaction(() -> {
      EntityManager em = context.getBean(EntityManagerProducer.class).getEntityManager();

      person1 = newPerson("P1");
      em.persist(person1);

      person2 = newPerson("P2");
      em.persist(person2);

      MovieEntity movie = newMovie("test");
      em.persist(movie);

      movie.setDirectors(new HashSet<>(Arrays.asList(person1, person2)));

      return null;
    });
  }

  @Test
  public void checkGet() {
    OperationsClient operationsClient = new OperationsClient(client);
    OperationsCall call = operationsClient.createCall();
    call.add(HttpMethod.GET, "person/" + person1.getId());
    call.add(HttpMethod.GET, "person/" + person2.getId());
    call.execute();

    Assert.assertEquals("P1", call.getResponse(0).getSingleData().get().getAttributes().get("name").asText());
    Assert.assertEquals("P2", call.getResponse(1).getSingleData().get().getAttributes().get("name").asText());
  }

  @Test
  public void checkGetWithIncludeParam() {
    OperationsClient operationsClient = new OperationsClient(client);
    OperationsCall call = operationsClient.createCall();
    call.add(HttpMethod.GET, "person/" + person1.getId() + "?include=directedMovies");
    call.add(HttpMethod.GET, "person/" + person2.getId() + "?include=directedMovies");
    call.execute();

    // directedMovies should have been included:
    Assert.assertEquals(1, call.getResponse(0).getSingleData().get().getRelationships().get("directedMovies")
        .getCollectionData().get().size());
    Assert.assertEquals(1, call.getResponse(1).getSingleData().get().getRelationships().get("directedMovies")
        .getCollectionData().get().size());
  }

}
