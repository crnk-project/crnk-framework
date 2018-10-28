package io.crnk.core.queryspec;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class FilterOperatorTest {


	@Test(expected = UnsupportedOperationException.class)
	public void andMatchNotSupported() {
		FilterOperator.AND.matches(null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void notMatchNotSupported() {
		FilterOperator.NOT.matches(null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void orMatchNotSupported() {
		FilterOperator.OR.matches(null, null);
	}

	@Test
	public void testLEOperator() {
		Assert.assertTrue(FilterOperator.LE.matches("a", "b"));
	}

	@Test
	public void testSerialization() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writerFor(FilterOperator.class).writeValueAsString(FilterOperator.EQ);
		Assert.assertEquals("\"EQ\"", json);

		FilterOperator operator = objectMapper.readerFor(FilterOperator.class).readValue(json);
		Assert.assertEquals(FilterOperator.EQ, operator);
	}

	@Test
	public void testLikeOperator() {
		Assert.assertTrue(FilterOperator.LIKE.matches("test", "te%"));
		Assert.assertTrue(FilterOperator.LIKE.matches("test", "Te%"));
		Assert.assertTrue(FilterOperator.LIKE.matches("test", "tE%"));
		Assert.assertFalse(FilterOperator.LIKE.matches("test", "aE%"));
		Assert.assertTrue(FilterOperator.LIKE.matches("test", "t%t"));
		Assert.assertTrue(FilterOperator.LIKE.matches("test.", "t%."));
		Assert.assertFalse(FilterOperator.LIKE.matches(".", "t"));
		Assert.assertTrue(FilterOperator.LIKE.matches(".", "."));

		Assert.assertFalse(FilterOperator.LIKE.matches(".", "t"));
		Assert.assertFalse(FilterOperator.LIKE.matches(".", "t"));
		Assert.assertFalse(FilterOperator.LIKE.matches("[", "t"));
		Assert.assertFalse(FilterOperator.LIKE.matches("\\", "t"));
		Assert.assertFalse(FilterOperator.LIKE.matches("^", "t"));
		Assert.assertFalse(FilterOperator.LIKE.matches("$", "t"));
		Assert.assertFalse(FilterOperator.LIKE.matches("|", "t"));
		Assert.assertFalse(FilterOperator.LIKE.matches("?", "t"));
		Assert.assertFalse(FilterOperator.LIKE.matches(")", "t"));
		Assert.assertFalse(FilterOperator.LIKE.matches("(", "t"));
		Assert.assertFalse(FilterOperator.LIKE.matches("+", "t"));
		Assert.assertFalse(FilterOperator.LIKE.matches("*", "t"));

		Assert.assertTrue(FilterOperator.LIKE.matches(".", "."));
		Assert.assertTrue(FilterOperator.LIKE.matches("[", "["));
		Assert.assertTrue(FilterOperator.LIKE.matches("\\", "\\"));
		Assert.assertTrue(FilterOperator.LIKE.matches("^", "^"));
		Assert.assertTrue(FilterOperator.LIKE.matches("$", "$"));
		Assert.assertTrue(FilterOperator.LIKE.matches("|", "|"));
		Assert.assertTrue(FilterOperator.LIKE.matches("?", "?"));
		Assert.assertTrue(FilterOperator.LIKE.matches(")", ")"));
		Assert.assertTrue(FilterOperator.LIKE.matches("(", "("));
		Assert.assertTrue(FilterOperator.LIKE.matches("+", "+"));
		Assert.assertTrue(FilterOperator.LIKE.matches("*", "*"));
		Assert.assertFalse(FilterOperator.LIKE.matches("*", null));
	}

	@Test
	public void testLikeOperatorUsesStringType() {
		Assert.assertEquals(String.class, FilterOperator.LIKE.getFilterType(null, Integer.class));
	}


	@Test
	public void testDefaultOperatorsUsesSameType() {
		Assert.assertEquals(Integer.class, FilterOperator.EQ.getFilterType(null, Integer.class));
		Assert.assertEquals(Integer.class, FilterOperator.GT.getFilterType(null, Integer.class));
		Assert.assertEquals(Integer.class, FilterOperator.GE.getFilterType(null, Integer.class));
		Assert.assertEquals(Boolean.class, FilterOperator.LT.getFilterType(null, Boolean.class));
		Assert.assertEquals(Long.class, FilterOperator.LE.getFilterType(null, Long.class));
	}

	@Test
	public void testEquals() {
		Assert.assertEquals(FilterOperator.AND, FilterOperator.AND);
		Assert.assertNotEquals(FilterOperator.AND, "notAnOperator");
		Assert.assertNotEquals(FilterOperator.AND, null);
		Assert.assertEquals(FilterOperator.OR, FilterOperator.OR);
		Assert.assertEquals(FilterOperator.OR, new FilterOperator("OR") {

			@Override
			public boolean matches(Object value1, Object value2) {
				return false;
			}
		});
		Assert.assertNotEquals(FilterOperator.AND, FilterOperator.OR);
	}
}
