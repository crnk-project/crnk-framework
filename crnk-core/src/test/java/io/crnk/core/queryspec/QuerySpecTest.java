package io.crnk.core.queryspec;

import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import io.crnk.core.engine.document.Resource;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.core.queryspec.pagingspec.PagingSpec;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class QuerySpecTest {

	@Test
	public void testEqualContract() throws NoSuchFieldException {
		EqualsVerifier.forClass(QuerySpec.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

	@Test
	public void testGetOrCreate() throws NoSuchFieldException {
		QuerySpec querySpec = new QuerySpec(Task.class, "tasks");
		Assert.assertSame(querySpec, querySpec.getOrCreateQuerySpec(Task.class));
		Assert.assertSame(querySpec, querySpec.getOrCreateQuerySpec("tasks"));

		querySpec = new QuerySpec(Task.class, null);
		Assert.assertSame(querySpec, querySpec.getOrCreateQuerySpec(Task.class));
		Assert.assertNotSame(querySpec, querySpec.getOrCreateQuerySpec(Project.class));

		querySpec = new QuerySpec(null, "tasks");
		Assert.assertSame(querySpec, querySpec.getOrCreateQuerySpec("tasks"));
		Assert.assertNotSame(querySpec, querySpec.getOrCreateQuerySpec("other"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCannotCreateResourceInstance() {
		new QuerySpec(Resource.class);
	}


	@Test(expected = IllegalArgumentException.class)
	public void testCannotGetAndCreateWithResourceClass() {
		new QuerySpec(Task.class).getOrCreateQuerySpec(Resource.class);
	}

	@Test
	public void testResourceClassIgnored() {
		QuerySpec querySpec = new QuerySpec(Resource.class, "tasks");
		assertNull(querySpec.getResourceClass());
	}


	@Test
	public void checkToString() {
		QuerySpec spec = new QuerySpec("projects");
		Assert.assertEquals("QuerySpec[resourceType=projects, paging=OffsetLimitPagingSpec[offset=0]]", spec.toString());

		spec = new QuerySpec(Project.class);
		Assert.assertEquals("QuerySpec[resourceClass=io.crnk.core.mock.models.Project, paging=OffsetLimitPagingSpec[offset=0]]",
				spec.toString());

		spec.addFilter(new FilterSpec(Arrays.asList("filterAttr"), FilterOperator.EQ, "test"));
		Assert.assertEquals(
				"QuerySpec[resourceClass=io.crnk.core.mock.models.Project, paging=OffsetLimitPagingSpec[offset=0], "
						+ "filters=[filterAttr EQ test]]",
				spec.toString());

		spec.addSort(new SortSpec(Arrays.asList("sortAttr"), Direction.ASC));
		Assert.assertEquals(
				"QuerySpec[resourceClass=io.crnk.core.mock.models.Project, paging=OffsetLimitPagingSpec[offset=0], "
						+ "filters=[filterAttr EQ test], sort=[sortAttr ASC]]",
				spec.toString());

		spec.includeField(Arrays.asList("includedField"));
		Assert.assertEquals(
				"QuerySpec[resourceClass=io.crnk.core.mock.models.Project, paging=OffsetLimitPagingSpec[offset=0], "
						+ "filters=[filterAttr EQ test], sort=[sortAttr ASC], "
						+ "includedFields=[includedField]]",
				spec.toString());

		spec.includeRelation(Arrays.asList("includedRelation"));
		Assert.assertEquals(
				"QuerySpec[resourceClass=io.crnk.core.mock.models.Project, paging=OffsetLimitPagingSpec[offset=0], "
						+ "filters=[filterAttr EQ test], sort=[sortAttr ASC], "
						+ "includedFields=[includedField], includedRelations=[includedRelation]]",
				spec.toString());

		spec.setPagingSpec(new OffsetLimitPagingSpec(12L, 13L));
		Assert.assertEquals(
				"QuerySpec[resourceClass=io.crnk.core.mock.models.Project, paging=OffsetLimitPagingSpec[offset=12, limit=13], "
						+ "filters=[filterAttr EQ test], "
						+ "sort=[sortAttr ASC], includedFields=[includedField], includedRelations=[includedRelation]]",
				spec.toString());
	}

	@Test
	public void testBasic() {
		FilterSpec filterSpec = new FilterSpec(Arrays.asList("filterAttr"), FilterOperator.EQ, "test");
		QuerySpec spec = new QuerySpec(Project.class);
		spec.addFilter(filterSpec);
		spec.addSort(new SortSpec(Arrays.asList("sortAttr"), Direction.ASC));
		spec.includeField(Arrays.asList("includedField"));
		spec.includeRelation(Arrays.asList("includedRelation"));

		Assert.assertEquals(1, spec.getFilters().size());
		Assert.assertEquals(filterSpec, spec.getFilter("filterAttr"));
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

	@Test(expected = BadRequestException.class)
	public void testFilterNotFoundOrThrow() {
		QuerySpec spec = new QuerySpec(Project.class);
		spec.addFilter(new FilterSpec(Arrays.asList("filterAttr"), FilterOperator.EQ, "test"));
		spec.getFilterOrThrow("unknown");
	}

	@Test
	public void testFilterNotFound() {
		QuerySpec spec = new QuerySpec(Project.class);
		spec.addFilter(new FilterSpec(Arrays.asList("filterAttr"), FilterOperator.EQ, "test"));
		FilterSpec filterSpec = spec.getFilter("unknown");
		assertNull(filterSpec);
	}


	@Test
	public void testClone() {
		FilterSpec filterSpec = new FilterSpec(Arrays.asList("filterAttr"), FilterOperator.EQ, "test");
		SortSpec sortSpec = new SortSpec(Arrays.asList("sortAttr"), Direction.ASC);
		QuerySpec spec = new QuerySpec(Project.class);
		spec.addFilter(filterSpec);
		spec.addSort(sortSpec);
		spec.includeField(Arrays.asList("includedField"));
		spec.includeRelation(Arrays.asList("includedRelation"));
		spec.setLimit(2L);
		spec.setOffset(1L);

		QuerySpec duplicate = spec.duplicate();
		Assert.assertNotSame(spec, duplicate);
		Assert.assertNotSame(spec.getFilters().get(0), duplicate.getFilters().get(0));
		Assert.assertNotSame(spec.getSort(), duplicate.getSort());
		Assert.assertNotSame(spec.getSort().get(0), duplicate.getSort().get(0));
		Assert.assertNotSame(spec.getIncludedFields(), duplicate.getIncludedFields());
		Assert.assertNotSame(spec.getIncludedRelations(), duplicate.getIncludedRelations());
		Assert.assertNotSame(spec.getPagingSpec(), duplicate.getPagingSpec());
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

	private static QuerySpec createTestQueySpec() {
		FilterSpec filterSpec = new FilterSpec(Arrays.asList("filterAttr"), FilterOperator.EQ, "test");
		SortSpec sortSpec = new SortSpec(Arrays.asList("sortAttr"), Direction.ASC);
		IncludeFieldSpec fieldSpec = new IncludeFieldSpec(PathSpec.of("includedField"));
		IncludeRelationSpec relationSpec = new IncludeRelationSpec(PathSpec.of("includedRelation"));
		QuerySpec spec = new QuerySpec(Task.class);
		spec.addFilter(filterSpec);
		spec.addSort(sortSpec);
		spec.getIncludedFields().add(fieldSpec);
		spec.getIncludedRelations().add(relationSpec);
		spec.setOffset(1);
		return spec;
	}

	@Test
	public void testVisitor() {
		QuerySpec spec = createTestQueySpec();

		QuerySpecVisitorBase visitor = Mockito.spy(QuerySpecVisitorBase.class);
		spec.accept(visitor);

		Mockito.verify(visitor, Mockito.times(1)).visitStart(Mockito.eq(spec));
		Mockito.verify(visitor, Mockito.times(1)).visitEnd(Mockito.eq(spec));
		Mockito.verify(visitor, Mockito.times(1)).visitField(Mockito.eq(spec.getIncludedFields().get(0)));
		Mockito.verify(visitor, Mockito.times(1)).visitFilterStart(Mockito.eq(spec.getFilters().get(0)));
		Mockito.verify(visitor, Mockito.times(1)).visitFilterEnd(Mockito.eq(spec.getFilters().get(0)));
		Mockito.verify(visitor, Mockito.times(1)).visitInclude(Mockito.eq(spec.getIncludedRelations().get(0)));
		Mockito.verify(visitor, Mockito.times(1)).visitSort(Mockito.eq(spec.getSort().get(0)));
		Mockito.verify(visitor, Mockito.times(1)).visitPaging(Mockito.eq(spec.getPaging()));
		Mockito.verify(visitor, Mockito.times(4)).visitPath(Mockito.any(PathSpec.class));
	}

	@Test
	public void testVisitorWithFilterAbort() {
		QuerySpec spec = createTestQueySpec();

		QuerySpecVisitorBase visitor = Mockito.mock(QuerySpecVisitorBase.class);
		Mockito.when(visitor.visitStart(Mockito.any(QuerySpec.class))).thenReturn(true);
		Mockito.when(visitor.visitFilterStart(Mockito.any(FilterSpec.class))).thenReturn(false);
		Mockito.when(visitor.visitSort(Mockito.any(SortSpec.class))).thenReturn(true);
		Mockito.when(visitor.visitField(Mockito.any(IncludeFieldSpec.class))).thenReturn(true);
		Mockito.when(visitor.visitInclude(Mockito.any(IncludeRelationSpec.class))).thenReturn(true);
		spec.accept(visitor);

		Mockito.verify(visitor, Mockito.times(1)).visitStart(Mockito.eq(spec));
		Mockito.verify(visitor, Mockito.times(1)).visitEnd(Mockito.eq(spec));
		Mockito.verify(visitor, Mockito.times(1)).visitField(Mockito.any(IncludeFieldSpec.class));
		Mockito.verify(visitor, Mockito.times(1)).visitFilterStart(Mockito.any(FilterSpec.class));
		Mockito.verify(visitor, Mockito.times(0)).visitFilterEnd(Mockito.any(FilterSpec.class));
		Mockito.verify(visitor, Mockito.times(1)).visitInclude(Mockito.any(IncludeRelationSpec.class));
		Mockito.verify(visitor, Mockito.times(1)).visitSort(Mockito.any(SortSpec.class));
		Mockito.verify(visitor, Mockito.times(1)).visitPaging(Mockito.any(PagingSpec.class));

		// filter path will not be visited
		Mockito.verify(visitor, Mockito.times(3)).visitPath(Mockito.any(PathSpec.class));
	}

	@Test
	public void testVisitorWithSortAbort() {
		QuerySpec spec = createTestQueySpec();

		QuerySpecVisitorBase visitor = Mockito.mock(QuerySpecVisitorBase.class);
		Mockito.when(visitor.visitStart(Mockito.any(QuerySpec.class))).thenReturn(true);
		Mockito.when(visitor.visitFilterStart(Mockito.any(FilterSpec.class))).thenReturn(true);
		Mockito.when(visitor.visitSort(Mockito.any(SortSpec.class))).thenReturn(false);
		Mockito.when(visitor.visitField(Mockito.any(IncludeFieldSpec.class))).thenReturn(true);
		Mockito.when(visitor.visitInclude(Mockito.any(IncludeRelationSpec.class))).thenReturn(true);
		spec.accept(visitor);

		// sort path will not be visited
		Mockito.verify(visitor, Mockito.times(3)).visitPath(Mockito.any(PathSpec.class));
	}

	@Test
	public void testVisitorWithMultipleAbort() {
		QuerySpec spec = createTestQueySpec();

		QuerySpecVisitorBase visitor = Mockito.mock(QuerySpecVisitorBase.class);
		Mockito.when(visitor.visitStart(Mockito.any(QuerySpec.class))).thenReturn(true);
		Mockito.when(visitor.visitFilterStart(Mockito.any(FilterSpec.class))).thenReturn(true);
		Mockito.when(visitor.visitSort(Mockito.any(SortSpec.class))).thenReturn(false);
		Mockito.when(visitor.visitField(Mockito.any(IncludeFieldSpec.class))).thenReturn(false);
		Mockito.when(visitor.visitInclude(Mockito.any(IncludeRelationSpec.class))).thenReturn(false);
		spec.accept(visitor);

		// sort path will not be visited
		Mockito.verify(visitor, Mockito.times(1)).visitPath(Mockito.any(PathSpec.class));
	}


	@Test
	public void testVisitorWithAbort() {
		QuerySpec spec = createTestQueySpec();

		QuerySpecVisitorBase visitor = Mockito.mock(QuerySpecVisitorBase.class);
		Mockito.when(visitor.visitStart(Mockito.any(QuerySpec.class))).thenReturn(false);
		spec.accept(visitor);

		Mockito.verify(visitor, Mockito.times(1)).visitStart(Mockito.eq(spec));
		Mockito.verify(visitor, Mockito.times(0)).visitEnd(Mockito.eq(spec));
		Mockito.verify(visitor, Mockito.times(0)).visitField(Mockito.any(IncludeFieldSpec.class));
		Mockito.verify(visitor, Mockito.times(0)).visitFilterStart(Mockito.any(FilterSpec.class));
		Mockito.verify(visitor, Mockito.times(0)).visitFilterEnd(Mockito.any(FilterSpec.class));
		Mockito.verify(visitor, Mockito.times(0)).visitInclude(Mockito.any(IncludeRelationSpec.class));
		Mockito.verify(visitor, Mockito.times(0)).visitSort(Mockito.any(SortSpec.class));
		Mockito.verify(visitor, Mockito.times(0)).visitPaging(Mockito.any(PagingSpec.class));
		Mockito.verify(visitor, Mockito.times(0)).visitPath(Mockito.any(PathSpec.class));
	}
}

