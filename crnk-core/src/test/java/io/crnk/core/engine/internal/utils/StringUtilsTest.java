package io.crnk.core.engine.internal.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class StringUtilsTest {

	@Test
	public void onSingleElementShouldReturnTheSameValue() throws Exception {
		// GIVEN
		String string = "hello world";
		List<String> values = Collections.singletonList(string);

		// WHEN
		String result = StringUtils.join(",", values);

		// THEN
		assertThat(result).isEqualTo(string);
	}

	@Test
	public void onTwoElementsShouldReturnJoinedValues() throws Exception {
		// GIVEN
		List<String> values = Arrays.asList("hello", "world");

		// WHEN
		String result = StringUtils.join(" ", values);

		// THEN
		assertThat(result).isEqualTo("hello world");
	}

	@Test
	public void onIsBlankValues() throws Exception {
		assertTrue(StringUtils.isBlank(null));
		assertTrue(StringUtils.isBlank(""));
		assertTrue(StringUtils.isBlank(" "));
		assertFalse(StringUtils.isBlank("crnk"));
		assertFalse(StringUtils.isBlank("  crnk  "));
	}


}
