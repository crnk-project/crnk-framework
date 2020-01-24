package io.crnk.internal.boot.cdi;

import io.crnk.cdi.internal.CdiServiceDiscovery;
import io.crnk.core.repository.Repository;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class WithoutCdiContextText {

	@Test
	public void checkNoCdiContext() {
		CdiServiceDiscovery discovery = new CdiServiceDiscovery();

		List<Repository> list = discovery.getInstancesByType(Repository.class);
		Assert.assertTrue(list.isEmpty());
	}
}
