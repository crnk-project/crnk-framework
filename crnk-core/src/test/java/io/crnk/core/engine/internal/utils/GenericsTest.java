package io.crnk.core.engine.internal.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GenericsTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void onGenericClassShouldReturnFirstParameter() throws Exception {
		// WHEN
		Class<?> clazz = Generics
				.getResourceClass(SampleGenericClass.class.getDeclaredField("strings").getGenericType(), List.class);

		// THEN
		assertThat(clazz).isEqualTo(String.class);
	}

	@Test
	public void onGenericWildcardClassShouldThrowException() throws Exception {
		// THEN
		expectedException.expect(RuntimeException.class);

		// WHEN
		Generics.getResourceClass(SampleGenericClass.class.getDeclaredField("stringsWildcard").getGenericType(),
				List.class);
	}

	private static class SampleGenericClass {
		private List<String> strings;
		private List<? extends String> stringsWildcard;
	}
}
