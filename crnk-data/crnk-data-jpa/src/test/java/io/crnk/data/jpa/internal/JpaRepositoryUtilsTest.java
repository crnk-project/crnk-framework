package io.crnk.data.jpa.internal;

import io.crnk.test.mock.ClassTestUtils;
import org.junit.Test;

public class JpaRepositoryUtilsTest {

	@Test
	public void hasPrivateConstructor() {
		ClassTestUtils.assertPrivateConstructor(JpaRepositoryUtils.class);
	}
}
