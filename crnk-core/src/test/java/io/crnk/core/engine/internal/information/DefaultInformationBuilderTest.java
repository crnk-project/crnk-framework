package io.crnk.core.engine.internal.information;

import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.resource.*;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
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
		idField.lazy(false);
		idField.setAccess(new ResourceFieldAccess(true, true, false, false));

		ResourceFieldAccessor accessor = Mockito.mock(ResourceFieldAccessor.class);
		InformationBuilder.Field projectField = resource.addField("project", ResourceFieldType.RELATIONSHIP, Project.class);
		projectField.lazy(true);
		projectField.setAccess(new ResourceFieldAccess(false, true, false, false));
		projectField.setOppositeName("tasks");
		projectField.lookupIncludeBehavior(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS);
		projectField.includeByDefault(true);
		projectField.setAccessor(accessor);

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
		Assert.assertFalse(idInfo.isLazy());
		Assert.assertFalse(idInfo.isCollection());

		ResourceField projectInfo = info.findFieldByName("project");
		Assert.assertEquals("project", projectInfo.getUnderlyingName());
		Assert.assertEquals("tasks", projectInfo.getOppositeName());
		Assert.assertEquals(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS, projectInfo.getLookupIncludeAutomatically());
		Assert.assertTrue(projectInfo.getIncludeByDefault());
		Assert.assertEquals(Project.class, projectInfo.getType());
		Assert.assertSame(accessor, projectInfo.getAccessor());
		Assert.assertFalse(projectInfo.getAccess().isFilterable());
		Assert.assertFalse(projectInfo.getAccess().isSortable());
		Assert.assertFalse(projectInfo.getAccess().isPostable());
		Assert.assertTrue(projectInfo.getAccess().isPatchable());
		Assert.assertTrue(projectInfo.isLazy());
		Assert.assertFalse(projectInfo.isCollection());
	}
}
