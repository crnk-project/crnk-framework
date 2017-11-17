package io.crnk.core.engine.internal.information;

import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.repository.RelationshipRepositoryInformation;
import io.crnk.core.engine.information.repository.RepositoryMethodAccess;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.*;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.SerializeType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DefaultInformationBuilderTest {

	private DefaultInformationBuilder builder;

	@Before
	public void setup() {
		TypeParser parser = new TypeParser();
		builder = new DefaultInformationBuilder(parser);

	}

	@Test
	public void resource() {
		InformationBuilder.Resource resource = builder.createResource(Task.class, "tasks");
		resource.superResourceType("superTask");
		resource.resourceType("changedTasks");
		resource.resourceClass(Project.class);

		InformationBuilder.Field idField = resource.addField("id", ResourceFieldType.ID, String.class);
		idField.serializeType(SerializeType.EAGER);
		idField.access(new ResourceFieldAccess(true, true, true, false, false));

		ResourceFieldAccessor accessor = Mockito.mock(ResourceFieldAccessor.class);
		InformationBuilder.Field projectField = resource.addField("project", ResourceFieldType.RELATIONSHIP, Project.class);
		projectField.serializeType(SerializeType.EAGER);
		projectField.access(new ResourceFieldAccess(true, false, true, false, false));
		projectField.oppositeName("tasks");
		projectField.lookupIncludeBehavior(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS);
		projectField.accessor(accessor);

		ResourceInformation info = resource.build();
		Assert.assertEquals("changedTasks", info.getResourceType());
		Assert.assertEquals(Project.class, info.getResourceClass());
		Assert.assertEquals("superTask", info.getSuperResourceType());

		ResourceField idInfo = info.findFieldByName("id");
		Assert.assertEquals("id", idInfo.getUnderlyingName());
		Assert.assertEquals(String.class, idInfo.getType());
		Assert.assertFalse(idInfo.getAccess().isFilterable());
		Assert.assertFalse(idInfo.getAccess().isSortable());
		Assert.assertTrue(idInfo.getAccess().isPostable());
		Assert.assertTrue(idInfo.getAccess().isPatchable());
		Assert.assertEquals(SerializeType.EAGER, idInfo.getSerializeType());
		Assert.assertFalse(idInfo.isCollection());

		ResourceField projectInfo = info.findFieldByName("project");
		Assert.assertEquals("project", projectInfo.getUnderlyingName());
		Assert.assertEquals("tasks", projectInfo.getOppositeName());
		Assert.assertEquals(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS, projectInfo.getLookupIncludeAutomatically());
		Assert.assertEquals(Project.class, projectInfo.getType());
		Assert.assertSame(accessor, projectInfo.getAccessor());
		Assert.assertFalse(projectInfo.getAccess().isFilterable());
		Assert.assertFalse(projectInfo.getAccess().isSortable());
		Assert.assertFalse(projectInfo.getAccess().isPostable());
		Assert.assertTrue(projectInfo.getAccess().isPatchable());
		Assert.assertEquals(SerializeType.EAGER, projectInfo.getSerializeType());
		Assert.assertFalse(projectInfo.isCollection());
	}

	@Test
	public void checkResourceRepository() {
		ResourceInformation resourceInformation = builder.createResource(Task.class, "tasks").build();

		InformationBuilder.ResourceRepository repositoryBuilder = builder.createResourceRepository();
		repositoryBuilder.setResourceInformation(resourceInformation);
		RepositoryMethodAccess expectedAccess = new RepositoryMethodAccess(true, false, true, false);
		repositoryBuilder.setAccess(expectedAccess);
		ResourceRepositoryInformation repositoryInformation = repositoryBuilder.build();
		RepositoryMethodAccess actualAccess = repositoryInformation.getAccess();
		Assert.assertEquals(expectedAccess, actualAccess);
		Assert.assertSame(resourceInformation, repositoryInformation.getResourceInformation().get());
	}

	@Test
	public void checkRelationshipRepository() {
		InformationBuilder.RelationshipRepository repositoryBuilder = builder.createRelationshipRepository("projects", "tasks");
		RepositoryMethodAccess expectedAccess = new RepositoryMethodAccess(true, false, true, false);
		repositoryBuilder.setAccess(expectedAccess);
		RelationshipRepositoryInformation repositoryInformation = repositoryBuilder.build();
		RepositoryMethodAccess actualAccess = repositoryInformation.getAccess();
		Assert.assertEquals(expectedAccess, actualAccess);
	}
}
