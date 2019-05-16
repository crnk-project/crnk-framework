package io.crnk.example.springboot.simple;

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.example.springboot.domain.model.Project;
import io.crnk.example.springboot.domain.model.ScheduleEntity;
import io.crnk.example.springboot.domain.model.Task;
import io.crnk.example.springboot.domain.model.UserEntity;
import io.crnk.example.springboot.domain.repository.ProjectRepository;
import io.crnk.example.springboot.domain.repository.ProjectRepository.ProjectList;
import io.crnk.example.springboot.domain.repository.ProjectRepository.ProjectListLinks;
import io.crnk.example.springboot.domain.repository.ProjectRepository.ProjectListMeta;
import org.apache.catalina.authenticator.jaspic.AuthConfigFactoryImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import javax.security.auth.message.config.AuthConfigFactory;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * Shows two kinds of test cases: RestAssured and CrnkClient.
 */
public class SpringBootSimpleExampleApplicationTests extends BaseTest {

    @Before
    public void setup() {
        // NPE fix
        if (AuthConfigFactory.getFactory() == null) {
            AuthConfigFactory.setFactory(new AuthConfigFactoryImpl());
        }
    }

    @Test
    public void testClient() {
        ProjectRepository projectRepo = client.getRepositoryForInterface(ProjectRepository.class);
        QuerySpec querySpec = new QuerySpec(Project.class);
        querySpec.setLimit(10L);
        ProjectList list = projectRepo.findAll(querySpec);
        Assert.assertNotEquals(0, list.size());

        // test meta access
        ProjectListMeta meta = list.getMeta();
        Assert.assertEquals(4L, meta.getTotalResourceCount().longValue());

        // test pagination links access
        ProjectListLinks links = list.getLinks();
        Assert.assertNotNull(links.getFirst());
    }

    @Test
    public void testRelationship() {
        RelationshipRepository<Project, Serializable, Task, Serializable> relRepo = client.getRepositoryForType(Project.class, Task.class);
        QuerySpec querySpec = new QuerySpec(Project.class);
        ResourceList<Task> tasks = relRepo.findManyTargets(123L, "tasks", querySpec);
        Assert.assertEquals(1, tasks.size());
    }

    @Test
    public void testUi() {
        Response response = RestAssured.given().when().get("/api/browse/");
        response.then().assertThat().statusCode(200);
        String body = response.getBody().print();
        Assert.assertTrue(body.contains("<title>Crnk UI</title>"));
    }

    @Test
    public void testUiRegistrationWithHome() {
        Response response = RestAssured.given().when().get("/api/");
        response.then().assertThat().statusCode(200);
        String body = response.getBody().print();
        Assert.assertTrue(body, body.contains("/api/browse/"));
    }

