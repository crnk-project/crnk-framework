package io.crnk.test.suite;

import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.RelationIdTestResource;
import io.crnk.test.mock.models.Schedule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Checks support for @JsonApiRelationId
 */
public abstract class RelationIdAccessTestBase {

	protected TestContainer testContainer;

	protected ResourceRepositoryV2<Project, Long> projectRepo;

	protected ResourceRepositoryV2<Schedule, Long> scheduleRepo;

	protected RelationshipRepositoryV2<Schedule, Long, Project, Long> relRepo;

	private Project project;

	private Project project2;

	@Before
	public void setup() {
		testContainer.start();
		scheduleRepo = testContainer.getRepositoryForType(Schedule.class);
		projectRepo = testContainer.getRepositoryForType(Project.class);
		relRepo = testContainer.getRepositoryForType(Schedule.class, Project.class);
	}

	@After
	public void tearDown() {
		testContainer.stop();
	}

	@Test
	public void checkCrud() {
		Schedule schedule = checkPost();
		checkFindWithoutInclusion(schedule);
		checkFindWithInclusion(schedule);
		schedule = checkPatch(schedule);
		checkPatchRelationship(schedule);
	}

	@Test
	public void checkResourceIdentifierField() {
		Schedule schedule = new Schedule();
		schedule.setId(13L);
		schedule.setName("mySchedule");
		Schedule savedSchedule = scheduleRepo.create(schedule);

		RelationIdTestResource resource = new RelationIdTestResource();
		resource.setId(14L);
		resource.setName("test");
		resource.setTestResourceIdRefId(new ResourceIdentifier("13", "schedules"));

		ResourceRepositoryV2<RelationIdTestResource, Serializable> repository =
				testContainer.getRepositoryForType(RelationIdTestResource.class);
		RelationIdTestResource createdResource = repository.create(resource);
		Assert.assertEquals(resource.getTestResourceIdRefId(), createdResource.getTestResourceIdRefId());

		RelationIdTestResource serverResource = testContainer.getTestData(RelationIdTestResource.class, 14L);
		Assert.assertEquals(resource.getTestResourceIdRefId(), serverResource.getTestResourceIdRefId());

		QuerySpec querySpec = new QuerySpec(RelationIdTestResource.class);
		RelationIdTestResource getResource = repository.findOne(14L, querySpec);
		Assert.assertNull(getResource.getTestResourceIdRefId());
		Assert.assertNull(createdResource.getTestResourceIdRef());

		querySpec = new QuerySpec(RelationIdTestResource.class);
		querySpec.includeRelation(Arrays.asList("testResourceIdRef"));
		getResource = repository.findOne(14L, querySpec);
		Assert.assertEquals(resource.getTestResourceIdRefId(), getResource.getTestResourceIdRefId());
		Assert.assertNotNull(getResource.getTestResourceIdRef());
	}

	private void checkFindWithoutInclusion(Schedule schedule) {
		QuerySpec querySpec = new QuerySpec(Schedule.class);
		Schedule foundSchedule = scheduleRepo.findOne(schedule.getId(), querySpec);
		Assert.assertEquals(project.getId(), foundSchedule.getProjectId());
		Assert.assertNull(foundSchedule.getProject());

		Assert.assertEquals(1, foundSchedule.getProjectIds().size());
		Assert.assertEquals(project.getId(), foundSchedule.getProjectIds().get(0));
		Assert.assertNotNull(foundSchedule.getProjects());

		// TODO list should contain proxies in the future
		List<Project> projects = foundSchedule.getProjects();
		Assert.assertEquals(1, projects.size());
		Assert.assertNull(projects.get(0).getName()); // not initialized, id-only
	}

	private void checkFindWithInclusion(Schedule schedule) {
		QuerySpec querySpec = new QuerySpec(Schedule.class);
		querySpec.includeRelation(Arrays.asList("project"));
		querySpec.includeRelation(Arrays.asList("projects"));
		Schedule foundSchedule = scheduleRepo.findOne(schedule.getId(), querySpec);
		Assert.assertEquals(project.getId(), foundSchedule.getProjectId());
		Assert.assertNotNull(foundSchedule.getProject());
		Assert.assertEquals(project.getId(), foundSchedule.getProject().getId());


		Assert.assertEquals(1, foundSchedule.getProjectIds().size());
		Assert.assertEquals(1, foundSchedule.getProjects().size());
		Assert.assertEquals(project.getId(), foundSchedule.getProjectIds().get(0));
		Assert.assertEquals(project.getId(), foundSchedule.getProjects().get(0).getId());
	}

