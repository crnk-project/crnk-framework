package io.crnk.security;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.security.SecurityConfig.Builder;
import io.crnk.security.model.Project;
import io.crnk.security.model.ProjectRepository;
import io.crnk.security.model.Task;
import io.crnk.security.model.TaskRepository;
import io.crnk.security.repository.CallerPermission;
import io.crnk.security.repository.CallerPermissionRepository;
import io.crnk.security.repository.Role;
import io.crnk.security.repository.RoleRepository;
import io.crnk.test.mock.ClassTestUtils;
import io.crnk.test.mock.models.UnknownResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;

public class SecurityModuleTest {

    private SecurityModule securityModule;

    private String allowedRule;

    private CrnkBoot boot;

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
        builder.exposeRepositories(true);
        builder.permitRole("taskRole", Task.class, ResourcePermission.ALL);
        builder.permitRole("projectRole", "projects", ResourcePermission.POST);
        SecurityConfig config = builder.build();
        securityModule = SecurityModule.newServerModule(config);
        Assert.assertSame(config, securityModule.getConfig());

        boot = new CrnkBoot();
        boot.addModule(securityModule);
        boot.addModule(appModule);
        boot.boot();
    }

    @Test
    public void testRolesRepository() {
        RegistryEntry entry = boot.getResourceRegistry().getEntry(Role.class);
        RoleRepository repository = (RoleRepository) entry.getResourceRepository().getResourceRepository();
        ResourceList<Role> roles = repository.findAll(new QuerySpec(Role.class));

        Set<String> roleNames = roles.stream().map(it -> it.getId()).collect(Collectors.toSet());
        Assert.assertTrue(roleNames.contains("taskRole"));
        Assert.assertTrue(roleNames.contains("projectRole"));
        Assert.assertEquals(2, roleNames.size());
    }


    @Test
    public void testCallerPermissionRepository() {
        RegistryEntry entry = boot.getResourceRegistry().getEntry(CallerPermission.class);
        CallerPermissionRepository repository = (CallerPermissionRepository) entry.getResourceRepository().getResourceRepository();

        QuerySpec querySpec = new QuerySpec(CallerPermission.class);
        querySpec.addSort(PathSpec.of("resourceType").sort(Direction.ASC));
        ResourceList<CallerPermission> permissions = repository.findAll(querySpec);
        Assert.assertEquals("projects", permissions.get(0).getResourceType());
        Assert.assertEquals(ResourcePermission.GET, permissions.get(0).getPermission());
        Assert.assertEquals("tasks", permissions.get(3).getResourceType());
        Assert.assertEquals(ResourcePermission.GET, permissions.get(3).getPermission());
        Assert.assertNull(permissions.get(3).getDataRoomFilter());
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
        ResourcePermission projectPermissions = securityModule.getCallerPermissions("projects");
        ResourcePermission tasksPermissions = securityModule.getCallerPermissions("tasks");
        Assert.assertEquals(ResourcePermission.ALL, tasksPermissions);
        Assert.assertEquals(ResourcePermission.GET, projectPermissions);
        Assert.assertTrue(securityModule.isAllowed(Project.class, ResourcePermission.GET));
        Assert.assertTrue(securityModule.isAllowed(Task.class, ResourcePermission.GET));
        Assert.assertTrue(securityModule.isAllowed(Task.class, ResourcePermission.ALL));
        Assert.assertFalse(securityModule.isAllowed(Project.class, ResourcePermission.DELETE));
        Assert.assertFalse(securityModule.isAllowed(Project.class, ResourcePermission.POST));
        allowedRule = "projectRole";
        projectPermissions = securityModule.getCallerPermissions("projects");
        tasksPermissions = securityModule.getCallerPermissions("tasks");
        Assert.assertEquals(ResourcePermission.GET, tasksPermissions);
        Assert.assertEquals(ResourcePermission.GET.or(ResourcePermission.POST), projectPermissions);
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
