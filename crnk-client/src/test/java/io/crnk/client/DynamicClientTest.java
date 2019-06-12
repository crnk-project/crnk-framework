package io.crnk.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.exception.InternalServerErrorException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class DynamicClientTest extends AbstractClientTest {

	@Before
	public void setup() {
		super.setup();
	}

	@Override
	protected TestApplication configure() {
		return new TestApplication();
	}

	protected void setupFeature(CrnkTestFeature feature) {
		feature.addModule(new io.crnk.test.mock.dynamic.DynamicModule());
	}

	@Test
	public void testResource() throws IOException {
		ResourceRepository<Resource, String> repository = client.getRepositoryForPath("dynamic1");
		ObjectMapper mapper = new ObjectMapper();

		Resource resource = new Resource();
		resource.setId("john");
		resource.setType("dynamic1");
		resource.getAttributes().put("value", mapper.readTree("\"doe\""));
		repository.create(resource);

		ResourceList<Resource> list = repository.findAll(new QuerySpec("dynamic1"));
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("doe", list.get(0).getAttributes().get("value").asText());

		resource.getAttributes().put("value", mapper.readTree("\"joe\""));
		repository.save(resource);

		list = repository.findAll(new QuerySpec("dynamic1"));
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("joe", list.get(0).getAttributes().get("value").asText());

		repository.delete(resource.getId());
		list = repository.findAll(new QuerySpec("dynamic1"));
		Assert.assertEquals(0, list.size());
	}

	@Test
	public void testRelationship() throws IOException {
		ResourceRepository<Resource, String> resourceRepository = client.getRepositoryForPath("dynamic1");
		RelationshipRepository<Resource, String, Resource, String> repository = client.getRepositoryForPath("dynamic1", "tasks");
		ObjectMapper mapper = new ObjectMapper();


		Resource target = repository.findOneTarget("a", "parent", new QuerySpec("tasks"));
		Assert.assertEquals("doe", target.getAttributes().get("value").asText());

		List<Resource> targets = repository.findManyTargets("a", "children", new QuerySpec("tasks"));
		Assert.assertEquals(1, targets.size());
		Assert.assertEquals("doe", targets.get(0).getAttributes().get("value").asText());

		Resource source = new Resource();
		source.setId("john");
		source.setType("dynamic1");
		source.getAttributes().put("value", mapper.readTree("\"doe\""));
		resourceRepository.create(source);

		repository.setRelation(source, "12", "parent");
		repository.setRelations(source, Arrays.asList("12"), "children");
		repository.addRelations(source, Arrays.asList("12"), "children");
		repository.removeRelations(source, Arrays.asList("12"), "children");

		try {
			repository.setRelation(source, "13", "parent");
			Assert.fail();
		} catch (InternalServerErrorException e) {
			// ok
		}
	}

}