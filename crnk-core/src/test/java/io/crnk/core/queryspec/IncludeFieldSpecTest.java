package io.crnk.core.queryspec;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class IncludeFieldSpecTest {

	@Test
	public void testBasic() {
		IncludeFieldSpec spec = new IncludeFieldSpec(Arrays.asList("name"));
		Assert.assertEquals(Arrays.asList("name"), spec.getAttributePath());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testThrowExceptionOnNullArgument() {
		new IncludeFieldSpec((PathSpec)null);
	}

	@Test
	public void testToString() {
		Assert.assertEquals("name", new IncludeFieldSpec(Arrays.asList("name")).toString());
		Assert.assertEquals("name1.name2", new IncludeFieldSpec(Arrays.asList("name1", "name2")).toString());
		Assert.assertEquals("name", new IncludeFieldSpec(Arrays.asList("name")).toString());
	}

	@Test
	public void testEquals() {
		IncludeFieldSpec spec1 = new IncludeFieldSpec(Arrays.asList("name1"));
		IncludeFieldSpec spec2 = new IncludeFieldSpec(Arrays.asList("name1"));
		IncludeFieldSpec spec3 = new IncludeFieldSpec(Arrays.asList("name2"));
		IncludeRelationSpec rel = new IncludeRelationSpec(Arrays.asList("name2"));

		Assert.assertEquals(spec1, spec1);
		Assert.assertEquals(spec3, spec3);
		Assert.assertEquals(spec1, spec2);
		Assert.assertEquals(spec2, spec1);
		Assert.assertEquals(spec1.hashCode(), spec1.hashCode());
		Assert.assertEquals(spec3.hashCode(), spec3.hashCode());
		Assert.assertEquals(spec1.hashCode(), spec2.hashCode());
		Assert.assertNotEquals(spec2, spec3);
		Assert.assertNotEquals(spec3, spec2);
		Assert.assertNotEquals(spec1, rel);
	}
}
