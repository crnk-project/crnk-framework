package io.crnk.rs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.RepositoryAction.RepositoryActionType;
import io.crnk.core.engine.information.repository.RepositoryInformationProviderContext;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.internal.information.DefaultInformationBuilder;
import io.crnk.core.engine.internal.information.resource.DefaultResourceFieldInformationProvider;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInformationProvider;
import io.crnk.core.engine.internal.jackson.JacksonResourceFieldInformationProvider;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.legacy.registry.DefaultResourceInformationProviderContext;
import io.crnk.rs.internal.JaxrsModule;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.SecurityContext;
import java.util.Map;

public class JaxrsModuleTest {

	private JaxrsModule.JaxrsResourceRepositoryInformationProvider builder;

	private RepositoryInformationProviderContext context;

	@Before
	public void setup() {
		final ModuleRegistry moduleRegistry = new ModuleRegistry();
		builder = new JaxrsModule.JaxrsResourceRepositoryInformationProvider();
		final ResourceInformationProvider resourceInformationProvider = new DefaultResourceInformationProvider(
				moduleRegistry.getPropertiesProvider(),
				ImmutableList.<PagingBehavior>of(new OffsetLimitPagingBehavior()),
				new DefaultResourceFieldInformationProvider(),
				new JacksonResourceFieldInformationProvider());
		resourceInformationProvider
				.init(new DefaultResourceInformationProviderContext(resourceInformationProvider, new DefaultInformationBuilder(moduleRegistry.getTypeParser()), moduleRegistry.getTypeParser(), new ObjectMapper()));
		context = new RepositoryInformationProviderContext() {

			@Override
			public ResourceInformationProvider getResourceInformationBuilder() {
				return resourceInformationProvider;
			}

			@Override
			public TypeParser getTypeParser() {
				return moduleRegistry.getTypeParser();
			}

			@Override
			public InformationBuilder builder() {
				return new DefaultInformationBuilder(moduleRegistry.getTypeParser());
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

		Assert.assertTrue(securityProvider.isUserInRole("admin").get());
		Assert.assertFalse(securityProvider.isUserInRole("other").get());
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
	public interface TaskRepository extends ResourceRepository<Task, Long> {

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
	public interface InvalidRootPathRepository extends ResourceRepository<Task, Long> {

		@GET
		@Path("")
		String resourceAction();

	}

	@Path("schedules")
	public interface MissingPathRepository1 extends ResourceRepository<Task, Long> {

		@GET
		String resourceAction();

	}

	@Path("schedules")
	public interface MissingPathRepository2 extends ResourceRepository<Task, Long> {

		String resourceAction(@PathParam("id") long id);

	}

	@Path("schedules")
	public interface PathToLongRepository extends ResourceRepository<Task, Long> {

		@GET
		@Path("a/b/c")
		String resourceAction();

	}

	@Path("schedules")
	public interface InvalidIdPathRepository1 extends ResourceRepository<Task, Long> {

		@GET
		@Path("{something}/test")
		String resourceAction();

	}

	@Path("schedules")
	public interface InvalidIdPathRepository2 extends ResourceRepository<Task, Long> {

		@GET
		@Path("{id}")
		String resourceAction();

	}

}
