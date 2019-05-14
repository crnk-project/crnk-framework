package io.crnk.operations.client;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.operations.model.MovieEntity;
import io.crnk.operations.model.PersonEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class OperationsCallTest extends io.crnk.operations.AbstractOperationsTest {


	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void testDefaultAtributesOmittedOnPost() {
		MovieEntity movie = newMovie("test");
		OperationsClient operationsClient = new OperationsClient(client);
		OperationsCall call = operationsClient.createCall();
		Resource resource = call.toResource(movie, HttpMethod.POST);
		Assert.assertNotNull(resource.getAttributes().get("title"));
		Assert.assertNull(resource.getAttributes().get("year"));
	}

	@Test
	public void testNullRelationshipsOmittedOnPost() {
		MovieEntity movie = newMovie("test");
		OperationsClient operationsClient = new OperationsClient(client);
		OperationsCall call = operationsClient.createCall();
		Resource resource = call.toResource(movie, HttpMethod.POST);
		Map<String, Relationship> relationships = resource.getRelationships();
		Assert.assertEquals(0, relationships.size());
	}

	@Test
	public void testEmptyRelationshipsOmittedOnPost() {
		MovieEntity movie = newMovie("test");
		movie.setDirectors(new HashSet<>());
		OperationsClient operationsClient = new OperationsClient(client);
		OperationsCall call = operationsClient.createCall();
		Resource resource = call.toResource(movie, HttpMethod.POST);
		Map<String, Relationship> relationships = resource.getRelationships();
		Assert.assertEquals(0, relationships.size());
	}

	@Test
	public void testEmptyRelationshipsNotOmittedOnPatch() {
		MovieEntity movie = newMovie("test");
		movie.setDirectors(new HashSet<>());
		OperationsClient operationsClient = new OperationsClient(client);
		OperationsCall call = operationsClient.createCall();
		Resource resource = call.toResource(movie, HttpMethod.PATCH);
		Map<String, Relationship> relationships = resource.getRelationships();
		Assert.assertEquals(1, relationships.size());
		Assert.assertNotNull(relationships.get("directors"));
	}

	@Test
	public void testNonDefaultRelationshipsNotOmittedOnPost() {
		MovieEntity movie = newMovie("test");
		PersonEntity person1 = newPerson("1");
		PersonEntity person2 = newPerson("2");
		movie.setDirectors(new HashSet<>(Arrays.asList(person1, person2)));
		OperationsClient operationsClient = new OperationsClient(client);
		OperationsCall call = operationsClient.createCall();
		Resource resource = call.toResource(movie, HttpMethod.POST);
		Map<String, Relationship> relationships = resource.getRelationships();
		Assert.assertEquals(1, relationships.size());
		Assert.assertNotNull(relationships.get("directors"));
	}
}
