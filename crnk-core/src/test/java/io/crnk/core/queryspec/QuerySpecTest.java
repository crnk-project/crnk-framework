package io.crnk.core.queryspec;

import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class QuerySpecTest {

	@Test
	public void testEqualContract() throws NoSuchFieldException {
		EqualsVerifier.forClass(QuerySpec.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}


	@Test
	public void checkToString() {
		QuerySpec	spec = new QuerySpec("projects");
		Assert.assertEquals("QuerySpec{resourceType=projects}", spec.toString());

		 spec = new QuerySpec(Project.class);
		Assert.assertEquals("QuerySpec{resourceClass=io.crnk.core.mock.models.Project}", spec.toString());

		spec.addFilter(new FilterSpec(Arrays.asList("filterAttr"), FilterOperator.EQ, "test"));
		Assert.assertEquals("QuerySpec{resourceClass=io.crnk.core.mock.models.Project, filters=[filterAttr EQ test]}", spec.toString());

		spec.addSort(new SortSpec(Arrays.asList("sortAttr"), Direction.ASC));
		Assert.assertEquals("QuerySpec{resourceClass=io.crnk.core.mock.models.Project, filters=[filterAttr EQ test], sort=[sortAttr ASC]}", spec.toString());

		spec.includeField(Arrays.asList("includedField"));
		Assert.assertEquals("QuerySpec{resourceClass=io.crnk.core.mock.models.Project, filters=[filterAttr EQ test], sort=[sortAttr ASC], includedFields=[includedField]}", spec.toString());

		spec.includeRelation(Arrays.asList("includedRelation"));
		Assert.assertEquals("QuerySpec{resourceClass=io.crnk.core.mock.models.Project, filters=[filterAttr EQ test], sort=[sortAttr ASC], includedFields=[includedField], includedRelations=[includedRelation]}", spec.toString());

		spec.setOffset(12);
		spec.setLimit(13L);
		Assert.assertEquals("QuerySpec{resourceClass=io.crnk.core.mock.models.Project, limit=13, offset=12, filters=[filterAttr EQ test], sort=[sortAttr ASC], includedFields=[includedField], includedRelations=[includedRelation]}", spec.toString());
	}

	@Test
	public void testBasic() {
		QuerySpec spec = new QuerySpec(Project.class);
		spec.addFilter(new FilterSpec(Arrays.asList("filterAttr"), FilterOperator.EQ, "test"));
		spec.addSort(new SortSpec(Arrays.asList("sortAttr"), Direction.ASC));
		spec.includeField(Arrays.asList("includedField"));
		spec.includeRelation(Arrays.asList("includedRelation"));

		Assert.assertEquals(1, spec.getFilters().size());
		Assert.assertEquals(1, spec.getSort().size());
		Assert.assertEquals(1, spec.getIncludedFields().size());
		Assert.assertEquals(1, spec.getIncludedRelations().size());
		spec.setFilters(new ArrayList<FilterSpec>());
		spec.setIncludedRelations(new ArrayList<IncludeRelationSpec>());
		spec.setIncludedFields(new ArrayList<IncludeFieldSpec>());
		spec.setSort(new ArrayList<SortSpec>());
		Assert.assertEquals(0, spec.getFilters().size());
		Assert.assertEquals(0, spec.getSort().size());
		Assert.assertEquals(0, spec.getIncludedFields().size());
		Assert.assertEquals(0, spec.getIncludedRelations().size());

		Assert.assertEquals(0, spec.getRelatedSpecs().size());
		QuerySpec relatedSpec = new QuerySpec(Task.class);
		spec.putRelatedSpec(Task.class, relatedSpec);
		Assert.assertSame(relatedSpec, spec.getQuerySpec(Task.class));
		Assert.assertEquals(1, spec.getRelatedSpecs().size());
		spec.setRelatedSpecs(new HashMap<Class<?>, QuerySpec>());
	}

	@Test
	public void testDuplicate() {
		QuerySpec spec = new QuerySpec(Project.class);
		spec.addFilter(new FilterSpec(Arrays.asList("filterAttr"), FilterOperator.EQ, "test"));
		spec.addSort(new SortSpec(Arrays.asList("sortAttr"), Direction.ASC));
		spec.includeField(Arrays.asList("includedField"));
		spec.includeRelation(Arrays.asList("includedRelation"));
		spec.setLimit(2L);
		spec.setOffset(1L);

		QuerySpec duplicate = spec.duplicate();
		Assert.assertNotSame(spec, duplicate);
		Assert.assertEquals(spec, duplicate);
	}

	@Test
	public void testDuplicateWithRelations() {
		QuerySpec spec = new QuerySpec(Project.class);
		QuerySpec relatedSpec = new QuerySpec(Task.class);
		spec.putRelatedSpec(Task.class, relatedSpec);

		QuerySpec duplicate = spec.duplicate();
		Assert.assertNotSame(spec, duplicate);
		Assert.assertEquals(spec, duplicate);
		Assert.assertNotSame(spec.getQuerySpec(Task.class), duplicate.getQuerySpec(Task.class));
		Assert.assertEquals(spec.getQuerySpec(Task.class), duplicate.getQuerySpec(Task.class));
	}

	@Test
	public void setNestedSpecWithClass() {
		QuerySpec spec = new QuerySpec(Project.class);
		QuerySpec relatedSpec = new QuerySpec(Task.class);
		spec.setNestedSpecs(Arrays.asList(relatedSpec));
		Assert.assertSame(relatedSpec, spec.getQuerySpec(Task.class));
	}

	@Test
	public void setNestedSpecWithResourceType() {
		QuerySpec spec = new QuerySpec("projects");
		QuerySpec relatedSpec = new QuerySpec("tasks");
		spec.setNestedSpecs(Arrays.asList(relatedSpec));
		Assert.assertSame(spec, spec.getQuerySpec("projects"));
		Assert.assertSame(relatedSpec, spec.getQuerySpec("tasks"));
		Assert.assertSame(relatedSpec, spec.getOrCreateQuerySpec("tasks"));
		Assert.assertNotSame(relatedSpec, spec.getOrCreateQuerySpec("schedules"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void putRelatedSpecShouldFailIfClassMatchesRoot() {
		QuerySpec spec = new QuerySpec(Project.class);
		QuerySpec relatedSpec = new QuerySpec(Task.class);
		spec.putRelatedSpec(Project.class, relatedSpec);
	}

	@Test
	public void testEquals() {
		QuerySpec spec1 = new QuerySpec(Task.class);
		spec1.addFilter(new FilterSpec(Arrays.asList("filterAttr"), FilterOperator.EQ, "test"));
		spec1.addSort(new SortSpec(Arrays.asList("sortAttr"), Direction.ASC));
		spec1.includeField(Arrays.asList("includedField"));
		spec1.includeRelation(Arrays.asList("includedRelation"));
		Assert.assertEquals(spec1, spec1);

		QuerySpec spec2 = new QuerySpec(Task.class);
		spec2.addFilter(new FilterSpec(Arrays.asList("filterAttr"), FilterOperator.EQ, "test"));
		spec2.addSort(new SortSpec(Arrays.asList("sortAttr"), Direction.ASC));
		spec2.includeField(Arrays.asList("includedField"));
		spec2.includeRelation(Arrays.asList("includedRelation"));
		Assert.assertEquals(spec2, spec2);
		Assert.assertEquals(spec1, spec2);

		spec2.getIncludedRelations().clear();
		Assert.assertNotEquals(spec1, spec2);
		spec2.includeRelation(Arrays.asList("includedRelation"));
		Assert.assertEquals(spec1, spec2);

		spec2.getIncludedFields().clear();
		Assert.assertNotEquals(spec1, spec2);
		Assert.assertNotEquals(spec1.hashCode(), spec2.hashCode());
		spec2.includeField(Arrays.asList("includedField"));
		Assert.assertEquals(spec1, spec2);

		spec2.getFilters().clear();
		Assert.assertNotEquals(spec1, spec2);
		spec2.addFilter(new FilterSpec(Arrays.asList("filterAttr"), FilterOperator.EQ, "test"));
		Assert.assertEquals(spec1, spec2);

		spec2.getSort().clear();
		Assert.assertNotEquals(spec1, spec2);
		spec2.addSort(new SortSpec(Arrays.asList("sortAttr"), Direction.ASC));
		Assert.assertEquals(spec1, spec2);

		spec2.setOffset(2);
		Assert.assertNotEquals(spec1, spec2);
		spec2.setOffset(0);
		Assert.assertEquals(spec1, spec2);

		spec2.setLimit(2L);
		Assert.assertNotEquals(spec1, spec2);
		spec2.setLimit(null);
		Assert.assertEquals(spec1, spec2);

		Assert.assertNotEquals(spec1, "someOtherType");
	}

}
