package io.crnk.core.queryspec;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class FilterSpecTest {

	@Test
	public void testBasic() {
		FilterSpec spec = new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "test");
		Assert.assertEquals("test", spec.getValue());
		Assert.assertEquals(FilterOperator.EQ, spec.getOperator());
		Assert.assertEquals(Arrays.asList("name"), spec.getAttributePath());
		Assert.assertFalse(spec.hasExpressions());
		spec.setValue("newValue");
		Assert.assertEquals("newValue", spec.getValue());
	}

	@Test
	public void fromPathSpec() {
		FilterSpec filter = PathSpec.of("a", "b").filter(FilterOperator.EQ, "12");
		Assert.assertEquals("12", filter.getValue());
		Assert.assertEquals(FilterOperator.EQ, filter.getOperator());
		Assert.assertEquals("a.b", filter.getPath().toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullOperatorThrowsException() {
		new FilterSpec(Arrays.asList("name"), null, "test");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAndOperatorWithValueThrowsException() {
		new FilterSpec(Arrays.asList("name"), FilterOperator.AND, "test");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOrOperatorWithValueThrowsException() {
		new FilterSpec(Arrays.asList("name"), FilterOperator.OR, "test");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNotOperatorWithValueThrowsException() {
		new FilterSpec(Arrays.asList("name"), FilterOperator.NOT, "test");
	}

	@Test
	public void testCloneBasic() {
		FilterSpec spec = new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "test");
		Assert.assertEquals(spec, spec.clone());
	}

	@Test
	public void testCloneExpressions() {
		FilterSpec spec1 = new FilterSpec(Arrays.asList("name1"), FilterOperator.EQ, "test");
		FilterSpec spec2 = new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test");
		FilterSpec andSpec = FilterSpec.and(spec1, spec2);
		Assert.assertEquals(andSpec, andSpec.clone());
	}

	@Test
	public void testToString() {
		Assert.assertEquals("name EQ test",
				new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "test").toString());
		Assert.assertEquals("name1.name2 EQ test",
				new FilterSpec(Arrays.asList("name1", "name2"), FilterOperator.EQ, "test").toString());
		Assert.assertEquals("name NEQ test",
				new FilterSpec(Arrays.asList("name"), FilterOperator.NEQ, "test").toString());
		Assert.assertEquals("NOT(name NEQ test)",
				FilterSpec.not(new FilterSpec(Arrays.asList("name"), FilterOperator.NEQ, "test")).toString());
		Assert.assertEquals("(name1 NEQ test1) AND (name2 EQ test2)",
				FilterSpec.and(new FilterSpec(Arrays.asList("name1"), FilterOperator.NEQ, "test1"),
						new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test2")).toString());
		Assert.assertEquals("NOT((name1 NEQ test1) AND (name2 EQ test2))",
				new FilterSpec(FilterOperator.NOT,
						Arrays.asList(new FilterSpec(Arrays.asList("name1"), FilterOperator.NEQ, "test1"),
								new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test2"))).toString());

		Assert.assertEquals("NOT(name2 EQ test2)", new FilterSpec(FilterOperator.NOT,
				Arrays.asList(new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test2"))).toString());

	}

	@Test
	public void testAndTwoExpr() {
		FilterSpec spec1 = new FilterSpec(Arrays.asList("name1"), FilterOperator.EQ, "test");
		FilterSpec spec2 = new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test");
		FilterSpec andSpec = FilterSpec.and(spec1, spec2);
		Assert.assertTrue(andSpec.hasExpressions());
		Assert.assertEquals(FilterOperator.AND, andSpec.getOperator());
		Assert.assertEquals(2, andSpec.getExpression().size());
	}

	@Test
	public void testAndOneExpr() {
		FilterSpec spec1 = new FilterSpec(Arrays.asList("name1"), FilterOperator.EQ, "test");
		FilterSpec andSpec = FilterSpec.and(spec1);
		Assert.assertSame(spec1, andSpec);
	}

	@Test
	public void testOrTwoExpr() {
		FilterSpec spec1 = new FilterSpec(Arrays.asList("name1"), FilterOperator.EQ, "test");
		FilterSpec spec2 = new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test");
		FilterSpec orSpec = FilterSpec.or(spec1, spec2);
		Assert.assertTrue(orSpec.hasExpressions());
		Assert.assertEquals(FilterOperator.OR, orSpec.getOperator());
		Assert.assertEquals(2, orSpec.getExpression().size());
	}

	@Test
	public void testOrTwoExprList() {
		FilterSpec spec1 = new FilterSpec(Arrays.asList("name1"), FilterOperator.EQ, "test");
		FilterSpec spec2 = new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test");
		FilterSpec orSpec = FilterSpec.or(Arrays.asList(spec1, spec2));
		Assert.assertTrue(orSpec.hasExpressions());
		Assert.assertEquals(FilterOperator.OR, orSpec.getOperator());
		Assert.assertEquals(2, orSpec.getExpression().size());
	}

	@Test
	public void testOrOneExpr() {
		FilterSpec spec1 = new FilterSpec(Arrays.asList("name1"), FilterOperator.EQ, "test");
		FilterSpec orSpec = FilterSpec.or(spec1);
		Assert.assertSame(spec1, orSpec);
	}

	@Test
	public void testEquals() {
		FilterSpec spec1A = new FilterSpec(Arrays.asList("name1"), FilterOperator.EQ, "test");
		FilterSpec spec2A = new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test");
		FilterSpec orSpecA = FilterSpec.or(spec1A, spec2A);

		FilterSpec spec1B = new FilterSpec(Arrays.asList("name1"), FilterOperator.EQ, "test");
		FilterSpec spec2B = new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test");
		FilterSpec orSpecB = FilterSpec.or(spec1B, spec2B);
		FilterSpec notSpec = FilterSpec.not(spec1A);

		Assert.assertEquals(orSpecA, orSpecB);
		Assert.assertEquals(spec1A, spec1A);
		Assert.assertEquals(spec1A, spec1B);
		Assert.assertEquals(spec2A, spec2B);
		Assert.assertEquals(orSpecA.hashCode(), orSpecB.hashCode());
		Assert.assertEquals(spec1A.hashCode(), spec1B.hashCode());
		Assert.assertEquals(spec2A.hashCode(), spec2B.hashCode());
		Assert.assertNotEquals(spec1A, spec2B);
		Assert.assertNotEquals(spec1A, "somethingDifferent");
		Assert.assertNotEquals(spec1A, null);
		Assert.assertNotEquals(spec2A, spec1B);
		Assert.assertNotEquals(orSpecA, spec1B);
		Assert.assertNotEquals(spec2B, orSpecA);
		Assert.assertNotEquals(spec2B, notSpec);
		Assert.assertEquals(notSpec, notSpec);
		Assert.assertEquals(orSpecB, orSpecB);
		Assert.assertNotEquals(notSpec, orSpecB);
	}

	@Test
	public void testNormalize() {
		FilterSpec spec1A = new FilterSpec(Arrays.asList("name1"), FilterOperator.EQ, "test");
		FilterSpec spec2A = new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test");
		FilterSpec orSpecA = FilterSpec.or(spec1A, spec2A);

		FilterSpec spec1B = new FilterSpec(Arrays.asList("name2"), FilterOperator.EQ, "test");
		FilterSpec spec2B = new FilterSpec(Arrays.asList("name1"), FilterOperator.EQ, "test");
		FilterSpec orSpecB = FilterSpec.or(spec1B, spec2B);
		Assert.assertNotEquals(orSpecA, orSpecB);

		// A does not change since sorted alphabetically
		Assert.assertEquals(orSpecA, orSpecA.normalize());

		// norm B equals A
		FilterSpec norm = orSpecB.normalize();
		Assert.assertEquals(orSpecA, norm);
	}

}
