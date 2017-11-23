package io.crnk.jpa.internal;

import io.crnk.jpa.internal.query.QueryUtil;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.Test;

public class QueryUtilTest {

	@Test
	public void hasPrivateConstructor() {
		ClassTestUtils.assertPrivateConstructor(QueryUtil.class);
	}
}
