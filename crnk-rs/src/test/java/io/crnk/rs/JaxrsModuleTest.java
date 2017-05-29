package io.crnk.rs;

import java.util.Map;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.SecurityContext;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.RepositoryAction.RepositoryActionType;
import io.crnk.core.engine.information.repository.RepositoryInformationBuilderContext;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceFieldNameTransformer;
import io.crnk.core.engine.information.resource.ResourceInformationBuilder;
import io.crnk.core.engine.internal.information.resource.AnnotationResourceInformationBuilder;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.legacy.registry.DefaultResourceInformationBuilderContext;
import io.crnk.rs.internal.JaxrsModule;
import io.crnk.rs.internal.JaxrsModule.JaxrsResourceRepositoryInformationBuilder;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class JaxrsModuleTest {

	private JaxrsResourceRepositoryInformationBuilder builder;

	private RepositoryInformationBuilderContext context;

	@Before
	public void setup() {
		final ModuleRegistry moduleRegistry = new ModuleRegistry();
		builder = new JaxrsResourceRepositoryInformationBuilder();
		final ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(
				new ResourceFieldNameTransformer());
		resourceInformationBuilder
				.init(new DefaultResourceInformationBuilderContext(resourceInformationBuilder, moduleRegistry.getTypeParser()));
		context = new RepositoryInformationBuilderContext() {

			@Override
			public ResourceInformationBuilder getResourceInformationBuilder() {
				return resourceInformationBuilder;
			}

			@Override
			public TypeParser getTypeParser() {
				return moduleRegistry.getTypeParser();
			}
		};
	}

	@Test
	public void testGetter() {
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		JaxrsModule module = new JaxrsModule(securityContext);
		Assert.assertEquals("jaxrs", module.getModuleName());
	}

	@Test
	public void checkSecurityProviderRegistered() {
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		JaxrsModule module = new JaxrsModule(securityContext);

		CrnkBoot boot = new CrnkBoot();
		boot.addModule(module);
		boot.boot();

		SecurityProvider securityProvider = boot.getModuleRegistry().getSecurityProvider();
		Assert.assertNotNull(securityProvider);

		Mockito.when(securityContext.isUserInRole("admin")).thenReturn(true);

		Assert.assertTrue(securityProvider.isUserInRole("admin"));
		Assert.assertFalse(securityProvider.isUserInRole("other"));
	}

	@Test
	public void testActionDetection() {
		ResourceRepositoryInformation information = (ResourceRepositoryInformation) builder.build(TaskRepository.class,
				context);
		Map<String, RepositoryAction> actions = information.getActions();
		Assert.assertEquals(5, actions.size());
		RepositoryAction action = actions.get("repositoryAction");
		Assert.assertNotNull(actions.get("repositoryPostAction"));
		Assert.assertNotNull(actions.get("repositoryDeleteAction"));
		Assert.assertNotNull(actions.get("repositoryPutAction"));
		Assert.assertNull(actions.get("notAnAction"));
		Assert.assertNotNull(action);
		Assert.assertEquals("repositoryAction", action.getName());
		Assert.assertEquals(RepositoryActionType.REPOSITORY, action.getActionType());
		Assert.assertEquals(RepositoryActionType.RESOURCE, actions.get("resourceAction").getActionType());
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidRootPathRepository() {
		builder.build(InvalidRootPathRepository.class, context);
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidIdPathRepository1() {
		builder.build(InvalidIdPathRepository1.class, context);
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidIdPathRepository2() {
		builder.build(InvalidIdPathRepository2.class, context);
	}

	@Test(expected = IllegalStateException.class)
	public void testPathToLongRepository() {
		builder.build(PathToLongRepository.class, context);
	}

	@Test(expected = IllegalStateException.class)
	public void testMissingPathRepository1() {
		builder.build(MissingPathRepository1.class, context);
	}

	@Test(expected = IllegalStateException.class)
	public void testMissingPathRepository2() {
		builder.build(MissingPathRepository2.class, context);
	}

	@Path("schedules")
	public interface TaskRepository extends ResourceRepositoryV2<Task, Long> {

		@GET
		@Path("repositoryAction")
		String repositoryAction(@QueryParam(value = "msg") String msg);

		@POST
		@Path("repositoryPostAction")
		String repositoryPostAction();

		@DELETE
		@Path("repositoryDeleteAction")
		String repositoryDeleteAction();

		@PUT
		@Path("/repositoryPutAction/")
		String repositoryPutAction();

		@GET
		@Path("{id}/resourceAction")
		String resourceAction(@PathParam("id") long id, @QueryParam(value = "msg") String msg);

	}

	@Path("schedules")
	public interface InvalidRootPathRepository extends ResourceRepositoryV2<Task, Long> {

		@GET
		@Path("")
		String resourceAction();

	}

	@Path("schedules")
	public interface MissingPathRepository1 extends ResourceRepositoryV2<Task, Long> {

		@GET
		String resourceAction();

	}

	@Path("schedules")
	public interface MissingPathRepository2 extends ResourceRepositoryV2<Task, Long> {

		String resourceAction(@PathParam("id") long id);

	}

	@Path("schedules")
	public interface PathToLongRepository extends ResourceRepositoryV2<Task, Long> {

		@GET
		@Path("a/b/c")
		String resourceAction();

	}

	@Path("schedules")
	public interface InvalidIdPathRepository1 extends ResourceRepositoryV2<Task, Long> {

		@GET
		@Path("{something}/test")
		String resourceAction();

	}

	@Path("schedules")
	public interface InvalidIdPathRepository2 extends ResourceRepositoryV2<Task, Long> {

		@GET
		@Path("{id}")
		String resourceAction();

	}

}
