package io.crnk.core.queryspec;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class IncludeRelationSpecTest {

	@Test
	public void testBasic() {
		IncludeRelationSpec spec = new IncludeRelationSpec(Arrays.asList("name"));
		Assert.assertEquals(Arrays.asList("name"), spec.getAttributePath());
	}

	@Test
	public void testToString() {
		Assert.assertEquals("name", new IncludeRelationSpec(Arrays.asList("name")).toString());
		Assert.assertEquals("name1.name2", new IncludeRelationSpec(Arrays.asList("name1", "name2")).toString());
		Assert.assertEquals("name", new IncludeRelationSpec(Arrays.asList("name")).toString());
	}

	@Test
	public void testEquals() {
		IncludeRelationSpec spec1 = new IncludeRelationSpec(Arrays.asList("name1"));
		IncludeRelationSpec spec2 = new IncludeRelationSpec(Arrays.asList("name1"));
		IncludeRelationSpec spec3 = new IncludeRelationSpec(Arrays.asList("name2"));
		IncludeFieldSpec rel = new IncludeFieldSpec(Arrays.asList("name2"));

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

	@Test
	public void testClone() {
		IncludeRelationSpec spec = new IncludeRelationSpec(Arrays.asList("sortAttr"));
		IncludeRelationSpec duplicate = spec.clone();
		Assert.assertNotSame(spec, duplicate);
		Assert.assertNotSame(spec.getAttributePath(), duplicate.getAttributePath());
	}
}
