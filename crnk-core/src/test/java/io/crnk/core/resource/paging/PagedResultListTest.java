package io.crnk.core.resource.paging;

import io.crnk.core.resource.list.PagedResultList;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class PagedResultListTest {

	@Test
	public void test() {
		PagedResultList<String> list = new PagedResultList<String>(new ArrayList<String>(), 13L);
		Assert.assertEquals(13L, list.getTotalCount().longValue());
	}
}
