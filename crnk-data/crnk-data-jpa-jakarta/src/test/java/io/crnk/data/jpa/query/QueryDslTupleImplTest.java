package io.crnk.data.jpa.query;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import io.crnk.data.jpa.internal.query.backend.querydsl.QuerydslTupleImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class QueryDslTupleImplTest {

	private QuerydslTupleImpl impl;
	private Expression expression;

	@Before
	public void setup() {

		expression = Mockito.mock(Expression.class);
		Tuple tuple = Mockito.mock(Tuple.class);
		Mockito.when(tuple.size()).thenReturn(2);
		Mockito.when(tuple.get(expression)).thenReturn("test");
		Mockito.when(tuple.toArray()).thenReturn(new Object[]{"0", "1"});
		Mockito.when(tuple.get(0, String.class)).thenReturn("0");
		Mockito.when(tuple.get(1, String.class)).thenReturn("1");

		Mockito.when(tuple.size()).thenReturn(2);
		Map<String, Integer> selectionBindings = new HashMap<>();
		impl = new QuerydslTupleImpl(tuple, selectionBindings);
	}

	@Test
	public void testReduce() {
		Assert.assertEquals(2, impl.size());
		Assert.assertEquals(2, impl.size());
		Assert.assertArrayEquals(new Object[]{"0", "1"}, impl.toArray());
		impl.reduce(1);
		Assert.assertEquals("1", impl.get(0, String.class));
		Assert.assertEquals(1, impl.size());
		Assert.assertArrayEquals(new Object[]{"1"}, impl.toArray());

		Assert.assertEquals("test", impl.get(expression));

	}
}
