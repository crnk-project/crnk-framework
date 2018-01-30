package io.crnk.core.resource.internal;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.mock.models.LazyTask;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.core.utils.Nullable;
import org.junit.Assert;
import org.junit.Test;

public class JsonApiRelationIdDocumentMapperTest extends AbstractDocumentMapperTest {

	@Test
	public void dataNotWrittenIfNotIncluded() {
		Schedule schedule = new Schedule();
		schedule.setId(2L);
		schedule.setName("sample schedule");

		Document document = mapper.toDocument(toResponse(schedule), createAdapter(Schedule.class));
		Resource resource = document.getSingleData().get();
		Assert.assertEquals("2", resource.getId());
		Assert.assertEquals("schedules", resource.getType());
		Assert.assertEquals("sample schedule", resource.getAttributes().get("name").asText());
		Assert.assertFalse(resource.getRelationships().get("project").getData().isPresent());
	}

	@Test
	public void dataNotWrittenIfNotIncludedButSet() {
		Schedule schedule = new Schedule();
		schedule.setId(2L);
		schedule.setName("sample schedule");
		schedule.setProjectId(13L);

		Document document = mapper.toDocument(toResponse(schedule), createAdapter(Schedule.class));
		Resource resource = document.getSingleData().get();
		Assert.assertEquals("2", resource.getId());
		Assert.assertEquals("schedules", resource.getType());
		Assert.assertEquals("sample schedule", resource.getAttributes().get("name").asText());
		Assert.assertFalse(resource.getRelationships().get("project").getData().isPresent());
	}

	@Test
	public void dataWrittenIfIncluded() {
		Schedule schedule = new Schedule();
		schedule.setId(2L);
		schedule.setName("sample schedule");
		schedule.setProjectId(3L);

		QuerySpec querySpec = new QuerySpec(Schedule.class);
		querySpec.includeRelation(Arrays.asList("project"));

		Document document = mapper.toDocument(toResponse(schedule), toAdapter(querySpec));
		Resource resource = document.getSingleData().get();
		Assert.assertEquals("2", resource.getId());

		Relationship relationship = resource.getRelationships().get("project");
		Assert.assertNotNull(relationship);
		ResourceIdentifier relationshipData = relationship.getSingleData().get();
		Assert.assertNotNull(relationshipData);
		Assert.assertEquals("3", relationshipData.getId());
		Assert.assertEquals("projects", relationshipData.getType());

		List<Resource> included = document.getIncluded();
		Assert.assertEquals(1, included.size());
		Assert.assertEquals("3", included.get(0).getId());
		Assert.assertEquals("projects", included.get(0).getType());
		Assert.assertEquals("sample project", included.get(0).getAttributes().get("name").asText());
	}


	@Test
	public void testRelationshipLazyMultiValued() {
		LazyTask task = createLazyTask(2);
		Project project1 = createProject(3, "sample project");
		Project project2 = createProject(4, "sample project");
		task.setProjects(Arrays.asList(project1, project2));

		Document document = mapper.toDocument(toResponse(task), createAdapter(Task.class));
		Resource resource = document.getSingleData().get();
		Assert.assertEquals("2", resource.getId());

		Relationship relationship = resource.getRelationships().get("projects");
		Assert.assertNotNull(relationship);
		Nullable<List<ResourceIdentifier>> relationshipData = relationship.getCollectionData();
		Assert.assertFalse(relationshipData.isPresent());
		Assert.assertTrue(document.getIncluded().isEmpty());
	}

	@Test
	public void testRelationshipIncludeMultiValued() {
		LazyTask task = createLazyTask(2);
		Project project1 = createProject(3, "sample project3");
		Project project2 = createProject(4, "sample project4");
		task.setProjects(Arrays.asList(project1, project2));

		QuerySpec querySpec = new QuerySpec(LazyTask.class);
		querySpec.includeRelation(Arrays.asList("projects"));

		Document document = mapper.toDocument(toResponse(task), toAdapter(querySpec));
		Resource resource = document.getSingleData().get();
		Assert.assertEquals("2", resource.getId());

		Relationship relationship = resource.getRelationships().get("projects");
		Assert.assertNotNull(relationship);
		List<ResourceIdentifier> relationshipData = relationship.getCollectionData().get();
		Assert.assertNotNull(relationshipData);
		Assert.assertEquals(2, relationshipData.size());
		Assert.assertEquals("3", relationshipData.get(0).getId());
		Assert.assertEquals("projects", relationshipData.get(0).getType());
		Assert.assertEquals("4", relationshipData.get(1).getId());
		Assert.assertEquals("projects", relationshipData.get(1).getType());

		Assert.assertFalse(document.getIncluded().isEmpty());

		List<Resource> included = document.getIncluded();
		Assert.assertEquals(2, included.size());
		Assert.assertEquals("3", included.get(0).getId());
		Assert.assertEquals("projects", included.get(0).getType());
		Assert.assertEquals("sample project3", included.get(0).getAttributes().get("name").asText());
		Assert.assertEquals("4", included.get(1).getId());
		Assert.assertEquals("projects", included.get(1).getType());
		Assert.assertEquals("sample project4", included.get(1).getAttributes().get("name").asText());
	}


