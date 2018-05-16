package io.crnk.core.engine.internal.dispatcher.path;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class FieldPathTest {

	@Test
	public void onParentWithNoIdsShouldReturnInformationAboutResource() throws Exception {
		// GIVEN
		JsonPath sut = new FieldPath("field");

		String parentName = "document";
		JsonPath parent = new ResourcePath(parentName);
		sut.setParentResource(parent);

		// WHEN
		boolean isCollection = sut.isCollection();
		String testParentName = sut.getResourcePath();

		// THEN
		assertThat(isCollection).isTrue();
		assertThat(testParentName).isEqualTo(parentName);
	}

	@Test
	public void onParentWithOneIdShouldReturnInformationAboutResource() throws Exception {
		// GIVEN
		JsonPath sut = new FieldPath("field");

		String parentName = "document";
		JsonPath parent = new ResourcePath(parentName, new PathIds(Collections.singletonList("1")));
		sut.setParentResource(parent);

		// WHEN
		boolean isCollection = sut.isCollection();
		String testParentName = sut.getResourcePath();

		// THEN
		assertThat(isCollection).isFalse();
		assertThat(testParentName).isEqualTo(parentName);
	}

	@Test
	public void onParentWithManyIdsShouldReturnInformationAboutResource() throws Exception {
		// GIVEN
		JsonPath sut = new FieldPath("field");

		String parentName = "document";
		JsonPath parent = new ResourcePath(parentName, new PathIds(Arrays.asList("1", "2")));
		sut.setParentResource(parent);

		// WHEN
		boolean isCollection = sut.isCollection();
		String testParentName = sut.getResourcePath();

		// THEN
		assertThat(isCollection).isTrue();
		assertThat(testParentName).isEqualTo(parentName);
	}
}
