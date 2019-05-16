package io.crnk.core.engine.internal.dispatcher.path;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
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

public class PathBuilderTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private PathBuilder pathBuilder;

	@Before
	public void prepare() {
		SimpleModule notExposedModule = new SimpleModule("notExposed");
		notExposedModule.addRepository(new NotExposedRepository());

		CoreTestContainer container = new CoreTestContainer();
		container.setDefaultPackage();
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
		JsonPath jsonPath = pathBuilder.build(path);

		// THEN
		assertThat(jsonPath).isNull();
	}

	@Test
	public void onFlatResourcePathShouldReturnFlatPath() {
		// GIVEN
		String path = "/tasks/";

		// WHEN
		JsonPath jsonPath = pathBuilder.build(path);

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
		JsonPath jsonPath = pathBuilder.build(path);

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
		ActionPath jsonPath = (ActionPath) pathBuilder.build(path);

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
		ActionPath jsonPath = (ActionPath) pathBuilder.build(path);

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
		FieldPath jsonPath = (FieldPath) pathBuilder.build(path);

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
		pathBuilder.build(path);
	}

	@Test
	public void onNestedResourceRelationshipPathShouldReturnNestedPath() {
		// GIVEN
		String path = "/tasks/1/relationships/project/";

		// WHEN
		RelationshipsPath jsonPath = (RelationshipsPath) pathBuilder.build(path);

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
		pathBuilder.build(path);
	}

	@Test
	public void onRelationshipFieldInRelationshipsShouldThrowException() {
		// GIVEN
		String path = "/users/1/relationships/projects";

		// THEN
		expectedException.expect(BadRequestException.class);
		expectedException.expectMessage("projects");

		// WHEN
		pathBuilder.build(path);
	}

	@Test
	public void onNestedWrongResourceRelationshipPathShouldThrowException() {
		// GIVEN
		String path = "/tasks/1/relationships/";

		// THEN
		expectedException.expect(BadRequestException.class);

		// WHEN
		pathBuilder.build(path);
	}

	@Test
	public void onRelationshipsPathWithIdShouldThrowException() {
		// GIVEN
		String path = "/tasks/1/relationships/project/1";

		// THEN
		expectedException.expect(BadRequestException.class);

		// WHEN
		pathBuilder.build(path);
	}

	@Test
	public void onNonExistingFieldShouldThrowException() {
		// GIVEN
		String path = "/tasks/1/nonExistingField/";

		// THEN
		expectedException.expect(BadRequestException.class);
		expectedException.expectMessage("nonExistingField");

		// WHEN
		pathBuilder.build(path);
	}

	@Test
	public void onNonExistingResourceShouldThrowException() {
		String path = "/nonExistingResource";
		Assert.assertNull(pathBuilder.build(path));
	}

	@Test
	public void onResourceStaringWithRelationshipsShouldThrowException() {
		String path = "/relationships";
		Assert.assertNull(pathBuilder.build(path));
	}

	@Test
	public void onMultipleResourceInstancesPathShouldReturnCollectionPath() {
		// GIVEN
		String path = "/tasks/1,2";

		// WHEN
		JsonPath jsonPath = pathBuilder.build(path);

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
		JsonPath jsonPath = pathBuilder.build(path);

		// THEN
		assertThat(jsonPath.getRootEntry().getResourceInformation().getResourceType()).isEqualTo("tasks");
		Assert.assertEquals(Arrays.asList(1L, 2L), jsonPath.getIds());
	}

	@Test
	public void ignoreEntriesNotBeingExposed() {
		String path = "/notExposed/1/";
		JsonPath jsonPath = pathBuilder.build(path);
		Assert.assertNull(jsonPath);
	}
}
