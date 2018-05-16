package io.crnk.core.engine.internal.dispatcher.path;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourcePathTest {

	@Test
	public void onParentWithNoIdsShouldReturnInformationAboutResource() throws Exception {
		// GIVEN
		String resourceName = "document";
		JsonPath sut = new ResourcePath(resourceName);

		// WHEN
		boolean isCollection = sut.isCollection();
		String testResourceName = sut.getResourcePath();

		// THEN
		assertThat(isCollection).isTrue();
		assertThat(testResourceName).isEqualTo(resourceName);
	}

	@Test
	public void onParentWithOneIdShouldReturnInformationAboutResource() throws Exception {
		// GIVEN
		String resourceName = "document";
		JsonPath sut = new ResourcePath(resourceName, new PathIds(Collections.singletonList("1")));

		// WHEN
		boolean isCollection = sut.isCollection();
		String testResourceName = sut.getResourcePath();

		// THEN
		assertThat(isCollection).isFalse();
		assertThat(testResourceName).isEqualTo(resourceName);
	}

	@Test
	public void onParentWithManyIdsShouldReturnInformationAboutResource() throws Exception {
		// GIVEN
		String resourceName = "document";
		JsonPath sut = new ResourcePath(resourceName, new PathIds(Arrays.asList("1", "2")));

		// WHEN
		boolean isCollection = sut.isCollection();
		String testResourceName = sut.getResourcePath();

		// THEN
		assertThat(isCollection).isTrue();
		assertThat(testResourceName).isEqualTo(resourceName);
	}
}
