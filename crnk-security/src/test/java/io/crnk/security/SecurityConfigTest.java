package io.crnk.security;

import java.util.List;

import io.crnk.security.SecurityConfig.Builder;
import io.crnk.security.model.Task;
import org.junit.Assert;
import org.junit.Test;

public class SecurityConfigTest {

	@Test
	public void test() {
		// tag::docs[]
		Builder builder = SecurityConfig.builder();
		builder.permitAll(ResourcePermission.GET);
		builder.permitAll(Task.class, ResourcePermission.DELETE);
		builder.permitAll("projects", ResourcePermission.PATCH);
		builder.permitRole("someRole", ResourcePermission.GET);
		builder.permitRole("someRole", Task.class, ResourcePermission.DELETE);
		builder.permitRole("someRole", "projects", ResourcePermission.PATCH);
		SecurityConfig config = builder.build();
		// end::docs[]

		List<SecurityRule> rules = config.getRules();
		Assert.assertEquals(6, rules.size());
		Assert.assertEquals(ResourcePermission.GET, rules.get(0).getPermission());
		Assert.assertEquals(ResourcePermission.DELETE, rules.get(1).getPermission());
		Assert.assertEquals(ResourcePermission.PATCH, rules.get(2).getPermission());
		Assert.assertEquals(ResourcePermission.GET, rules.get(3).getPermission());
		Assert.assertEquals(ResourcePermission.DELETE, rules.get(4).getPermission());
		Assert.assertEquals(ResourcePermission.PATCH, rules.get(5).getPermission());

		Assert.assertNull(rules.get(0).getRole());
		Assert.assertNull(rules.get(1).getRole());
		Assert.assertNull(rules.get(2).getRole());
		Assert.assertEquals("someRole", rules.get(3).getRole());
		Assert.assertEquals("someRole", rules.get(4).getRole());
		Assert.assertEquals("someRole", rules.get(5).getRole());

		Assert.assertNull(rules.get(0).getResourceClass());
		Assert.assertEquals(Task.class, rules.get(1).getResourceClass());
		Assert.assertEquals("projects", rules.get(2).getResourceType());
		Assert.assertNull(rules.get(3).getResourceClass());
		Assert.assertEquals(Task.class, rules.get(4).getResourceClass());
		Assert.assertEquals("projects", rules.get(5).getResourceType());
	}
}