	@Test
	public void testRelationshipCyclicInclusion() {
		Task task = createTask(2, "sample task");
		Project project = createProject(3, "sample project");
		task.setProject(project);
		project.setTask(task);

		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeRelation(Arrays.asList("project"));
		querySpec.getOrCreateQuerySpec(Project.class).includeRelation(Arrays.asList("task"));

		Document document = mapper.toDocument(toResponse(task), toAdapter(querySpec));
		Resource resource = document.getSingleData().get();
		Assert.assertEquals("2", resource.getId());

		Relationship relationship = resource.getRelationships().get("project");
		Assert.assertNotNull(relationship);
		ResourceIdentifier relationshipData = relationship.getSingleData().get();
		Assert.assertNotNull(relationshipData);
		Assert.assertEquals("3", relationshipData.getId());
		Assert.assertEquals("projects", relationshipData.getType());

		List<Resource> included = document.getIncluded();
		Assert.assertEquals(1, included.size());
		Assert.assertEquals("3", included.get(0).getId());
		Assert.assertEquals("projects", included.get(0).getType());
		Assert.assertEquals("sample project", included.get(0).getAttributes().get("name").asText());
		Assert.assertEquals("2", included.get(0).getRelationships().get("task").getSingleData().get().getId());
	}

	@Test
	public void testRelationshipSingleValuedIncludeByDefault() {
		Task task = createTask(2, "sample task");
		Project project = createProject(3, "sample project");
		task.setProject(project);

		Document document = mapper.toDocument(toResponse(task), createAdapter(Task.class));
		Resource resource = document.getSingleData().get();
		Assert.assertEquals("2", resource.getId());
		Assert.assertEquals("tasks", resource.getType());
		Assert.assertEquals("sample task", resource.getAttributes().get("name").asText());

		Relationship relationship = resource.getRelationships().get("project");
		Assert.assertNotNull(relationship);
		Assert.assertEquals("https://service.local/tasks/2/relationships/project",
				getLinkText(relationship.getLinks().get("self")));
		Assert.assertEquals("https://service.local/tasks/2/project", getLinkText(relationship.getLinks().get("related")));
		ResourceIdentifier relationshipData = relationship.getSingleData().get();
		Assert.assertNotNull(relationshipData);
		Assert.assertEquals("3", relationshipData.getId());
		Assert.assertEquals("projects", relationshipData.getType());

		List<Resource> included = document.getIncluded();
		Assert.assertEquals(1, included.size());
		Assert.assertEquals("3", included.get(0).getId());
		Assert.assertEquals("projects", included.get(0).getType());
		Assert.assertEquals("sample project", included.get(0).getAttributes().get("name").asText());
	}

	@Test
	public void testRelationshipSingleValuedLazy() {
		LazyTask task = createLazyTask(2);
		Project project = createProject(3, "sample project");
		task.setLazyProject(project);

		Document document = mapper.toDocument(toResponse(task), createAdapter(Task.class));
		Resource resource = document.getSingleData().get();
		Assert.assertEquals("2", resource.getId());
		Assert.assertEquals("lazy_tasks", resource.getType());

		Relationship relationship = resource.getRelationships().get("lazyProject");
		Assert.assertNotNull(relationship);
		Assert.assertEquals("https://service.local/lazy_tasks/2/relationships/lazyProject",
				getLinkText(relationship.getLinks().get("self")));
		Assert.assertEquals("https://service.local/lazy_tasks/2/lazyProject",
				getLinkText(relationship.getLinks().get("related")));
		Nullable<ResourceIdentifier> relationshipData = relationship.getSingleData();
		Assert.assertFalse(relationshipData.isPresent());
		Assert.assertTrue(document.getIncluded().isEmpty());
	}

	@Test
	public void testAttributesSelection() {
		Task task = createTask(2, "sample task");
		task.setCategory("sample category");
		task.setProject(new Project());
		JsonApiResponse response = new JsonApiResponse();
		response.setEntity(task);

		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeField(Arrays.asList("category"));

		Document document = mapper.toDocument(response, toAdapter(querySpec));
		Resource resource = document.getSingleData().get();
		Assert.assertEquals("2", resource.getId());
		Assert.assertEquals("tasks", resource.getType());
		Assert.assertNull(resource.getAttributes().get("name"));
		Assert.assertNull(resource.getRelationships().get("project"));
		Assert.assertEquals("sample category", resource.getAttributes().get("category").asText());
	}

	private Project createProject(long id, String name) {
		Project project = new Project();
		project.setId(id);
		project.setName(name);
		return project;
	}

	private Task createTask(long id, String name) {
		Task task = new Task();
		task.setId(id);
		task.setName(name);
		return task;
	}

	private LazyTask createLazyTask(long id) {
		LazyTask task = new LazyTask();
		task.setId(id);
		return task;
	}

	public static class TestLinksInformation implements LinksInformation {

		public String value;

		public String getValue() {
			return value;
		}

		// used to test the LinksInformationSerializer -> should not be serialized
		@JsonIgnore
		public String getOtherValue() {
			return null;
		}
	}

	public static class TestMetaInformation implements MetaInformation {

		public String value;

		public String getValue() {
			return value;
		}
	}

}
