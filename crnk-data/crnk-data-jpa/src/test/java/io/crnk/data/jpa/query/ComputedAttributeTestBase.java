package io.crnk.data.jpa.query;

import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.data.jpa.model.TestEntity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@Transactional
public abstract class ComputedAttributeTestBase extends AbstractJpaTest {

	protected static final String ATTR_VIRTUAL_VALUE = "virtualValue";

	private JpaQuery<TestEntity> builder() {
		return queryFactory.query(TestEntity.class);
	}

	@Test
	public void testEqualsFilter() {

		assertEquals((Long) 1L, builder().addFilter(ATTR_VIRTUAL_VALUE, FilterOperator.EQ, "TEST1").buildExecutor().getUniqueResult(false).getId());

	}

	@Test
	public void testSelection() {
		JpaQuery<TestEntity> query = builder();
		query.addSelection(Arrays.asList(ATTR_VIRTUAL_VALUE));
		query.addSortBy(Arrays.asList(TestEntity.ATTR_stringValue), Direction.ASC);

		List<Tuple> resultList = query.buildExecutor().getResultTuples();
		Assert.assertEquals(5, resultList.size());
		for (int i = 0; i < resultList.size(); i++) {
			Tuple tuple = resultList.get(i);
			assertEquals("TEST" + i, tuple.get(ATTR_VIRTUAL_VALUE, String.class));
		}
	}

}
