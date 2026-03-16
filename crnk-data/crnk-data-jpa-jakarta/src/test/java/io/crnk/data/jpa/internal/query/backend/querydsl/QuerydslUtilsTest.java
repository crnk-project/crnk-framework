package io.crnk.data.jpa.internal.query.backend.querydsl;

import com.querydsl.core.types.Expression;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.Test;
import org.mockito.Mockito;

public class QuerydslUtilsTest {

	@Test
	public void checkHasPrivateConstructor() {
		ClassTestUtils.assertPrivateConstructor(QuerydslUtils.class);
	}

	@Test(expected = IllegalStateException.class)
	public void throwExceptionWhenAccessingInvalidEntityPath() {
		QuerydslUtils.getEntityPath(InvalidEntity.class);
	}

	@Test(expected = IllegalStateException.class)
	public void throwExceptionWhenGettingInvalidQueryClass() {
		QuerydslUtils.getQueryClass(InvalidEntity.class);
	}

	@Test(expected = IllegalStateException.class)
	public void throwExceptionWhenFollowingInvalidPath() {
		Expression<?> expression = Mockito.mock(Expression.class);
		Mockito.when(expression.getType()).thenReturn((Class) InvalidEntity.class);
		QuerydslUtils.get(expression, "doesNotExist");
	}

	class InvalidEntity {

	}
}