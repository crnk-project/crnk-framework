package io.crnk.core.engine.internal.document.mapper.lookup.relationid;

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

public class SerializeEagerRelationIdLookupTest extends AbstractDocumentMapperTest {


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
			entity.setTestSerializeEagerId(3L);
		}
		if (setRelatedEntity) {
			entity.setTestSerializeEager(schedule);
		}

		QuerySpec querySpec = new QuerySpec(RelationIdTestResource.class);
		// since eager no inclusion necessary

		Document document = mapper.toDocument(toResponse(entity), toAdapter(querySpec), mappingConfig).get();
		Resource resource = document.getSingleData().get();
		Assert.assertEquals("2", resource.getId());
		Assert.assertEquals("relationIdTest", resource.getType());
		Assert.assertEquals("test", resource.getAttributes().get("name").asText());

		Nullable<ResourceIdentifier> data = resource.getRelationships().get("testSerializeEager").getSingleData();
		Assert.assertTrue(data.isPresent());

		if (setRelatedId) {
			Assert.assertNotNull(data.get());
			Assert.assertEquals(1, document.getIncluded().size());
			Assert.assertEquals("3", document.getIncluded().get(0).getId());
			Assert.assertEquals(setRelatedEntity ? 0 : 1, scheduleRepository.getNumFindAll());
		}
		else {
			Assert.assertNull(data.get());
			Assert.assertEquals(0, scheduleRepository.getNumFindAll());
		}
	}

}
