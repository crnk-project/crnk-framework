package io.crnk.core.engine.internal.document.mapper.lookup.relationid;

import java.util.Arrays;

import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.mock.models.RelationIdTestResource;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.repository.RelationIdTestRepository;
import io.crnk.core.mock.repository.ScheduleRepositoryImpl;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.engine.internal.document.mapper.AbstractDocumentMapperTest;
import io.crnk.core.utils.Nullable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NestedRelationIdLookupTest extends AbstractDocumentMapperTest {


	private ScheduleRepositoryImpl scheduleRepository;

	private Schedule schedule;

	private RelationIdTestRepository testRepository;

	private RelationIdTestResource child;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void setup() {
		super.setup();

		scheduleRepository = new ScheduleRepositoryImpl();
		schedule = new Schedule();
		schedule.setId(3L);
		schedule.setName("test");
		scheduleRepository.save(schedule);

		testRepository = new RelationIdTestRepository();
		child = new RelationIdTestResource();
		child.setId(1L);
		child.setName("child");
		testRepository.save(child);
		child.setTestSerializeEager(schedule);
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
			entity.setTestNestedId(child.getId());
		}
		if (setRelatedEntity) {
			entity.setTestNested(child);
		}

		QuerySpec querySpec = new QuerySpec(RelationIdTestResource.class);
		querySpec.includeRelation(Arrays.asList("testNested", "schedule"));

		Document document = mapper.toDocument(toResponse(entity), toAdapter(querySpec));
		Resource resource = document.getSingleData().get();
		Assert.assertEquals("2", resource.getId());
		Assert.assertEquals("relationIdTest", resource.getType());
		Assert.assertEquals("test", resource.getAttributes().get("name").asText());

		Nullable<ResourceIdentifier> data = resource.getRelationships().get("testNested").getSingleData();
		Assert.assertTrue(data.isPresent());

		if (setRelatedId) {
			Assert.assertNotNull(data.get());
			Assert.assertEquals(2, document.getIncluded().size());
		}
		else {
			Assert.assertNull(data.get());
		}
	}

}
