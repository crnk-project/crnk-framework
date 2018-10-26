package io.crnk.core.queryspec;

import java.util.Arrays;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Assert;
import org.junit.Test;

public class SortSpecTest {

	@Test
	public void testBasic() {
		SortSpec spec = new SortSpec(Arrays.asList("name"), Direction.ASC);
		Assert.assertEquals(Direction.ASC, spec.getDirection());
		Assert.assertEquals(Arrays.asList("name"), spec.getAttributePath());
	}

	@Test(expected = IllegalStateException.class)
	public void testThrowExceptionOnNullPathArgument() {
		new SortSpec((PathSpec) null, Direction.ASC);
	}

	@Test(expected = IllegalStateException.class)
	public void testThrowExceptionOnNullDirArgument() {
		new SortSpec(Arrays.asList("test"), null);
	}


	@Test
	public void fromPathSpec() {
		SortSpec sort = PathSpec.of("a", "b").sort(Direction.ASC);
		Assert.assertEquals(Direction.ASC, sort.getDirection());
		Assert.assertEquals("a.b", sort.getPath().toString());
	}

	@Test
	public void testToString() {
		Assert.assertEquals("name ASC", new SortSpec(Arrays.asList("name"), Direction.ASC).toString());
		Assert.assertEquals("name1.name2 ASC", new SortSpec(Arrays.asList("name1", "name2"), Direction.ASC).toString());
		Assert.assertEquals("name DESC", new SortSpec(Arrays.asList("name"), Direction.DESC).toString());
	}

	@Test
	public void testReverse() {
		SortSpec specAsc = new SortSpec(Arrays.asList("name1"), Direction.ASC);
		SortSpec specDesc = new SortSpec(Arrays.asList("name1"), Direction.DESC);
		Assert.assertEquals(specDesc, specAsc.reverse());
		Assert.assertEquals(specAsc, specDesc.reverse());
	}

	@Test
	public void testEquals() {
		EqualsVerifier.forClass(SortSpec.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();

		SortSpec spec1 = new SortSpec(Arrays.asList("name1"), Direction.ASC);
		SortSpec spec2 = new SortSpec(Arrays.asList("name1"), Direction.ASC);
		SortSpec spec3 = new SortSpec(Arrays.asList("name2"), Direction.ASC);
		SortSpec spec4 = new SortSpec(Arrays.asList("name1"), Direction.DESC);

		Assert.assertEquals(spec1, spec1);
		Assert.assertEquals(spec3, spec3);
		Assert.assertEquals(spec1, spec2);
		Assert.assertEquals(spec2, spec1);
		Assert.assertEquals(spec1.hashCode(), spec1.hashCode());
		Assert.assertEquals(spec3.hashCode(), spec3.hashCode());
		Assert.assertEquals(spec1.hashCode(), spec2.hashCode());
		Assert.assertNotEquals(spec2, spec3);
		Assert.assertNotEquals(spec3, spec2);
		Assert.assertNotEquals(spec1, spec4);
		Assert.assertNotEquals(spec3, spec4);

		Assert.assertEquals(spec1, SortSpec.asc(Arrays.asList("name1")));
		Assert.assertEquals(spec4, SortSpec.desc(Arrays.asList("name1")));
		Assert.assertNotEquals(spec1, null);
		Assert.assertNotEquals(spec1, "test");
	}

	@Test
	public void testClone() {
		SortSpec sortSpec = new SortSpec(Arrays.asList("sortAttr"), Direction.ASC);
		SortSpec duplicate = sortSpec.clone();
		Assert.assertNotSame(sortSpec, duplicate);
		Assert.assertNotSame(sortSpec.getAttributePath(), duplicate.getAttributePath());
		Assert.assertSame(sortSpec.getDirection(), duplicate.getDirection());
	}
}
