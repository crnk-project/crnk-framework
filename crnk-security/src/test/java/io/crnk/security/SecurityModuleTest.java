package io.crnk.security;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.engine.security.SecurityProviderContext;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
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
import io.crnk.security.repository.RolePermission;
import io.crnk.security.repository.RolePermissionRepository;
import io.crnk.security.repository.RoleRepository;
import io.crnk.test.mock.ClassTestUtils;
import io.crnk.test.mock.models.UnknownResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Set;
import java.util.stream.Collectors;

public class SecurityModuleTest {

    private SecurityModule securityModule;

    private String allowedRule = SecurityModule.ANY_ROLE;

    private CrnkBoot boot;

    private boolean authenticated;

    @Before
    public void setup() {
        // TODO simplify ones simple module is fixed
        SimpleModule appModule = new SimpleModule("app") {

            @Override
            public void setupModule(ModuleContext context) {
                super.setupModule(context);


                context.addSecurityProvider(new SecurityProvider() {

                    @Override
                    public boolean isUserInRole(String role, SecurityProviderContext context) {
                        return role.equals(allowedRule);
                    }

                    @Override
                    public boolean isAuthenticated(SecurityProviderContext context) {
                        return authenticated;
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
        RoleRepository repository = (RoleRepository) entry.getResourceRepository().getImplementation();
        ResourceList<Role> roles = repository.findAll(new QuerySpec(Role.class));

        Set<String> roleNames = roles.stream().map(it -> it.getId()).collect(Collectors.toSet());
        Assert.assertTrue(roleNames.contains("taskRole"));
        Assert.assertTrue(roleNames.contains("projectRole"));
        Assert.assertTrue(roleNames.contains(SecurityModule.ANY_ROLE));
        Assert.assertEquals(3, roleNames.size());
    }


    @Test
    public void testCallerPermissionRepository() {
        RegistryEntry entry = boot.getResourceRegistry().getEntry(CallerPermission.class);
        CallerPermissionRepository repository = (CallerPermissionRepository) entry.getResourceRepository().getImplementation();

        QuerySpec querySpec = new QuerySpec(CallerPermission.class);
        querySpec.addSort(PathSpec.of("resourceType").sort(Direction.ASC));
        ResourceList<CallerPermission> permissions = repository.findAll(querySpec);
        Assert.assertEquals("projects", permissions.get(0).getResourceType());
        Assert.assertEquals(ResourcePermission.GET, permissions.get(0).getPermission());
        Assert.assertEquals("tasks", permissions.get(4).getResourceType());
        Assert.assertEquals(ResourcePermission.GET, permissions.get(4).getPermission());
        Assert.assertNull(permissions.get(3).getDataRoomFilter());
    }

    @Test
    public void testRolePermissionRepository() {
        RegistryEntry entry = boot.getResourceRegistry().getEntry(RolePermission.class);
        RolePermissionRepository repository = (RolePermissionRepository) entry.getResourceRepository().getImplementation();

        QuerySpec querySpec = new QuerySpec(CallerPermission.class);
        querySpec.addSort(PathSpec.of("role").sort(Direction.ASC));
        querySpec.addFilter(PathSpec.of("resourceType").filter(FilterOperator.EQ, "projects"));
        ResourceList<RolePermission> permissions = repository.findAll(querySpec);

        Assert.assertEquals(3, permissions.size());

        RolePermission anyRole = permissions.get(0);
        Assert.assertEquals("ANY", anyRole.getRole());
        Assert.assertEquals("projects", anyRole.getResourceType());
        Assert.assertEquals(ResourcePermission.GET, anyRole.getPermission());
        Assert.assertNull(anyRole.getDataRoomFilter());

        RolePermission projectRole = permissions.get(1);
        Assert.assertEquals("projectRole", projectRole.getRole());
        Assert.assertEquals("projects", projectRole.getResourceType());
        Assert.assertEquals(ResourcePermission.POST.or(ResourcePermission.GET), projectRole.getPermission());
        Assert.assertNull(projectRole.getDataRoomFilter());

        RolePermission taskRole = permissions.get(2);
        Assert.assertEquals("taskRole", taskRole.getRole());
        Assert.assertEquals("projects", taskRole.getResourceType());
        Assert.assertEquals(ResourcePermission.GET, taskRole.getPermission());
        Assert.assertNull(taskRole.getDataRoomFilter());
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
            Assert.assertTrue(e.getMessage().contains("Repository for a resource not found: doesNotExist"));
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
        QueryContext queryContext = Mockito.mock(QueryContext.class);

        allowedRule = "taskRole";
        ResourcePermission projectPermissions = securityModule.getCallerPermissions(queryContext, "projects");
        ResourcePermission tasksPermissions = securityModule.getCallerPermissions(queryContext, "tasks");
        Assert.assertEquals(ResourcePermission.ALL, tasksPermissions);
        Assert.assertEquals(ResourcePermission.GET, projectPermissions);
        Assert.assertTrue(securityModule.isAllowed(queryContext, Project.class, ResourcePermission.GET));
        Assert.assertTrue(securityModule.isAllowed(queryContext, Task.class, ResourcePermission.GET));
        Assert.assertTrue(securityModule.isAllowed(queryContext, Task.class, ResourcePermission.ALL));
        Assert.assertFalse(securityModule.isAllowed(queryContext, Project.class, ResourcePermission.DELETE));
        Assert.assertFalse(securityModule.isAllowed(queryContext, Project.class, ResourcePermission.POST));
        allowedRule = "projectRole";
        projectPermissions = securityModule.getCallerPermissions(queryContext, "projects");
        tasksPermissions = securityModule.getCallerPermissions(queryContext, "tasks");
        Assert.assertEquals(ResourcePermission.GET, tasksPermissions);
        Assert.assertEquals(ResourcePermission.GET.or(ResourcePermission.POST), projectPermissions);
        Assert.assertTrue(securityModule.isAllowed(queryContext, Project.class, ResourcePermission.GET));
        Assert.assertTrue(securityModule.isAllowed(queryContext, Task.class, ResourcePermission.GET));
        Assert.assertFalse(securityModule.isAllowed(queryContext, Task.class, ResourcePermission.ALL));
        Assert.assertFalse(securityModule.isAllowed(queryContext, Project.class, ResourcePermission.DELETE));
        Assert.assertTrue(securityModule.isAllowed(queryContext, Project.class, ResourcePermission.POST));
    }

    @Test
    public void testBlackListingOfUnknownResources() {
        QueryContext queryContext = Mockito.mock(QueryContext.class);
        Assert.assertEquals(ResourcePermission.EMPTY, securityModule.getResourcePermission(queryContext, "doesNotExist"));
    }


    @Test(expected = RepositoryNotFoundException.class)
    public void testBlackListingOfUnknownClass() {
        QueryContext queryContext = Mockito.mock(QueryContext.class);
        securityModule.isAllowed(queryContext, UnknownResource.class, ResourcePermission.GET);
    }


    @Test
    public void testReconfigure() {
        QueryContext queryContext = Mockito.mock(QueryContext.class);
        Assert.assertTrue(securityModule.isAllowed(queryContext, Project.class, ResourcePermission.GET));
        Assert.assertFalse(securityModule.isAllowed(queryContext, Project.class, ResourcePermission.DELETE));

        Builder builder = SecurityConfig.builder();
        builder.permitRole(allowedRule, "projects", ResourcePermission.DELETE);
        securityModule.reconfigure(builder.build());
        Assert.assertFalse(securityModule.isAllowed(queryContext, Project.class, ResourcePermission.GET));
        Assert.assertTrue(securityModule.isAllowed(queryContext, Project.class, ResourcePermission.DELETE));
    }

    @Test
    public void testUnknownResource() {
        QueryContext queryContext = Mockito.mock(QueryContext.class);
        allowedRule = "taskRole";
        Assert.assertTrue(securityModule.isAllowed(queryContext, Task.class, ResourcePermission.GET));
        Assert.assertTrue(securityModule.isAllowed(queryContext, Task.class, ResourcePermission.ALL));
        Assert.assertFalse(securityModule.isAllowed(queryContext, Project.class, ResourcePermission.DELETE));
        Assert.assertFalse(securityModule.isAllowed(queryContext, Project.class, ResourcePermission.POST));
        allowedRule = "projectRole";
        Assert.assertTrue(securityModule.isAllowed(queryContext, Project.class, ResourcePermission.GET));
        Assert.assertTrue(securityModule.isAllowed(queryContext, Task.class, ResourcePermission.GET));
        Assert.assertFalse(securityModule.isAllowed(queryContext, Task.class, ResourcePermission.ALL));
        Assert.assertFalse(securityModule.isAllowed(queryContext, Project.class, ResourcePermission.DELETE));
        Assert.assertTrue(securityModule.isAllowed(queryContext, Project.class, ResourcePermission.POST));
    }
}
