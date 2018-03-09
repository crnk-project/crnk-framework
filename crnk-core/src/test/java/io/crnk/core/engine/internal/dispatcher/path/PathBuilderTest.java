package io.crnk.core.engine.internal.dispatcher.path;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.exception.ResourceException;
import io.crnk.core.exception.ResourceFieldNotFoundException;
import io.crnk.core.mock.MockConstants;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.Module;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.core.resource.registry.ResourceRegistryTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class PathBuilderTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	private PathBuilder pathBuilder;

	@Before
	public void prepare() {
		CoreTestContainer container = new CoreTestContainer();
		container.setDefaultPackage();
		container.boot();

		pathBuilder = new PathBuilder(container.getResourceRegistry());

		RegistryEntry entry = container.getEntry(Task.class);
		ResourceRepositoryInformation repositoryInformation = entry.getRepositoryInformation();
		repositoryInformation.getActions().put("someRepositoryAction", Mockito.mock(RepositoryAction.class));
		repositoryInformation.getActions().put("someResourceAction", Mockito.mock(RepositoryAction.class));
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
		assertThat(jsonPath).isEqualTo(new ResourcePath("tasks"));
		assertThat(jsonPath.isCollection()).isTrue();
	}

	@Test
	public void onFlatResourceInstancePathShouldReturnFlatPath() {
		// GIVEN
		String path = "/tasks/1";

		// WHEN
		JsonPath jsonPath = pathBuilder.build(path);

		// THEN
		assertThat(jsonPath).isEqualTo(new ResourcePath("tasks", new PathIds("1")));
		assertThat(jsonPath.isCollection()).isFalse();
	}

	@Test
	public void onRepositoryActionShouldActionPath() {
		// GIVEN
		String path = "/tasks/someRepositoryAction";

		// WHEN
		JsonPath jsonPath = pathBuilder.build(path);

		// THEN
		JsonPath expectedPath = new ActionPath("someRepositoryAction");
		expectedPath.setParentResource(new ResourcePath("tasks"));
		assertThat(jsonPath).isEqualTo(expectedPath);
	}

	@Test
	public void onResourceActionShouldActionPath() {
		// GIVEN
		String path = "/tasks/123/someResourceAction";

		// WHEN
		JsonPath jsonPath = pathBuilder.build(path);

		// THEN
		JsonPath expectedPath = new ActionPath("someResourceAction");
		expectedPath.setParentResource(new ResourcePath("tasks", new PathIds("123")));
		assertThat(jsonPath).isEqualTo(expectedPath);
	}


	@Test
	public void onNestedResourcePathShouldReturnNestedPath() {
		// GIVEN
		String path = "/tasks/1/project";

		// WHEN
		JsonPath jsonPath = pathBuilder.build(path);

		// THEN
		JsonPath expectedPath = new FieldPath("project");
		expectedPath.setParentResource(new ResourcePath("tasks", new PathIds("1")));
		assertThat(jsonPath).isEqualTo(expectedPath);
	}

	@Test
	public void onNestedResourceInstancePathShouldThrowException() {
		// GIVEN
		String path = "/tasks/1/project/2";

		// THEN
		expectedException.expect(ResourceException.class);
		expectedException.expectMessage("RelationshipsPath and FieldPath cannot contain ids");

		// WHEN
		pathBuilder.build(path);
	}

	@Test
	public void onNestedResourceRelationshipPathShouldReturnNestedPath() {
		// GIVEN
		String path = "/tasks/1/relationships/project/";

		// WHEN
		JsonPath jsonPath = pathBuilder.build(path);

		// THEN
		JsonPath expectedPath = new RelationshipsPath("project");
		expectedPath.setParentResource(new ResourcePath("tasks", new PathIds("1")));

		assertThat(jsonPath).isEqualTo(expectedPath);
	}

	@Test
	public void onNonRelationshipFieldShouldThrowException() {
		// GIVEN
		String path = "/tasks/1/relationships/name/";

		// THEN
		expectedException.expect(ResourceFieldNotFoundException.class);
		expectedException.expectMessage("name");

		// WHEN
		pathBuilder.build(path);
	}

	@Test
	public void onRelationshipFieldInRelationshipsShouldThrowException() {
		// GIVEN
		String path = "/users/1/relationships/projects";

		// THEN
		expectedException.expect(ResourceFieldNotFoundException.class);
		expectedException.expectMessage("projects");

		// WHEN
		pathBuilder.build(path);
	}

	@Test
	public void onNestedWrongResourceRelationshipPathShouldThrowException() {
		// GIVEN
		String path = "/tasks/1/relationships/";

		// THEN
		expectedException.expect(ResourceFieldNotFoundException.class);

		// WHEN
		pathBuilder.build(path);
	}

	@Test
	public void onRelationshipsPathWithIdShouldThrowException() {
		// GIVEN
		String path = "/tasks/1/relationships/project/1";

		// THEN
		expectedException.expect(ResourceException.class);
		expectedException.expectMessage("RelationshipsPath and FieldPath cannot contain ids");

		// WHEN
		pathBuilder.build(path);
	}

	@Test
	public void onNonExistingFieldShouldThrowException() {
		// GIVEN
		String path = "/tasks/1/nonExistingField/";

		// THEN
		expectedException.expect(ResourceFieldNotFoundException.class);
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
		Assert.assertTrue(jsonPath.isCollection());
		Assert.assertEquals(jsonPath.getIds().getIds(), Arrays.asList("1", "2"));
	}

	@Test
	public void onUrlEncodedMultipleResourceInstancesPathShouldReturnCollectionPath() {
		// GIVEN
		String path = "/tasks/1%2C2";

		// WHEN
		JsonPath jsonPath = pathBuilder.build(path);

		// THEN
		Assert.assertTrue(jsonPath.isCollection());
		Assert.assertEquals(jsonPath.getIds().getIds(), Arrays.asList("1", "2"));
	}

	@Test
	public void onSimpleResourcePathShouldReturnCorrectStringPath() {
		// GIVEN
		JsonPath jsonPath = new ResourcePath("tasks");

		// WHEN
		String result = PathBuilder.build(jsonPath);

		// THEN
		assertThat(result).isEqualTo("/tasks/");
	}

	@Test
	public void onResourcePathWithIdsShouldReturnCorrectStringPath() {
		// GIVEN
		JsonPath jsonPath = new ResourcePath("tasks", new PathIds(Arrays.asList("1", "2")));

		// WHEN
		String result = PathBuilder.build(jsonPath);

		// THEN
		assertThat(result).isEqualTo("/tasks/1,2/");
	}

	@Test
	public void onResourcePathWithIdsAndRelationshipsPathShouldReturnCorrectStringPath() {
		// GIVEN
		JsonPath parentJsonPath = new ResourcePath("tasks", new PathIds(Collections.singletonList("1")));
		JsonPath jsonPath = new RelationshipsPath("project");
		jsonPath.setParentResource(parentJsonPath);

		// WHEN
		String result = PathBuilder.build(jsonPath);

		// THEN
		assertThat(result).isEqualTo("/tasks/1/relationships/project/");
	}

	@Test
	public void onResourcePathWithIdsAndFieldPathShouldReturnCorrectStringPath() {
		// GIVEN
		JsonPath parentJsonPath = new ResourcePath("tasks", new PathIds(Collections.singletonList("1")));
		JsonPath jsonPath = new FieldPath("project");
		jsonPath.setParentResource(parentJsonPath);

		// WHEN
		String result = PathBuilder.build(jsonPath);

		// THEN
		assertThat(result).isEqualTo("/tasks/1/project/");
	}

	@Test
	public void onFieldNameAsSameAsResourceShouldBuildCorrectPath() {
		// GIVEN
		String path = "/tasks/1/projects";

		// WHEN
		JsonPath jsonPath = pathBuilder.build(path);

		// THEN
		JsonPath expectedPath = new FieldPath("projects");
		expectedPath.setParentResource(new ResourcePath("tasks", new PathIds("1")));
		assertThat(jsonPath).isEqualTo(expectedPath);
	}
}
