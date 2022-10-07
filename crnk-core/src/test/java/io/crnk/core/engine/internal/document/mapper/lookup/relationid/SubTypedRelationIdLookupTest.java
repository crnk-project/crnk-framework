package io.crnk.core.engine.internal.document.mapper.lookup.relationid;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
import io.crnk.core.mock.models.TopTaskWrapper;
import io.crnk.core.mock.repository.TopTaskRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.utils.Nullable;

public class SubTypedRelationIdLookupTest extends AbstractDocumentMapperTest {

	public static final Long TEST_RESOURCE_ID  = 1L;
	public static final Long TASK_1_ID         = 2L;
	public static final Long TASK_2_ID         = 3L;
	public static final Long TASK_WRAPPER_1_ID = 4L;
	public static final Long TASK_WRAPPER_2_ID = 5L;

	private TopTaskRepository topTaskRepository;

	private BottomTask bottomTask1;

	private BottomTask bottomTask2;

	private TopTaskWrapper topTaskWrapper1;

	private TopTaskWrapper topTaskWrapper2;

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Before
	public void setup() {
		super.setup();

		topTaskRepository = (TopTaskRepository) (ResourceRepository) container.getRepository(TopTask.class);

		bottomTask1 = createTask(TASK_1_ID);
		topTaskRepository.save(bottomTask1);

		bottomTask2 = createTask(TASK_2_ID);
		topTaskRepository.save(bottomTask2);

		topTaskWrapper1 = createTopTaskWrapper(TASK_WRAPPER_1_ID);
		topTaskWrapper2 = createTopTaskWrapper(TASK_WRAPPER_2_ID);
	}

	private BottomTask createTask(final Long id) {
		BottomTask task = new BottomTask();
		task.setId(id);
		task.setName("test" + id);
		return task;
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
		entity.setId(TEST_RESOURCE_ID);
		entity.setName("test");
		entity.setTopTaskWrappers(Arrays.asList(topTaskWrapper1, topTaskWrapper2));
		if (setRelatedId) {
			entity.setTestSubTypedResourceId(TASK_1_ID);
			topTaskWrapper1.setTaskId(bottomTask2.getId());
			topTaskWrapper2.setTaskId(bottomTask2.getId());
		}
		if (setRelatedEntity) {
			entity.setTestSubTypedResource(bottomTask1);
			topTaskWrapper1.setTask(bottomTask2);
			topTaskWrapper2.setTask(bottomTask2);
		}

		QuerySpec querySpec = new QuerySpec(RelationIdTestResource.class);
		querySpec.includeRelation(Collections.singletonList("testSubTypedResource"));
		querySpec.includeRelation(Arrays.asList("topTaskWrappers", "task"));

		Document document = mapper.toDocument(toResponse(entity), toAdapter(querySpec), mappingConfig).get();
		Resource resource = document.getSingleData().get();
		Assert.assertEquals(TEST_RESOURCE_ID.toString(), resource.getId());
		Assert.assertEquals("relationIdTest", resource.getType());
		Assert.assertEquals("test", resource.getAttributes().get("name").asText());

		Nullable<ResourceIdentifier> testSubTypedResourceId = resource.getRelationships().get("testSubTypedResource").getSingleData();
		Assert.assertTrue(testSubTypedResourceId.isPresent());
		Nullable<List<ResourceIdentifier>> topTaskWrappersIds = resource.getRelationships().get("topTaskWrappers").getCollectionData();
		Assert.assertTrue(topTaskWrappersIds.isPresent());
		Assert.assertNotNull(topTaskWrappersIds.get());
		List<Resource> topTaskWrappers = document.getIncluded().stream().filter(it -> it.getType().equals("topTaskWrapper")).collect(Collectors.toList());
		Assert.assertEquals(2, topTaskWrappers.size());

		if (setRelatedId) {
			Assert.assertNotNull(testSubTypedResourceId.get());
			Assert.assertEquals("bottomTask", testSubTypedResourceId.get().getType());
			Assert.assertEquals(TASK_1_ID.toString(), testSubTypedResourceId.get().getId());

			List<ResourceIdentifier> taskIds = topTaskWrappers.stream().map(it -> it.getRelationships().get("task").getSingleData().get()).collect(Collectors.toList());
			Assert.assertEquals(2, findResourceIdentifierByTypeAndId(taskIds, "bottomTask", TASK_2_ID).size());
			Assert.assertEquals(0, findResourceIdentifierByTypeAndId(taskIds, "topTask", TASK_2_ID).size());

			Assert.assertEquals(4, document.getIncluded().size());
			Assert.assertEquals(1, findIncludedByTypeAndId(document.getIncluded(), "bottomTask", TASK_1_ID).size());
			Assert.assertEquals(0, findIncludedByTypeAndId(document.getIncluded(), "topTask", TASK_1_ID).size());
			Assert.assertEquals(1, findIncludedByTypeAndId(document.getIncluded(), "bottomTask", TASK_2_ID).size());
			Assert.assertEquals(0, findIncludedByTypeAndId(document.getIncluded(), "topTask", TASK_2_ID).size());
		} else {
			Assert.assertNull(testSubTypedResourceId.get());
		}
	}

	private Collection<Object> findIncludedByTypeAndId(final List<Resource> included, final String type, final Long id) {
		return included.stream().filter(i -> i.getId().equals(id.toString()) && i.getType().equals(type)).collect(Collectors.toList());
	}

	private Collection<Object> findResourceIdentifierByTypeAndId(final List<ResourceIdentifier> ids, final String type, final Long id) {
		return ids.stream().filter(i -> i.getId().equals(id.toString()) && i.getType().equals(type)).collect(Collectors.toList());
	}

	private TopTaskWrapper createTopTaskWrapper(final long id) {
		final TopTaskWrapper topTaskWrapper = new TopTaskWrapper();
		topTaskWrapper.setId(id);
		return topTaskWrapper;
	}
}
