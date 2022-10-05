package io.crnk.core.engine.internal.document.mapper.lookup.relationid;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.internal.document.mapper.AbstractDocumentMapperTest;
import io.crnk.core.mock.models.BottomTask;
import io.crnk.core.mock.models.RelationIdTestResource;
import io.crnk.core.mock.models.TopTask;
import io.crnk.core.mock.repository.TopTaskRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.utils.Nullable;

public class SubTypedRelationIdLookupTest extends AbstractDocumentMapperTest {


	private TopTaskRepository topTaskRepository;

	private BottomTask bottomTask;

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Before
	public void setup() {
		super.setup();

		topTaskRepository = (TopTaskRepository) (ResourceRepository) container.getRepository(TopTask.class);
		bottomTask = new BottomTask();
		bottomTask.setId(3L);
		bottomTask.setName("test");
		topTaskRepository.save(bottomTask);

	}

	@Test
	public void checkOnlyIdSet() {
		check(false, true);
	}

	@Test
	public void checkNull() {
		check(false, false);
	}

	@Test
	public void checkEntitySet() {
		check(true, true);
	}

	private void check(boolean setRelatedEntity, boolean setRelatedId) {
		RelationIdTestResource entity = new RelationIdTestResource();
		entity.setId(2L);
		entity.setName("test");
		if (setRelatedId) {
			entity.setTestSubTypedResourceId(3L);
		}
		if (setRelatedEntity) {
			entity.setTestSubTypedResource(bottomTask);
		}

		QuerySpec querySpec = new QuerySpec(RelationIdTestResource.class);
		querySpec.includeRelation(Arrays.asList("testSubTypedResource"));

		Document document = mapper.toDocument(toResponse(entity), toAdapter(querySpec), mappingConfig).get();
		Resource resource = document.getSingleData().get();
		Assert.assertEquals("2", resource.getId());
		Assert.assertEquals("relationIdTest", resource.getType());
		Assert.assertEquals("test", resource.getAttributes().get("name").asText());

		Nullable<ResourceIdentifier> data = resource.getRelationships().get("testSubTypedResource").getSingleData();
		Assert.assertTrue(data.isPresent());

		if (setRelatedId) {
			Assert.assertNotNull(data.get());
			Assert.assertEquals(1, document.getIncluded().size());
			Assert.assertEquals("3", document.getIncluded().get(0).getId());
			Assert.assertEquals("bottomTask", document.getIncluded().get(0).getType());
			Assert.assertEquals("bottomTask", data.get().getType());
		} else {
			Assert.assertNull(data.get());
		}
	}
}