    @Test
    public void testJpaEntityAccess() {
        ResourceRepository<ScheduleEntity, Serializable> entityRepo = client.getRepositoryForType(ScheduleEntity.class);

        QuerySpec querySpec = new QuerySpec(ScheduleEntity.class);
        ResourceList<ScheduleEntity> list = entityRepo.findAll(querySpec);
        for (ScheduleEntity schedule : list) {
            entityRepo.delete(schedule.getId());
        }

        ScheduleEntity schedule = new ScheduleEntity();
        schedule.setId(13L);
        schedule.setName("My Schedule");
        entityRepo.create(schedule);

        list = entityRepo.findAll(querySpec);
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testJpaEntity() {
        ResourceRepository<ScheduleEntity, Serializable> entityRepo = client.getRepositoryForType(ScheduleEntity.class);

        QuerySpec querySpec = new QuerySpec(ScheduleEntity.class);
        ResourceList<ScheduleEntity> list = entityRepo.findAll(querySpec);
        for (ScheduleEntity schedule : list) {
            entityRepo.delete(schedule.getId());
        }

        ScheduleEntity schedule = new ScheduleEntity();
        schedule.setId(13L);
        schedule.setName("My Schedule");
        entityRepo.create(schedule);

        list = entityRepo.findAll(querySpec);
        Assert.assertEquals(1, list.size());
        schedule = list.get(0);
        Assert.assertEquals(13L, schedule.getId().longValue());
        Assert.assertEquals("My Schedule", schedule.getName());
    }

    @Test
    public void testJpaInclusion() {
        ResourceRepository<ScheduleEntity, Serializable> entityRepo = client.getRepositoryForType(ScheduleEntity.class);

        QuerySpec querySpec = new QuerySpec(ScheduleEntity.class);
        querySpec.includeRelation(PathSpec.of("creator"));
        querySpec.includeRelation(PathSpec.of("verifiers"));

        ResourceList<ScheduleEntity> list = entityRepo.findAll(querySpec);
        for (ScheduleEntity schedule : list) {
            if (schedule.getName().startsWith("schedule")) {
                Assert.assertNotNull(schedule.getCreator());
                Set<UserEntity> verifiers = schedule.getVerifiers();
                Assert.assertEquals(2, verifiers.size());
            }
        }
    }

    @Test
    public void testOppositeInclusion() {
        ResourceRepository<UserEntity, Serializable> entityRepo = client.getRepositoryForType(UserEntity.class);

        QuerySpec querySpec = new QuerySpec(UserEntity.class);
        querySpec.includeRelation(PathSpec.of("createdSchedules"));

        ResourceList<UserEntity> list = entityRepo.findAll(querySpec);
        for (UserEntity user : list) {
            Assert.assertNotNull(user.getCreatedSchedules());
        }
    }

    @Test
    public void testFindOne() {
        testFindOne("/api/tasks/1");
        testFindOne("/api/projects/123");
    }

    @Test
    public void testFindOne_NotFound() {
        testFindOne_NotFound("/api/tasks/0");
        testFindOne_NotFound("/api/projects/0");
    }

    @Test
    public void testFindMany() {
        testFindMany("/api/tasks");
        testFindMany("/api/projects");
    }

    @Test
    public void testDelete() {
        testDelete("/api/tasks/1");
        testDelete("/api/projects/123");
    }

    @Test
    public void testCreateTask() {
        Map<String, Object> attributeMap = new ImmutableMap.Builder<String, Object>().put("my-name", "Getter Done")
                .put("description", "12345678901234567890").build();

        Map dataMap = ImmutableMap.of("data", ImmutableMap.of("type", "tasks", "attributes", attributeMap));

        ValidatableResponse response = RestAssured.given().contentType("application/vnd.api+json").body(dataMap).when().post
                ("/api/tasks")
                .then().statusCode(CREATED.value());
        response.assertThat().body(matchesJsonSchema(jsonApiSchema));
    }

    @Test
    public void testUpdateTask() {
        Map<String, Object> attributeMap = new ImmutableMap.Builder<String, Object>().put("my-name", "Gotter Did")
                .put("description", "12345678901234567890").build();

        Map dataMap = ImmutableMap.of("data", ImmutableMap.of("type", "tasks", "id", 1, "attributes", attributeMap));

        RestAssured.given().contentType("application/vnd.api+json").body(dataMap).when().patch("/api/tasks/1").then()
                .statusCode(OK.value());
    }

    @Test
    public void testUpdateTask_withDescriptionTooLong() {
        Map<String, Object> attributeMap = new ImmutableMap.Builder<String, Object>().put("description", "123456789012345678901")
                .build();

        Map dataMap = ImmutableMap.of("data", ImmutableMap.of("type", "tasks", "id", 1, "attributes", attributeMap));

        ValidatableResponse response = RestAssured.given().contentType("application/vnd.api+json").body(dataMap).when()
                .patch("/api/tasks/1").then().statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
        response.assertThat().body(matchesJsonSchema(jsonApiSchema));
    }

    @Test
    public void testAccessHome() {
        RestAssured.given().contentType("*").when().get("/api/").then()
                .statusCode(OK.value());
    }
}
