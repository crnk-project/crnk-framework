package io.crnk.core.engine.internal.document.mapper.lookup.relationid;

import java.util.Arrays;

import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.mock.models.RelationIdTestResource;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.repository.ScheduleRepositoryImpl;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.engine.internal.document.mapper.AbstractDocumentMapperTest;
import io.crnk.core.utils.Nullable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LookupNoneRelationIdLookupTest extends AbstractDocumentMapperTest {


	private ScheduleRepositoryImpl scheduleRepository;

	private Schedule schedule;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void setup() {
		super.setup();

		scheduleRepository = new ScheduleRepositoryImpl();
		schedule = new Schedule();
		schedule.setId(3L);
		schedule.setName("test");
		scheduleRepository.save(schedule);
	}


	@Test
	public void checkOnlyIdSet() {
		try {
			check(false, true);
			Assert.fail();
		}
		catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("inconsistent relationship"));
		}
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
			entity.setTestLookupNoneId(3L);
		}
		if (setRelatedEntity) {
			entity.setTestLookupNone(schedule);
		}

		QuerySpec querySpec = new QuerySpec(RelationIdTestResource.class);
		querySpec.includeRelation(Arrays.asList("testLookupNone"));

		Document document = mapper.toDocument(toResponse(entity), toAdapter(querySpec));
		Resource resource = document.getSingleData().get();
		Assert.assertEquals("2", resource.getId());
		Assert.assertEquals("relationIdTest", resource.getType());
		Assert.assertEquals("test", resource.getAttributes().get("name").asText());

		Nullable<ResourceIdentifier> data = resource.getRelationships().get("testLookupNone").getSingleData();
		Assert.assertTrue(data.isPresent());
		Assert.assertEquals(0, scheduleRepository.getNumFindAll());
		if (setRelatedEntity) {
			Assert.assertNotNull(data.get());
			Assert.assertEquals(1, document.getIncluded().size());
			Assert.assertEquals("3", document.getIncluded().get(0).getId());
		}
		else if (setRelatedId) {
			Assert.fail("without lookup related entity should be set if related id is set");
		}
		else {
			Assert.assertNull(data.get());
		}
	}

}
