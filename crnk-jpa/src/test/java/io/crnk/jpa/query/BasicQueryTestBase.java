package io.crnk.jpa.query;

import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.jpa.model.CollectionAttributesTestEntity;
import io.crnk.jpa.model.RelatedEntity;
import io.crnk.jpa.model.TestEntity;
import io.crnk.jpa.model.TestSubclassWithSuperclassPk;
import io.crnk.jpa.model.UuidTestEntity;
import org.hibernate.Hibernate;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.JoinType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@Transactional
public abstract class BasicQueryTestBase extends AbstractJpaTest {

	protected JpaQuery<TestEntity> builder() {
		return queryFactory.query(TestEntity.class);
	}

	@Test
	public void testAll() {
		assertEquals(5, builder().buildExecutor().getResultList().size());
	}

	@Test
	public void testRelations() {
		List<Long> ids = Arrays.asList(1L);
		JpaQuery<RelatedEntity> builder = queryFactory.query(TestEntity.class, TestEntity.ATTR_oneRelatedValue, "id", ids);
		RelatedEntity relatedEntity = builder.buildExecutor().getUniqueResult(false);
		assertEquals(101L, relatedEntity.getId().longValue());
	}

	@Test
	public void testRelationsWithParentIdSelection() {
		List<Long> ids = Arrays.asList(1L);
		JpaQuery<RelatedEntity> builder = queryFactory.query(TestEntity.class, TestEntity.ATTR_oneRelatedValue, "id", ids);
		builder.addParentIdSelection();
		List<Tuple> tuples = builder.buildExecutor().getResultTuples();
		Assert.assertEquals(1, tuples.size());
		Tuple tuple = tuples.get(0);
		Assert.assertEquals(1L, tuple.get(0, Object.class));
		Assert.assertEquals(101L, tuple.get(1, RelatedEntity.class).getId().longValue());
	}

	@Test
	public void testTupleQuery() {
		JpaQuery<TestEntity> query = builder();
		query.addSortBy(Arrays.asList(TestEntity.ATTR_stringValue), Direction.ASC);
		query.addSelection(Arrays.asList(TestEntity.ATTR_stringValue));
		List<io.crnk.jpa.query.Tuple> resultTuples = query.buildExecutor().getResultTuples();
		Assert.assertEquals(5, resultTuples.size());
		for (int i = 0; i < resultTuples.size(); i++) {
			io.crnk.jpa.query.Tuple tuple = resultTuples.get(i);
			Assert.assertEquals("test" + i, tuple.get(TestEntity.ATTR_stringValue, String.class));
		}
	}