	private Schedule checkPost() {
		project = new Project();
		project.setName("myProject");
		project = projectRepo.create(project);

		Schedule schedule = new Schedule();
		schedule.setId(1L);
		schedule.setName("mySchedule");
		schedule.setProjectId(project.getId());
		schedule.setProjectIds(Arrays.asList(project.getId()));
		Schedule savedSchedule = scheduleRepo.create(schedule);

		Schedule serverSchedule = testContainer.getTestData(Schedule.class, 1L);
		Assert.assertEquals(project.getId(), serverSchedule.getProjectId());
		Assert.assertEquals(1, serverSchedule.getProjectIds().size());
		Assert.assertEquals(project.getId(), serverSchedule.getProjectIds().get(0));
		Assert.assertNull(serverSchedule.getProject());

		Assert.assertNotSame(schedule, savedSchedule);
		Assert.assertEquals(project.getId(), savedSchedule.getProjectId());
		Assert.assertEquals(1, savedSchedule.getProjectIds().size());
		return savedSchedule;
	}

	private Schedule checkPatch(Schedule schedule) {
		project2 = new Project();
		project2.setName("myProject2");
		project2 = projectRepo.create(project2);

		schedule.setProjectId(project2.getId());
		schedule.setProjectIds(Arrays.asList(project.getId(), project2.getId()));
		Schedule savedSchedule = scheduleRepo.save(schedule);

		Schedule serverSchedule = testContainer.getTestData(Schedule.class, 1L);
		Assert.assertEquals(project2.getId(), serverSchedule.getProjectId());
		Assert.assertEquals(2, serverSchedule.getProjectIds().size());
		Assert.assertEquals(project.getId(), serverSchedule.getProjectIds().get(0));
		Assert.assertEquals(project2.getId(), serverSchedule.getProjectIds().get(1));
		Assert.assertNull(serverSchedule.getProject());

		Assert.assertNotSame(schedule, savedSchedule);
		Assert.assertEquals(project2.getId(), savedSchedule.getProjectId());
		Assert.assertEquals(2, savedSchedule.getProjectIds().size());
		return savedSchedule;
	}

	private Schedule checkPatchRelationship(Schedule schedule) {

		RelationshipRepositoryV2<Schedule, Serializable, Project, Serializable> relRepository =
				testContainer.getRepositoryForType(Schedule.class, Project.class);

		relRepository.setRelation(schedule, project.getId(), "project");
		schedule = scheduleRepo.findOne(schedule.getId(), new QuerySpec(Schedule.class));
		Assert.assertEquals(project.getId(), schedule.getProjectId());

		relRepository.setRelation(schedule, project2.getId(), "project");
		schedule = scheduleRepo.findOne(schedule.getId(), new QuerySpec(Schedule.class));
		Assert.assertEquals(project2.getId(), schedule.getProjectId());

		relRepository.setRelations(schedule, Collections.emptyList(), "projects");
		schedule = scheduleRepo.findOne(schedule.getId(), new QuerySpec(Schedule.class));
		Assert.assertEquals(Collections.emptyList(), schedule.getProjectIds());

		relRepository.addRelations(schedule, Arrays.asList(project.getId()), "projects");
		schedule = scheduleRepo.findOne(schedule.getId(), new QuerySpec(Schedule.class));
		Assert.assertEquals(Arrays.asList(project.getId()), schedule.getProjectIds());

		relRepository.addRelations(schedule, Arrays.asList(project2.getId()), "projects");
		schedule = scheduleRepo.findOne(schedule.getId(), new QuerySpec(Schedule.class));
		Assert.assertEquals(Arrays.asList(project.getId(), project2.getId()), schedule.getProjectIds());

		relRepository.removeRelations(schedule, Arrays.asList(project.getId()), "projects");
		schedule = scheduleRepo.findOne(schedule.getId(), new QuerySpec(Schedule.class));
		Assert.assertEquals(Arrays.asList(project2.getId()), schedule.getProjectIds());

		return schedule;
	}

}
