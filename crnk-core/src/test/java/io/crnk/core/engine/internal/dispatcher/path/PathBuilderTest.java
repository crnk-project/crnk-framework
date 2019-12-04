package io.crnk.core.engine.internal.dispatcher.path;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.CoreTestModule;
import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.internal.resource.NestedResourceTest;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.core.resource.annotations.JsonApiExposed;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class PathBuilderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PathBuilder pathBuilder;

    private QueryContext queryContext = new QueryContext().setRequestVersion(0);

    @Before
    public void prepare() {
        SimpleModule notExposedModule = new SimpleModule("notExposed");
        notExposedModule.addRepository(new NotExposedRepository());
        notExposedModule.addRepository(new NestedResourceTest.TestRepository());
        notExposedModule.addRepository(new NestedResourceTest.OneNestedRepository());
        notExposedModule.addRepository(new NestedResourceTest.ManyNestedRepository());
        notExposedModule.addRepository(new NestedResourceTest.OneGrandchildRepository());
        notExposedModule.addRepository(new NestedResourceTest.ManyGrandchildrenRepository());

        CoreTestContainer container = new CoreTestContainer();
        container.addModule(new CoreTestModule());
        container.addModule(notExposedModule);
        container.boot();

        pathBuilder = new PathBuilder(container.getResourceRegistry(), container.getBoot().getModuleRegistry().getTypeParser());

        RegistryEntry entry = container.getEntry(Task.class);
        ResourceRepositoryInformation repositoryInformation = entry.getRepositoryInformation();
        repositoryInformation.getActions().put("someRepositoryAction", Mockito.mock(RepositoryAction.class));
        repositoryInformation.getActions().put("someResourceAction", Mockito.mock(RepositoryAction.class));
    }

    @JsonApiExposed(false)
    public static class NotExposedRepository extends InMemoryResourceRepository<NotExposedResource, String> {

        public NotExposedRepository() {
            super(NotExposedResource.class);
        }
    }

    @JsonApiResource(type = "notExposed")
    public static class NotExposedResource {

        @JsonApiId
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    @Test
    public void onEmptyPathReturnsNull() {
        // GIVEN
        String path = "/";

        // WHEN
        JsonPath jsonPath = pathBuilder.build(path, queryContext);

        // THEN
        assertThat(jsonPath).isNull();
    }

    @Test
    public void onFlatResourcePathShouldReturnFlatPath() {
        // GIVEN
        String path = "/tasks/";

        // WHEN
        JsonPath jsonPath = pathBuilder.build(path, queryContext);

        // THEN
        assertThat(jsonPath.getRootEntry().getResourceInformation().getResourceType()).isEqualTo("tasks");
        assertThat(jsonPath.isCollection()).isTrue();
        assertThat(jsonPath.toGroupPath()).isEqualTo("tasks");
    }

    @Test
    public void onFlatResourceInstancePathShouldReturnFlatPath() {
        // GIVEN
        String path = "/tasks/1";

        // WHEN
        JsonPath jsonPath = pathBuilder.build(path, queryContext);

        // THEN
        assertThat(jsonPath.getRootEntry().getResourceInformation().getResourceType()).isEqualTo("tasks");
        assertThat(jsonPath.getId()).isEqualTo(1L);
        assertThat(jsonPath.isCollection()).isFalse();
        assertThat(jsonPath.toGroupPath()).isEqualTo("tasks/{id}");
    }

    @Test
    public void onRepositoryActionShouldActionPath() {
        // GIVEN
        String path = "/tasks/someRepositoryAction";

        // WHEN
        ActionPath jsonPath = (ActionPath) pathBuilder.build(path, queryContext);

        // THEN
        assertThat(jsonPath.getRootEntry().getResourceInformation().getResourceType()).isEqualTo("tasks");
        Assert.assertEquals("someRepositoryAction", jsonPath.getActionName());
        Assert.assertNull(jsonPath.getIds());
        assertThat(jsonPath.toGroupPath()).isEqualTo("tasks/someRepositoryAction");
    }

    @Test
    public void onResourceActionShouldActionPath() {
        // GIVEN
        String path = "/tasks/123/someResourceAction";

        // WHEN
        ActionPath jsonPath = (ActionPath) pathBuilder.build(path, queryContext);

        // THEN
        assertThat(jsonPath.getRootEntry().getResourceInformation().getResourceType()).isEqualTo("tasks");
        Assert.assertEquals("someResourceAction", jsonPath.getActionName());
        Assert.assertEquals(123L, jsonPath.getId());
        assertThat(jsonPath.toGroupPath()).isEqualTo("tasks/{id}/someResourceAction");
    }


    @Test
    public void onNestedResourcePathShouldReturnNestedPath() {
        // GIVEN
        String path = "/tasks/1/project";

        // WHEN
        FieldPath jsonPath = (FieldPath) pathBuilder.build(path, queryContext);

        // THEN
        assertThat(jsonPath.getRootEntry().getResourceInformation().getResourceType()).isEqualTo("tasks");
        Assert.assertEquals(1L, jsonPath.getId());
        Assert.assertEquals("project", jsonPath.getField().getJsonName());
        assertThat(jsonPath.toGroupPath()).isEqualTo("tasks/{id}/project");
    }

    @Test
    public void onNestedResourceInstancePathShouldThrowException() {
        // GIVEN
        String path = "/tasks/1/project/2";

        // THEN
        expectedException.expect(BadRequestException.class);

        // WHEN
        pathBuilder.build(path, queryContext);
    }

    @Test
    public void onNestedResourceRelationshipPathShouldReturnNestedPath() {
        // GIVEN
        String path = "/tasks/1/relationships/project/";

        // WHEN
        RelationshipsPath jsonPath = (RelationshipsPath) pathBuilder.build(path, queryContext);

        // THEN
        assertThat(jsonPath.getRootEntry().getResourceInformation().getResourceType()).isEqualTo("tasks");
        Assert.assertEquals(1L, jsonPath.getId());
        Assert.assertEquals("project", jsonPath.getRelationship().getJsonName());
        assertThat(jsonPath.toGroupPath()).isEqualTo("tasks/{id}/relationships/project");
    }

    @Test
    public void onNonRelationshipFieldShouldThrowException() {
        // GIVEN
        String path = "/tasks/1/relationships/name/";

        // THEN
        expectedException.expect(BadRequestException.class);

        // WHEN
        pathBuilder.build(path, queryContext);
    }

    @Test
    public void onRelationshipFieldInRelationshipsShouldThrowException() {
        // GIVEN
        String path = "/users/1/relationships/projects";

        // THEN
        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage("projects");

        // WHEN
        pathBuilder.build(path, queryContext);
    }

    @Test
    public void onNestedWrongResourceRelationshipPathShouldThrowException() {
        // GIVEN
        String path = "/tasks/1/relationships/";

        // THEN
        expectedException.expect(BadRequestException.class);

        // WHEN
        pathBuilder.build(path, queryContext);
    }

    @Test
    public void onRelationshipsPathWithIdShouldThrowException() {
        // GIVEN
        String path = "/tasks/1/relationships/project/1";

        // THEN
        expectedException.expect(BadRequestException.class);

        // WHEN
        pathBuilder.build(path, queryContext);
    }

    @Test
    public void onNonExistingFieldShouldThrowException() {
        // GIVEN
        String path = "/tasks/1/nonExistingField/";

        // THEN
        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage("nonExistingField");

        // WHEN
        pathBuilder.build(path, queryContext);
    }


    @Test
    public void singularSingularNestedRelationshipPath() {
        // GIVEN
        String path = "/test/1/oneNested/";

        // WHEN
        ResourcePath jsonPath = (ResourcePath) pathBuilder.build(path, queryContext);

        // THEN
        assertThat(jsonPath.getRootEntry().getResourceInformation().getResourceType()).isEqualTo("oneNested");
        Assert.assertEquals("1", jsonPath.getId());
        Assert.assertEquals("oneNested", jsonPath.getParentField().getUnderlyingName());
        assertThat(jsonPath.toGroupPath()).isEqualTo("test/{id}/oneNested");
    }

    @Test
    public void checkMultivaluedRelationshipPath() {
        // GIVEN
        String path = "/test/1/manyNested/";

        // WHEN
        FieldPath jsonPath = (FieldPath) pathBuilder.build(path, queryContext);

        // THEN
        assertThat(jsonPath.getRootEntry().getResourceInformation().getResourceType()).isEqualTo("test");
        Assert.assertEquals("1", jsonPath.getId());
        Assert.assertEquals("manyNested", jsonPath.getField().getUnderlyingName());
        assertThat(jsonPath.toGroupPath()).isEqualTo("test/{id}/manyNested");
    }

    @Test
    public void checkMultivaluedRelationshipIdPath() {
        // GIVEN
        String path = "/test/1/manyNested/2";

        // WHEN
        ResourcePath jsonPath = (ResourcePath) pathBuilder.build(path, queryContext);

        // THEN
        assertThat(jsonPath.getRootEntry().getResourceInformation().getResourceType()).isEqualTo("manyNested");
        Assert.assertEquals("1-2", jsonPath.getId().toString());
        Assert.assertEquals("manyNested", jsonPath.getParentField().getUnderlyingName());
        assertThat(jsonPath.toGroupPath()).isEqualTo("test/{id}/manyNested/{id}");
    }

    @Test
    public void onNonExistingResourceShouldThrowException() {
        String path = "/nonExistingResource";
        Assert.assertNull(pathBuilder.build(path, queryContext));
    }

    @Test
    public void onResourceStaringWithRelationshipsShouldThrowException() {
        String path = "/relationships";
        Assert.assertNull(pathBuilder.build(path, queryContext));
    }

    @Test
    public void onMultipleResourceInstancesPathShouldReturnCollectionPath() {
        // GIVEN
        String path = "/tasks/1,2";

        // WHEN
        JsonPath jsonPath = pathBuilder.build(path, queryContext);

        // THEN
        assertThat(jsonPath.getRootEntry().getResourceInformation().getResourceType()).isEqualTo("tasks");
        Assert.assertEquals(Arrays.asList(1L, 2L), jsonPath.getIds());
        assertThat(jsonPath.toGroupPath()).isEqualTo("tasks/{id}");
    }

    @Test
    public void onUrlEncodedMultipleResourceInstancesPathShouldReturnCollectionPath() {
        // GIVEN
        String path = "/tasks/1%2C2";

        // WHEN
        JsonPath jsonPath = pathBuilder.build(path, queryContext);

        // THEN
        assertThat(jsonPath.getRootEntry().getResourceInformation().getResourceType()).isEqualTo("tasks");
        Assert.assertEquals(Arrays.asList(1L, 2L), jsonPath.getIds());
    }

    @Test
    public void ignoreEntriesNotBeingExposed() {
        String path = "/notExposed/1/";
        JsonPath jsonPath = pathBuilder.build(path, queryContext);
        Assert.assertNull(jsonPath);
    }
}
