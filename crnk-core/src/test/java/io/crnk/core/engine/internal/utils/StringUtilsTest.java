package io.crnk.core.engine.internal.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class StringUtilsTest {

	@Test
	public void onSingleElementShouldReturnTheSameValue() {
		// GIVEN
		String string = "hello world";
		List<String> values = Collections.singletonList(string);

		// WHEN
		String result = StringUtils.join(",", values);

		// THEN
		assertThat(result).isEqualTo(string);
	}

	@Test
	public void onTwoElementsShouldReturnJoinedValues() {
		// GIVEN
		List<String> values = Arrays.asList("hello", "world");

		// WHEN
		String result = StringUtils.join(" ", values);

		// THEN
		assertThat(result).isEqualTo("hello world");
	}

	@Test
	public void onIsBlankValues() {
		assertTrue(StringUtils.isBlank(null));
		assertTrue(StringUtils.isBlank(""));
		assertTrue(StringUtils.isBlank(" "));
		assertFalse(StringUtils.isBlank("crnk"));
		assertFalse(StringUtils.isBlank("  crnk  "));
	}

	@Test
	public void onJoinOfNulls() {
		Assert.assertEquals("null,null", StringUtils.join(",", Arrays.asList(null, null)));
	}

	@Test
	public void checkDecapitalize() {
		Assert.assertEquals("", StringUtils.decapitalize(""));
		Assert.assertEquals("test", StringUtils.decapitalize("Test"));
		Assert.assertEquals("someTest", StringUtils.decapitalize("SomeTest"));
	}


}