	@Test
	public void testEqualsFilter() {
		assertEquals((Long) 0L,
				builder().addFilter(TestEntity.ATTR_id, FilterOperator.EQ, 0L).buildExecutor().getUniqueResult(false).getId());
		assertEquals((Long) 1L,
				builder().addFilter(TestEntity.ATTR_id, FilterOperator.EQ, 1L).buildExecutor().getUniqueResult(false).getId());
		assertEquals((Long) 2L,
				builder().addFilter(TestEntity.ATTR_id, FilterOperator.EQ, 2L).buildExecutor().getUniqueResult(false).getId());

		assertEquals((Long) 0L, builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.EQ, "test0").buildExecutor()
				.getUniqueResult(false).getId());
		assertEquals((Long) 1L, builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.EQ, "test1").buildExecutor()
				.getUniqueResult(false).getId());
		assertEquals((Long) 2L, builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.EQ, "test2").buildExecutor()
				.getUniqueResult(false).getId());
	}

	@Test
	public void testEqualsInCollectionFilter() {
		CollectionAttributesTestEntity entity = new CollectionAttributesTestEntity();
		entity.setId(13L);
		entity.setLongValues(Arrays.asList(1L, 2L));
		entity.setStringValues(Arrays.asList("John", "Doe"));
		em.persist(entity);

		assertEquals((Long) 13L, queryFactory.query(CollectionAttributesTestEntity.class)
				.addFilter(CollectionAttributesTestEntity.ATTR_stringValues, FilterOperator.EQ, "Doe")
				.buildExecutor().getUniqueResult(false).getId());
		assertEquals((Long) 13L, queryFactory.query(CollectionAttributesTestEntity.class)
				.addFilter(CollectionAttributesTestEntity.ATTR_stringValues, FilterOperator.EQ, "John")
				.buildExecutor().getUniqueResult(false).getId());
		assertNull(queryFactory.query(CollectionAttributesTestEntity.class)
				.addFilter(CollectionAttributesTestEntity.ATTR_stringValues, FilterOperator.EQ, "Jane")
				.buildExecutor().getUniqueResult(true));
	}


	@Test
	public void testNotEqualsFilter() {
		assertEquals(4, builder().addFilter(TestEntity.ATTR_id, FilterOperator.NEQ, 0L).buildExecutor().getResultList().size());
		assertEquals(4, builder().addFilter(TestEntity.ATTR_id, FilterOperator.NEQ, 1L).buildExecutor().getResultList().size());
		assertEquals(5,
				builder().addFilter(TestEntity.ATTR_id, FilterOperator.NEQ, 9999L).buildExecutor().getResultList().size());
	}

	@Test
	public void testLikeFilter() {
		assertEquals(5,
				builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.LIKE, "test%").buildExecutor().getResultList()
						.size());
		assertEquals(1,
				builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.LIKE, "test1").buildExecutor().getResultList()
						.size());
		assertEquals(0,
				builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.LIKE, "abc").buildExecutor().getResultList()
						.size());
	}

	@Test
	public void testLikeFilterWithCollectionFilterValue() {
		assertEquals(2,
				builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.LIKE, Arrays.asList("test1", "test2")).buildExecutor().getResultList()
						.size());
	}

	@Test
	public void testUuidLikeFilter() {
		UUID id = UUID.fromString("805eda6f-fa43-3586-bcd3-0e22a0a8261d");
		UuidTestEntity entity = new UuidTestEntity();
		entity.setId(id);
		em.persist(entity);

		JpaQuery<UuidTestEntity> query = queryFactory.query(UuidTestEntity.class);
		assertEquals(1, query.addFilter("id", FilterOperator.LIKE, "805%").buildExecutor().getResultList().size());
	}

	@Test
	public void testGreaterFilter() {
		assertEquals(4, builder().addFilter(TestEntity.ATTR_id, FilterOperator.GT, 0L).buildExecutor().getResultList().size());
		assertEquals(3, builder().addFilter(TestEntity.ATTR_id, FilterOperator.GT, 1L).buildExecutor().getResultList().size());
		assertEquals(0, builder().addFilter(TestEntity.ATTR_id, FilterOperator.GT, 4L).buildExecutor().getResultList().size());
		assertEquals(3,
				builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.GT, "test1").buildExecutor().getResultList()
						.size());
	}

	@Test
	public void testLessFilter() {
		assertEquals(0, builder().addFilter(TestEntity.ATTR_id, FilterOperator.LT, 0L).buildExecutor().getResultList().size());
		assertEquals(1, builder().addFilter(TestEntity.ATTR_id, FilterOperator.LT, 1L).buildExecutor().getResultList().size());
		assertEquals(2, builder().addFilter(TestEntity.ATTR_id, FilterOperator.LT, 2L).buildExecutor().getResultList().size());
		assertEquals(1,
				builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.LT, "test1").buildExecutor().getResultList()
						.size());
	}

	@Test
	public void testGreaterEqualsFilter() {
		assertEquals(5, builder().addFilter(TestEntity.ATTR_id, FilterOperator.GE, 0L).buildExecutor().getResultList().size());
		assertEquals(4, builder().addFilter(TestEntity.ATTR_id, FilterOperator.GE, 1L).buildExecutor().getResultList().size());
		assertEquals(3, builder().addFilter(TestEntity.ATTR_id, FilterOperator.GE, 2L).buildExecutor().getResultList().size());
		assertEquals(4,
				builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.GE, "test1").buildExecutor().getResultList()
						.size());
	}

	@Test
	public void testLessEqualsFilter() {
		assertEquals(1, builder().addFilter(TestEntity.ATTR_id, FilterOperator.LE, 0L).buildExecutor().getResultList().size());
		assertEquals(2, builder().addFilter(TestEntity.ATTR_id, FilterOperator.LE, 1L).buildExecutor().getResultList().size());
		assertEquals(3, builder().addFilter(TestEntity.ATTR_id, FilterOperator.LE, 2L).buildExecutor().getResultList().size());
		assertEquals(2,
				builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.LE, "test1").buildExecutor().getResultList()
						.size());
	}

	@Test
	public void testAndFilter() {
		assertEquals(4, builder().addFilter(FilterSpec
				.and(new FilterSpec(Arrays.asList(TestEntity.ATTR_id), FilterOperator.GE, 0L),
						new FilterSpec(Arrays.asList(TestEntity.ATTR_id), FilterOperator.LT, 4L)))
				.buildExecutor().getResultList().size());
		assertEquals(1, builder().addFilter(FilterSpec
				.and(new FilterSpec(Arrays.asList(TestEntity.ATTR_id), FilterOperator.GE, 3L),
						new FilterSpec(Arrays.asList(TestEntity.ATTR_id), FilterOperator.LT, 4L)))
				.buildExecutor().getResultList().size());
		assertEquals(0, builder().addFilter(FilterSpec
				.and(new FilterSpec(Arrays.asList(TestEntity.ATTR_id), FilterOperator.GE, 3L),
						new FilterSpec(Arrays.asList(TestEntity.ATTR_id), FilterOperator.LT, 3L)))
				.buildExecutor().getResultList().size());
	}

	@Test
	public void testNotFilter() {
		assertEquals(5,
				builder().addFilter(FilterSpec.not(new FilterSpec(Arrays.asList(TestEntity.ATTR_id), FilterOperator.GE, 5L)))
						.buildExecutor().getResultList().size());
		assertEquals(3,
				builder().addFilter(FilterSpec.not(new FilterSpec(Arrays.asList(TestEntity.ATTR_id), FilterOperator.GE, 3L)))
						.buildExecutor().getResultList().size());
	}

	@Test
	public void testOrFilter() {
		assertEquals(5, builder().addFilter(FilterSpec
				.or(new FilterSpec(Arrays.asList(TestEntity.ATTR_id), FilterOperator.GE, 3L),
						new FilterSpec(Arrays.asList(TestEntity.ATTR_id), FilterOperator.LT, 3L)))
				.buildExecutor().getResultList().size());
		assertEquals(2, builder().addFilter(FilterSpec
				.or(new FilterSpec(Arrays.asList(TestEntity.ATTR_id), FilterOperator.GE, 4L),
						new FilterSpec(Arrays.asList(TestEntity.ATTR_id), FilterOperator.LT, 1L)))
				.buildExecutor().getResultList().size());
	}

	@Test
	public void testEmbeddableFilter() {
		assertEquals((Long) 0L, builder().addFilter(TestEntity.ATTR_embValue_intValue, FilterOperator.EQ, 0).buildExecutor()
				.getUniqueResult(false).getId());
		assertEquals((Long) 1L, builder().addFilter(TestEntity.ATTR_embValue_intValue, FilterOperator.EQ, 1).buildExecutor()
				.getUniqueResult(false).getId());
		assertEquals((Long) 2L, builder().addFilter(TestEntity.ATTR_embValue_intValue, FilterOperator.EQ, 2).buildExecutor()
				.getUniqueResult(false).getId());

		assertEquals((Long) 0L,
				builder().addFilter(TestEntity.ATTR_embValue_stringValue, FilterOperator.EQ, "emb0").buildExecutor()
						.getUniqueResult(false).getId());
		assertEquals((Long) 1L,
				builder().addFilter(TestEntity.ATTR_embValue_stringValue, FilterOperator.EQ, "emb1").buildExecutor()
						.getUniqueResult(false).getId());
		assertEquals((Long) 2L,
				builder().addFilter(TestEntity.ATTR_embValue_stringValue, FilterOperator.EQ, "emb2").buildExecutor()
						.getUniqueResult(false).getId());

		assertEquals((Long) 0L,
				builder().addFilter(TestEntity.ATTR_embValue_nestedValue_boolValue, FilterOperator.EQ, true).buildExecutor()
						.getUniqueResult(false).getId());
		assertEquals(4,
				builder().addFilter(TestEntity.ATTR_embValue_nestedValue_boolValue, FilterOperator.EQ, false).buildExecutor()
						.getResultList().size());
	}

	@Test
	public void testMapFilter() {
		assertEquals((Long) 0L, builder().addFilter(TestEntity.ATTR_mapValue + ".a", FilterOperator.EQ, "a0").buildExecutor()
				.getUniqueResult(false).getId());
		assertEquals((Long) 0L, builder().addFilter(TestEntity.ATTR_mapValue + ".b", FilterOperator.EQ, "b0").buildExecutor()
				.getUniqueResult(false).getId());
		assertEquals((Long) 1L, builder().addFilter(TestEntity.ATTR_mapValue + ".a", FilterOperator.EQ, "a1").buildExecutor()
				.getUniqueResult(false).getId());
		assertEquals((Long) 1L, builder().addFilter(TestEntity.ATTR_mapValue + ".b", FilterOperator.EQ, "b1").buildExecutor()
				.getUniqueResult(false).getId());
		assertNull(builder().addFilter(TestEntity.ATTR_mapValue + ".a", FilterOperator.EQ, "b1").buildExecutor()
				.getUniqueResult(true));
	}

	@Test
	public void testJoinFilter() {
		assertEquals((Long) 0L, builder()
				.addFilter(TestEntity.ATTR_oneRelatedValue + "." + RelatedEntity.ATTR_stringValue, FilterOperator.EQ, "related0")
				.buildExecutor().getUniqueResult(false).getId());
		assertEquals((Long) 1L, builder()
				.addFilter(TestEntity.ATTR_oneRelatedValue + "." + RelatedEntity.ATTR_stringValue, FilterOperator.EQ, "related1")
				.buildExecutor().getUniqueResult(false).getId());
		assertEquals((Long) 2L, builder()
				.addFilter(TestEntity.ATTR_oneRelatedValue + "." + RelatedEntity.ATTR_stringValue, FilterOperator.EQ, "related2")
				.buildExecutor().getUniqueResult(false).getId());
	}

	@Test(expected = IllegalStateException.class)
	public void testThrowExceptionOnNonUnique() {
		builder().buildExecutor().getUniqueResult(false);
	}

	@Test(expected = IllegalStateException.class)
	public void testThrowExceptionOnNonNullableUnique() {
		builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.EQ, "doesNotExist").buildExecutor()
				.getUniqueResult(false);
	}

	@Test
	public void testPrimitiveOrder() {
		assertEquals(5,
				builder().addSortBy(Arrays.asList(TestEntity.ATTR_id), Direction.DESC).buildExecutor().getResultList().size());
		assertEquals((Long) 0L,
				builder().addSortBy(Arrays.asList(TestEntity.ATTR_id), Direction.ASC).buildExecutor().getResultList().get(0)
						.getId());
		assertEquals((Long) 4L,
				builder().addSortBy(Arrays.asList(TestEntity.ATTR_id), Direction.DESC).buildExecutor().getResultList().get(0)
						.getId());

		assertEquals(5,
				builder().addSortBy(Arrays.asList(TestEntity.ATTR_stringValue), Direction.DESC).buildExecutor().getResultList()
						.size());
		assertEquals((Long) 0L,
				builder().addSortBy(Arrays.asList(TestEntity.ATTR_stringValue), Direction.ASC).buildExecutor().getResultList()
						.get(0).getId());
		assertEquals((Long) 4L,
				builder().addSortBy(Arrays.asList(TestEntity.ATTR_stringValue), Direction.DESC).buildExecutor().getResultList()
						.get(0).getId());
	}

	@Test
	public void testEmbeddedOrder() {
		assertEquals(5,
				builder().addSortBy(TestEntity.ATTR_embValue_intValue, Direction.DESC).buildExecutor().getResultList().size());
		assertEquals((Long) 0L,
				builder().addSortBy(TestEntity.ATTR_embValue_intValue, Direction.ASC).buildExecutor().getResultList().get(0)
						.getId());
		assertEquals((Long) 4L,
				builder().addSortBy(TestEntity.ATTR_embValue_intValue, Direction.DESC).buildExecutor().getResultList().get(0)
						.getId());
	}

	@Test
	public void testOneRelatedEntityOrder() {
		assertEquals(5, builder().setDefaultJoinType(JoinType.LEFT)
				.addSortBy(Arrays.asList(TestEntity.ATTR_oneRelatedValue), Direction.DESC).buildExecutor().getResultList()
				.size());
		assertEquals((Long) 0L,
				builder().addSortBy(Arrays.asList(TestEntity.ATTR_oneRelatedValue), Direction.ASC).buildExecutor()
						.getResultList()
						.get(0).getId());
		assertEquals((Long) 3L,
				builder().addSortBy(Arrays.asList(TestEntity.ATTR_oneRelatedValue), Direction.DESC).buildExecutor()
						.getResultList().get(0).getId());
	}

	@Test
	public void testOneRelatedAttributeOrder() {
		assertEquals((Long) 0L,
				builder().addSortBy(Arrays.asList(TestEntity.ATTR_oneRelatedValue, RelatedEntity.ATTR_stringValue), Direction
						.ASC)
						.buildExecutor().getResultList().get(0).getId());
		assertEquals((Long) 3L, builder()
				.addSortBy(Arrays.asList(TestEntity.ATTR_oneRelatedValue, RelatedEntity.ATTR_stringValue), Direction.DESC)
				.buildExecutor().getResultList().get(0).getId());
	}

	@Test
	public void testMapOrder() {
		assertEquals(5, builder().setDefaultJoinType(JoinType.LEFT)
				.addSortBy(Arrays.asList(TestEntity.ATTR_mapValue, "a"), Direction.DESC).buildExecutor().getResultList().size());

		List<TestEntity> list =
				builder().addSortBy(Arrays.asList(TestEntity.ATTR_mapValue, "a"), Direction.ASC).buildExecutor().getResultList();
		assertEquals((Long) 0L, list.get(1).getId());
		list = builder().addSortBy(Arrays.asList(TestEntity.ATTR_mapValue, "a"), Direction.DESC).buildExecutor().getResultList();
		assertEquals((Long) 3L, list.get(0).getId());
	}

	@Test
	public void testTotalOrderNoSorting() {
		testPaging(false);

		JpaQueryExecutor<TestEntity> exec = builder().buildExecutor();
		for (int i = 0; i < 5; i++) {
			exec.setWindow(i, 1);
			TestEntity entity = exec.getUniqueResult(false);
			assertEquals(i, entity.getId().intValue());
		}
	}

	@Test
	public void testTotalOrderNoTotalSorting() {
		JpaQueryExecutor<TestEntity> exec =
				builder().addSortBy(TestEntity.ATTR_embValue_nestedValue_boolValue, Direction.ASC).buildExecutor();
		for (int i = 0; i < 5; i++) {
			exec.setWindow(i, 1);
			TestEntity entity = exec.getUniqueResult(false);
			if (i == 4) {
				assertTrue(entity.getEmbValue().getNestedValue().getEmbBoolValue());
				assertEquals(0, entity.getId().intValue());
			} else {
				assertFalse(entity.getEmbValue().getNestedValue().getEmbBoolValue());
				assertEquals(1 + i, entity.getId().intValue());
			}
		}
	}

	@Test
	public void testPaging() {
		testPaging(true);
	}

	private void testPaging(boolean applySorting) {
		JpaQuery<TestEntity> builder = builder();
		if (applySorting) {
			builder.addSortBy(Arrays.asList(TestEntity.ATTR_id), Direction.DESC);
		}
		JpaQueryExecutor<TestEntity> exec = builder().buildExecutor();

		assertEquals(5, exec.getResultList().size());

		// repeat
		assertEquals(5, exec.getResultList().size());

		// apply paging
		assertEquals(4, exec.setWindow(1, -1).getResultList().size());
		assertEquals(3, exec.setWindow(2, -1).getResultList().size());
		assertEquals(2, exec.setWindow(3, -1).getResultList().size());
		assertEquals(1, exec.setWindow(4, -1).getResultList().size());
		assertEquals(0, exec.setWindow(5, -1).getResultList().size());
		assertEquals(0, exec.setWindow(6, -1).getResultList().size());

		assertEquals(2, exec.setWindow(1, 2).getResultList().size());
		assertEquals(2, exec.setWindow(2, 2).getResultList().size());
		assertEquals(2, exec.setWindow(3, 2).getResultList().size());
		assertEquals(1, exec.setWindow(4, 2).getResultList().size());
		assertEquals(0, exec.setWindow(5, 2).getResultList().size());
	}

	@Test
	public void testFilterNull() {
		assertEquals(5, builder().buildExecutor().getResultList().size());
		assertEquals(4,
				builder().addFilter(TestEntity.ATTR_oneRelatedValue, FilterOperator.NEQ, null).buildExecutor().getResultList()
						.size());

		// NOTE one could argue about the left join...
		assertEquals(1,
				builder().setDefaultJoinType(JoinType.LEFT).addFilter(TestEntity.ATTR_oneRelatedValue, FilterOperator.EQ, null)
						.buildExecutor().getResultList().size());
	}

	@Test
	public void testWithGraphControlWithoutJoin() {
		JpaQueryExecutor<TestEntity> exec = builder().buildExecutor().fetch(Arrays.asList(TestEntity.ATTR_oneRelatedValue));
		for (TestEntity test : exec.getResultList()) {
			assertTrue(Hibernate.isInitialized(test));

			RelatedEntity relatedValue = test.getOneRelatedValue();
			if (relatedValue != null) {
				assertTrue(Hibernate.isInitialized(relatedValue));
			}
		}
	}

	@Test
	public void testWithGraphControlWithJoin() {
		JpaQueryExecutor<TestEntity> exec =
				builder().addFilter(TestEntity.ATTR_oneRelatedValue, FilterOperator.NEQ, null).buildExecutor()
						.fetch(Arrays.asList(TestEntity.ATTR_oneRelatedValue));
		for (TestEntity test : exec.getResultList()) {
			assertTrue(Hibernate.isInitialized(test));
			assertTrue(Hibernate.isInitialized(test.getOneRelatedValue()));
		}
	}

	@Test
	public void testWithoutGraphControl() {
		JpaQueryExecutor<TestEntity> exec =
				builder().addFilter(TestEntity.ATTR_oneRelatedValue, FilterOperator.NEQ, null).buildExecutor();
		for (TestEntity test : exec.getResultList()) {
			RelatedEntity relatedValue = test.getOneRelatedValue();
			assertTrue(Hibernate.isInitialized(test));
			assertFalse(Hibernate.isInitialized(relatedValue));
		}
	}

	// NOTE enable with Java 8
	// @Test
	// public void testDateTime() {
	// assertEquals(5, builder().addFilter(TestEntity.ATTR_localDateValue,
	// FilterOperator.LESS_EQUAL, LocalDate.now())
	// .buildExecutor().getResultList().size());
	// assertEquals(0, builder().addFilter(TestEntity.ATTR_localDateValue,
	// FilterOperator.GREATER, LocalDate.now())
	// .buildExecutor().getResultList().size());
	//
	// assertEquals(5, builder().addFilter(TestEntity.ATTR_localTimeValue,
	// FilterOperator.LESS_EQUAL, LocalTime.now())
	// .buildExecutor().getResultList().size());
	// assertEquals(0, builder().addFilter(TestEntity.ATTR_localTimeValue,
	// FilterOperator.GREATER, LocalTime.now())
	// .buildExecutor().getResultList().size());
	//
	// assertEquals(5,
	// builder().addFilter(TestEntity.ATTR_localDateTimeValue,
	// FilterOperator.LESS_EQUAL, LocalDateTime.now())
	// .buildExecutor().getResultList().size());
	// assertEquals(0,
	// builder().addFilter(TestEntity.ATTR_localDateTimeValue,
	// FilterOperator.GREATER, LocalDateTime.now())
	// .buildExecutor().getResultList().size());
	//
	// assertEquals(5,
	// builder()
	// .addFilter(TestEntity.ATTR_offsetDateTimeValue,
	// FilterOperator.LESS_EQUAL, OffsetDateTime.now())
	// .buildExecutor().getResultList().size());
	// assertEquals(0,
	// builder().addFilter(TestEntity.ATTR_offsetDateTimeValue,
	// FilterOperator.GREATER, OffsetDateTime.now())
	// .buildExecutor().getResultList().size());
	//
	// assertEquals(5,
	// builder().addFilter(TestEntity.ATTR_offsetTimeValue,
	// FilterOperator.LESS_EQUAL, OffsetTime.now())
	// .buildExecutor().getResultList().size());
	// assertEquals(0, builder().addFilter(TestEntity.ATTR_offsetTimeValue,
	// FilterOperator.GREATER, OffsetTime.now())
	// .buildExecutor().getResultList().size());
	// }

	@Test
	public void testJoinType() {
		// note one entity has no relation
		assertEquals(4, builder().setDefaultJoinType(JoinType.INNER)
				.addSortBy(Arrays.asList(TestEntity.ATTR_oneRelatedValue, RelatedEntity.ATTR_id), Direction.ASC).buildExecutor()
				.getResultList().size());
		assertEquals(5, builder().setDefaultJoinType(JoinType.LEFT)
				.addSortBy(Arrays.asList(TestEntity.ATTR_oneRelatedValue, RelatedEntity.ATTR_id), Direction.ASC).buildExecutor()
				.getResultList().size());
		assertEquals(4, builder().setJoinType(Arrays.asList(TestEntity.ATTR_oneRelatedValue), JoinType.INNER)
				.addSortBy(Arrays.asList(TestEntity.ATTR_oneRelatedValue, RelatedEntity.ATTR_id), Direction.ASC)
				.buildExecutor().getResultList().size());
		assertEquals(5, builder().setJoinType(Arrays.asList(TestEntity.ATTR_oneRelatedValue), JoinType.LEFT)
				.addSortBy(Arrays.asList(TestEntity.ATTR_oneRelatedValue, RelatedEntity.ATTR_id), Direction.ASC).buildExecutor()
				.getResultList().size());
	}

	@Test
	public void testAnyType() {
		assertEquals(0, builder().addFilter(TestEntity.ATTR_embValue_anyValue, FilterOperator.EQ, "first").buildExecutor()
				.getUniqueResult(false).getId().intValue());
		assertEquals(1, builder().addFilter(TestEntity.ATTR_embValue_anyValue, FilterOperator.EQ, 1).buildExecutor()
				.getUniqueResult(false).getId().intValue());
		assertEquals(2, builder().addFilter(TestEntity.ATTR_embValue_anyValue, FilterOperator.EQ, 2).buildExecutor()
				.getUniqueResult(false).getId().intValue());
		assertEquals(3, builder().addFilter(TestEntity.ATTR_embValue_anyValue, FilterOperator.EQ, 3).buildExecutor()
				.getUniqueResult(false).getId().intValue());
		assertEquals(4, builder().addFilter(TestEntity.ATTR_embValue_anyValue, FilterOperator.EQ, 4).buildExecutor()
				.getUniqueResult(false).getId().intValue());

		List<TestEntity> list =
				builder().addSortBy(TestEntity.ATTR_embValue_anyValue, Direction.DESC).buildExecutor().getResultList();
		assertEquals(5, list.size());
		assertEquals("first", list.get(0).getEmbValue().getAnyValue().getValue());
		assertEquals(4, list.get(1).getEmbValue().getAnyValue().getValue());
		assertEquals(3, list.get(2).getEmbValue().getAnyValue().getValue());
		assertEquals(2, list.get(3).getEmbValue().getAnyValue().getValue());
		assertEquals(1, list.get(4).getEmbValue().getAnyValue().getValue());
	}

	@Test
	public void testEqualsFilterWithCollection() {
		assertEquals(3, builder().addFilter(TestEntity.ATTR_id, FilterOperator.EQ, Arrays.asList(0L, 1L, 2L)).buildExecutor()
				.getResultList().size());

		assertEquals(3, builder().addFilter(TestEntity.ATTR_id, FilterOperator.EQ, new HashSet<>(Arrays.asList(0L, 1L, 2L)))
				.buildExecutor().getResultList().size());

		List<Long> largeList = new ArrayList<>();
		for (long i = 2; i < 2500; i++) {
			largeList.add(i);
		}
		largeList.add(0L);

		JpaQuery<TestEntity> query = builder().addFilter(TestEntity.ATTR_id, FilterOperator.EQ, largeList);
		JpaQueryExecutor<TestEntity> executor = query.buildExecutor();
		assertEquals(4, executor.getResultList().size());

	}

	@Test
	public void testMappedSuperTypeWithPkOnSuperType() {
		RelatedEntity related = new RelatedEntity();
		related.setId(23423L);
		related.setStringValue("test");
		em.persist(related);

		TestSubclassWithSuperclassPk entity = new TestSubclassWithSuperclassPk();
		entity.setId("testId");
		entity.setLongValue(12L);
		entity.setSuperRelatedValue(related);
		em.persist(entity);

		JpaQuery<TestSubclassWithSuperclassPk> query = queryFactory.query(TestSubclassWithSuperclassPk.class);
		List<TestSubclassWithSuperclassPk> list = query.buildExecutor().getResultList();
		Assert.assertEquals(1, list.size());
		TestSubclassWithSuperclassPk testEntity = list.get(0);
		Assert.assertEquals("testId", testEntity.getId());

		JpaQuery<Object> relatedQuery =
				queryFactory.query(TestSubclassWithSuperclassPk.class, "superRelatedValue", "id", Arrays.asList("testId"));
		relatedQuery.addParentIdSelection();
		JpaQueryExecutor<Object> relatedExecutor = relatedQuery.buildExecutor();
		List<Tuple> resultTuples = relatedExecutor.getResultTuples();

		Assert.assertEquals(1, resultTuples.size());
		Tuple resultTuple = resultTuples.get(0);
		Assert.assertEquals("testId", resultTuple.get(0, String.class));
		RelatedEntity relatedEntity = resultTuple.get(1, RelatedEntity.class);
		Assert.assertEquals(23423L, relatedEntity.getId().longValue());
	}

}
