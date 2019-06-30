package io.crnk.security;

import io.crnk.client.CrnkClient;
import io.crnk.client.http.okhttp.OkHttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapterListenerBase;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.exception.UnauthorizedException;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.rs.CrnkFeature;
import io.crnk.security.SecurityConfig.Builder;
import io.crnk.security.model.Project;
import io.crnk.security.model.ProjectRepository;
import io.crnk.security.model.Task;
import io.crnk.security.model.TaskRepository;
import io.crnk.security.model.TaskToProjectRepository;
import io.crnk.test.JerseyTestBase;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class SecurityModuleIntTest extends JerseyTestBase {

    private final InMemoryIdentityManager identityManager = new InMemoryIdentityManager();

    protected CrnkClient client;

    protected ResourceRepository<Task, Long> taskRepo;

    protected ResourceRepository<Project, Long> projectRepo;

    protected RelationshipRepository<Task, Long, Project, Long> relRepo;

    private SecurityModule module;

    private String userName;

    private String password;

    private static int responseCount(Response response) {
        Response priorResponse = response;
        int result = 1;
        while ((priorResponse = priorResponse.priorResponse()) != null) {
            result++;
        }
        return result;
    }

    @Override
    protected Application configure() {
        return new TestApplication();

    }

    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        final TestContainerFactory testContainerFactory = super.getTestContainerFactory();

        return new TestContainerFactory() {

            @Override
            public TestContainer create(URI baseUri, DeploymentContext deploymentContext) {
                TestContainer container = testContainerFactory.create(baseUri, deploymentContext);
                try {
                    Field field = container.getClass().getDeclaredField("server");
                    field.setAccessible(true);
                    Server server = (Server) field.get(container);

                    Handler handler = server.getHandler();
                    SecurityHandler securityHandler = identityManager.getSecurityHandler();
                    if (securityHandler.getHandler() == null) {
                        securityHandler.setHandler(handler);
                    }
                    server.setHandler(securityHandler);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
                return container;
            }
        };
    }

    @After
    @Before
    public void cleanup() {
        module.setEnabled(true);
    }

    @Before
    public void setup() {
        identityManager.clear();

        client = new CrnkClient(getBaseUri().toString());
        client.addModule(SecurityModule.newClientModule());
        client.getHttpAdapter().setReceiveTimeout(1000000, TimeUnit.MILLISECONDS);

        taskRepo = client.getRepositoryForType(Task.class);
        projectRepo = client.getRepositoryForType(Project.class);
        relRepo = client.getRepositoryForType(Task.class, Project.class);

        userName = "doe";
        password = "doePass";

        OkHttpAdapter httpAdapter = (OkHttpAdapter) client.getHttpAdapter();
        httpAdapter.addListener(new OkHttpAdapterListenerBase() {

            @Override
            public void onBuild(OkHttpClient.Builder builder) {
                if (userName != null) {
                    builder.authenticator(new TestAuthenticator(userName, password));
                }
            }

        });

        TaskRepository.clear();
        ProjectRepository.clear();
    }

    @Test
    public void metaAllPermissions() {
        identityManager.addUser("doe", "doePass", "allRole");

        ResourceList<Project> list = projectRepo.findAll(new QuerySpec(Project.class));
        ResourcePermissionInformation metaInformation = list.getMeta(ResourcePermissionInformationImpl.class);
        ResourcePermission resourcePermission = metaInformation.getResourcePermission();
        Assert.assertEquals(ResourcePermission.ALL, resourcePermission);
    }

    @Test
    public void metaGetPatchPermissions() {
        identityManager.addUser("doe", "doePass");

        ResourceList<Project> list = projectRepo.findAll(new QuerySpec(Project.class));
        ResourcePermissionInformation metaInformation = list.getMeta(ResourcePermissionInformationImpl.class);
        ResourcePermission resourcePermission = metaInformation.getResourcePermission();
        Assert.assertEquals(ResourcePermission.GET.or(ResourcePermission.POST), resourcePermission);
    }

    @Test
    public void rootAll() {
        identityManager.addUser("doe", "doePass", "allRole");

        Project project = new Project();
        project.setId(1L);
        project.setName("test");
        projectRepo.create(project);

        project.setName("updated");
        projectRepo.save(project);

        project = projectRepo.findOne(project.getId(), new QuerySpec(Project.class));
        Assert.assertNotNull(project);

        projectRepo.delete(project.getId());
    }

    @Test(expected = ForbiddenException.class)
    public void forbiddenPost() {
        identityManager.addUser("doe", "doePass", "getRole");

        Task task = new Task();
        task.setId(1L);
        task.setName("test");
        taskRepo.create(task);
    }


    @Test(expected = UnauthorizedException.class)
    public void unauthorizedPost() {
        userName = null; // do not authenticate

        Task task = new Task();
        task.setId(1L);
        task.setName("test");
        taskRepo.create(task);
    }

    @Test
    public void disableSecurityModule() {
        module.setEnabled(false);

        Assert.assertEquals(ResourcePermission.ALL, module.getCallerPermissions("projects"));
        Assert.assertEquals(ResourcePermission.ALL, module.getCallerPermissions("tasks"));
        Assert.assertTrue(module.isAllowed(Project.class, ResourcePermission.ALL));
        Assert.assertTrue(module.isAllowed(Task.class, ResourcePermission.ALL));
        Assert.assertEquals(ResourcePermission.ALL, module.getResourcePermission(Task.class));
    }

    @Test(expected = IllegalStateException.class)
    public void noIsRolesAllowedWhenDisabled() {
        module.setEnabled(false);

        module.isUserInRole("whatever");
    }

    @Test
    public void getPostOnly() {
        identityManager.addUser("doe", "doePass", "getRole", "postRole");

        Project project = new Project();
        project.setId(1L);
        project.setName("test");
        projectRepo.create(project);

        project = projectRepo.findOne(project.getId(), new QuerySpec(Project.class));
        Assert.assertNotNull(project);
    }

    @Test(expected = UnauthorizedException.class)
    public void unauthorizedException() {
        identityManager.addUser("otherUser", "doePass", "allRole");

        Project project = new Project();
        project.setId(1L);
        project.setName("test");
        projectRepo.create(project);
    }

    @Test
    public void permitAllMatchAnyType() {
        identityManager.addUser("doe", "doePass");
        projectRepo.findAll(new QuerySpec(Project.class));
    }

    @Test
    public void permitAllMatchProjectType() {
        identityManager.addUser("doe", "doePass");
        Project project = new Project();
        project.setId(1L);
        project.setName("test");
        projectRepo.create(project);
    }

    @Test(expected = ForbiddenException.class)
    public void permitAllNoMatch() {
        identityManager.addUser("doe", "doePass");
        Task task = new Task();
        task.setId(1L);
        task.setName("test");
        taskRepo.create(task);
    }

    private static class TestAuthenticator implements Authenticator {

        private String userName;

        private String password;

        public TestAuthenticator(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }

        @Override
        public Request authenticate(Route route, Response response) { // NOSONAR this is a lambda, legacy cannot be removed!
            if (responseCount(response) >= 3) {
                return null; // If we've failed 3 times, give up.
            }
            String credential = Credentials.basic(userName, password);
            return response.request().newBuilder().header("Authorization", credential).build();
        }
    }

    @ApplicationPath("/")
    private class TestApplication extends ResourceConfig {

        public TestApplication() {
            SimpleModule testModule = new SimpleModule("test");
            testModule.addRepository(new ProjectRepository());
            testModule.addRepository(new TaskRepository());
            testModule.addRepository(new TaskToProjectRepository());

            // tag::setup[]
            Builder builder = SecurityConfig.builder();
            builder.permitRole("allRole", ResourcePermission.ALL);
            builder.permitRole("getRole", ResourcePermission.GET);
            builder.permitRole("patchRole", ResourcePermission.PATCH);
            builder.permitRole("postRole", ResourcePermission.POST);
            builder.permitRole("deleteRole", ResourcePermission.DELETE);
            builder.permitRole("taskRole", Task.class, ResourcePermission.ALL);
            builder.permitRole("taskReadRole", Task.class, ResourcePermission.GET);
            builder.permitRole("projectRole", Project.class, ResourcePermission.ALL);
            builder.permitAll(ResourcePermission.GET);
            builder.permitAll(Project.class, ResourcePermission.POST);
            module = SecurityModule.newServerModule(builder.build());

            CrnkFeature feature = new CrnkFeature();
            feature.addModule(module);
            feature.addModule(testModule);
            // end::setup[]
            register(feature);
        }
    }

}
