package io.crnk.security;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.module.SimpleModule;
import io.crnk.security.SecurityConfig.Builder;
import io.crnk.security.model.Project;
import io.crnk.security.model.ProjectRepository;
import io.crnk.security.model.Task;
import io.crnk.security.model.TaskRepository;
import io.crnk.test.mock.ClassTestUtils;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.UnknownResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SecurityModuleTest {

	private SecurityModule securityModule;

	private String allowedRule;

	@Before
	public void setup() {
		// TODO simplify ones simple module is fixed
		SimpleModule appModule = new SimpleModule("app") {

			@Override
			public void setupModule(ModuleContext context) {
				super.setupModule(context);

				context.addSecurityProvider(new SecurityProvider() {

					@Override
					public boolean isUserInRole(String role) {
						return role.equals(allowedRule);
					}
				});
			}
		};
		appModule.addRepository(new TaskRepository());
		appModule.addRepository(new ProjectRepository());

		Builder builder = SecurityConfig.builder();
		builder.permitAll(ResourcePermission.GET);
		builder.permitRole("taskRole", Task.class, ResourcePermission.ALL);
		builder.permitRole("projectRole", "projects", ResourcePermission.POST);
		SecurityConfig config = builder.build();
		securityModule = SecurityModule.newServerModule(config);
		Assert.assertSame(config, securityModule.getConfig());

		CrnkBoot boot = new CrnkBoot();
		boot.addModule(securityModule);
		boot.addModule(appModule);
		boot.boot();
	}

	@Test
	public void testInvalidClassNameThrowsException() {
		Builder builder = SecurityConfig.builder();
		builder.permitRole("taskRole", Task.class, ResourcePermission.ALL);
		securityModule = SecurityModule.newServerModule(builder.build());

		CrnkBoot boot = new CrnkBoot();
		boot.addModule(securityModule);
		boot.boot();
		try {
			securityModule.checkInit();
			Assert.fail();
		} catch (RepositoryNotFoundException e) {
			Assert.assertEquals("Repository for a resource not found: io.crnk.security.model.Task", e.getMessage());
		}
	}

	@Test
	public void testInvalidResourceTypeThrowsException() {
		Builder builder = SecurityConfig.builder();
		builder.permitRole("taskRole", "doesNotExist", ResourcePermission.ALL);
		securityModule = SecurityModule.newServerModule(builder.build());

		CrnkBoot boot = new CrnkBoot();
		boot.addModule(securityModule);
		boot.boot();
		try {
			securityModule.checkInit();
			Assert.fail();
		} catch (RepositoryNotFoundException e) {
			Assert.assertEquals("Repository for a resource not found: doesNotExist", e.getMessage());
		}
	}

	@Test
	public void testModuleName() {
		Assert.assertEquals("security", securityModule.getModuleName());
	}

	@Test
	public void hasProtectedConstructor() {
		ClassTestUtils.assertProtectedConstructor(SecurityModule.class);
	}

	@Test
	public void testAllowed() {
		allowedRule = "taskRole";
		Assert.assertTrue(securityModule.isAllowed(Project.class, ResourcePermission.GET));
		Assert.assertTrue(securityModule.isAllowed(Task.class, ResourcePermission.GET));
		Assert.assertTrue(securityModule.isAllowed(Task.class, ResourcePermission.ALL));
		Assert.assertFalse(securityModule.isAllowed(Project.class, ResourcePermission.DELETE));
		Assert.assertFalse(securityModule.isAllowed(Project.class, ResourcePermission.POST));
		allowedRule = "projectRole";
		Assert.assertTrue(securityModule.isAllowed(Project.class, ResourcePermission.GET));
		Assert.assertTrue(securityModule.isAllowed(Task.class, ResourcePermission.GET));
		Assert.assertFalse(securityModule.isAllowed(Task.class, ResourcePermission.ALL));
		Assert.assertFalse(securityModule.isAllowed(Project.class, ResourcePermission.DELETE));
		Assert.assertTrue(securityModule.isAllowed(Project.class, ResourcePermission.POST));
	}

	@Test
	public void testBlackListingOfUnknownResources() {
		Assert.assertEquals(ResourcePermission.EMPTY, securityModule.getResourcePermission("doesNotExist"));
	}


	@Test(expected = RepositoryNotFoundException.class)
	public void testBlackListingOfUnknownClass() {
		securityModule.isAllowed(UnknownResource.class, ResourcePermission.GET);
	}


	@Test
	public void testReconfigure() {
		Assert.assertTrue(securityModule.isAllowed(Project.class, ResourcePermission.GET));
		Assert.assertFalse(securityModule.isAllowed(Project.class, ResourcePermission.DELETE));

		Builder builder = SecurityConfig.builder();
		builder.permitRole(allowedRule, "projects", ResourcePermission.DELETE);
		securityModule.reconfigure(builder.build());
		Assert.assertFalse(securityModule.isAllowed(Project.class, ResourcePermission.GET));
		Assert.assertTrue(securityModule.isAllowed(Project.class, ResourcePermission.DELETE));
	}

	@Test
	public void testUnknownResource() {
		allowedRule = "taskRole";
		Assert.assertTrue(securityModule.isAllowed(Task.class, ResourcePermission.GET));
		Assert.assertTrue(securityModule.isAllowed(Task.class, ResourcePermission.ALL));
		Assert.assertFalse(securityModule.isAllowed(Project.class, ResourcePermission.DELETE));
		Assert.assertFalse(securityModule.isAllowed(Project.class, ResourcePermission.POST));
		allowedRule = "projectRole";
		Assert.assertTrue(securityModule.isAllowed(Project.class, ResourcePermission.GET));
		Assert.assertTrue(securityModule.isAllowed(Task.class, ResourcePermission.GET));
		Assert.assertFalse(securityModule.isAllowed(Task.class, ResourcePermission.ALL));
		Assert.assertFalse(securityModule.isAllowed(Project.class, ResourcePermission.DELETE));
		Assert.assertTrue(securityModule.isAllowed(Project.class, ResourcePermission.POST));
	}
}
