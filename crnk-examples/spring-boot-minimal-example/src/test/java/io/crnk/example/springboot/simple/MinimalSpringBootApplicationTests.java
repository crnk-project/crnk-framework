package io.crnk.example.springboot.simple;

import java.io.Serializable;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.example.springboot.minimal.Project;
import org.junit.Assert;
import org.junit.Test;

/**
 * Shows two kinds of test cases: RestAssured and CrnkClient.
 */
public class MinimalSpringBootApplicationTests extends BaseTest {

	@Test
	public void testClient() {
		ResourceRepositoryV2<Project, Serializable> projectRepo =
				client.getRepositoryForType(Project.class);
		QuerySpec querySpec = new QuerySpec(Project.class);
		querySpec.setLimit(10L);
		ResourceList<Project> list = projectRepo.findAll(querySpec);
		Assert.assertNotEquals(0, list.size());
	}

	@Test
	public void testFindOne() {
		testFindOne("/projects/123");
	}

	@Test
	public void testFindOne_NotFound() {
		testFindOne_NotFound("/projects/0");
	}

	@Test
	public void testFindMany() {
		testFindMany("/projects");
	}

	@Test
	public void testDelete() {
		testDelete("/projects/123");
	}

	@Test
	public void testAccessHome() {
		Response response = RestAssured.given().when().get("/");
		response.then().assertThat().statusCode(200);
		String body = response.getBody().print();
		Assert.assertTrue(body, body.contains("/projects"));
	}
}
