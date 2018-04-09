package io.crnk.test.suite;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Project.ProjectLinks;
import io.crnk.test.mock.models.Project.ProjectMeta;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.ProjectRepository.ProjectsLinksInformation;
import io.crnk.test.mock.repository.ProjectRepository.ProjectsMetaInformation;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public abstract class InformationAccessTestBase {

	protected TestContainer testContainer;

	protected ResourceRepositoryV2<Task, Long> taskRepo;

	protected ResourceRepositoryV2<Project, Long> projectRepo;

	@Before
	public void setup() {
		testContainer.start();
		taskRepo = testContainer.getRepositoryForType(Task.class);
		projectRepo = testContainer.getRepositoryForType(Project.class);

		Project project = new Project();
		project.setId(14L);
		projectRepo.create(project);
	}

	@After
	public void tearDown() {
		testContainer.stop();
	}


	@Test
	public void testMeta() {
		QuerySpec querySpec = new QuerySpec(Project.class);
		ResourceList<Project> list = projectRepo.findAll(querySpec);
		ProjectsMetaInformation metaInformation = list.getMeta(ProjectsMetaInformation.class);
		Assert.assertEquals("testMeta", metaInformation.getMetaValue());
		Project project = list.get(0);
		ProjectMeta projectMeta = project.getMeta();
		Assert.assertNotNull(projectMeta);
		Assert.assertEquals("someMetaValue", projectMeta.getValue());
	}

	@Test
	public void testLinks() {
		QuerySpec querySpec = new QuerySpec(Project.class);
		querySpec.setLimit(1L);
		ResourceList<Project> list = projectRepo.findAll(querySpec);
		ProjectsLinksInformation lnksInformation = list.getLinks(ProjectsLinksInformation.class);
		Assert.assertEquals("testLink", lnksInformation.getLinkValue());
		Project project = list.get(0);
		ProjectLinks projectLinks = project.getLinks();
		Assert.assertNotNull(projectLinks);
		Assert.assertEquals("someLinkValue", projectLinks.getValue());
	}
}