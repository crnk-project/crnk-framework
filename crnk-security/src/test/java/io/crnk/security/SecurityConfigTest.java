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

		Assert.assertEquals("ANY", rules.get(0).getRole());
		Assert.assertEquals("ANY", rules.get(1).getRole());
		Assert.assertEquals("ANY", rules.get(2).getRole());
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

	@Test
	public void testChained() {
		// tag::docs[]
		SecurityConfig config = SecurityConfig.builder()
				.permitAll(ResourcePermission.GET)
				.permitAll(Task.class, ResourcePermission.DELETE)
				.permitAll("projects", ResourcePermission.PATCH)
				.permitRole("someRole", ResourcePermission.GET)
				.permitRole("someRole", Task.class, ResourcePermission.DELETE)
				.permitRole("someRole", "projects", ResourcePermission.PATCH)
				.build();
		// end::docs[]

		List<SecurityRule> rules = config.getRules();
		Assert.assertEquals(6, rules.size());
		Assert.assertEquals(ResourcePermission.GET, rules.get(0).getPermission());
		Assert.assertEquals(ResourcePermission.DELETE, rules.get(1).getPermission());
		Assert.assertEquals(ResourcePermission.PATCH, rules.get(2).getPermission());
		Assert.assertEquals(ResourcePermission.GET, rules.get(3).getPermission());
		Assert.assertEquals(ResourcePermission.DELETE, rules.get(4).getPermission());
		Assert.assertEquals(ResourcePermission.PATCH, rules.get(5).getPermission());

		Assert.assertEquals("ANY", rules.get(0).getRole());
		Assert.assertEquals("ANY", rules.get(1).getRole());
		Assert.assertEquals("ANY", rules.get(2).getRole());
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

	@Test
	public void testMultiple() {
		SecurityConfig config = SecurityConfig.builder()
				.permitAll(ResourcePermission.GET, ResourcePermission.PATCH)
				.permitAll(Task.class, ResourcePermission.DELETE, ResourcePermission.GET)
				.permitAll("projects", ResourcePermission.PATCH, ResourcePermission.GET)
				.permitRole("someRole", ResourcePermission.GET, ResourcePermission.PATCH)
				.permitRole("someRole", Task.class, ResourcePermission.DELETE, ResourcePermission.PATCH)
				.permitRole("someRole", "projects", ResourcePermission.POST, ResourcePermission.PATCH)
				.build();

		List<SecurityRule> rules = config.getRules();
		Assert.assertEquals(12, rules.size());
		Assert.assertEquals(ResourcePermission.GET, rules.get(0).getPermission());
		Assert.assertEquals(ResourcePermission.PATCH, rules.get(1).getPermission());
		Assert.assertEquals(ResourcePermission.DELETE, rules.get(2).getPermission());
		Assert.assertEquals(ResourcePermission.GET, rules.get(3).getPermission());
		Assert.assertEquals(ResourcePermission.PATCH, rules.get(4).getPermission());
		Assert.assertEquals(ResourcePermission.GET, rules.get(5).getPermission());
		Assert.assertEquals(ResourcePermission.GET, rules.get(6).getPermission());
		Assert.assertEquals(ResourcePermission.PATCH, rules.get(7).getPermission());
		Assert.assertEquals(ResourcePermission.DELETE, rules.get(8).getPermission());
		Assert.assertEquals(ResourcePermission.PATCH, rules.get(9).getPermission());
		Assert.assertEquals(ResourcePermission.POST, rules.get(10).getPermission());
		Assert.assertEquals(ResourcePermission.PATCH, rules.get(11).getPermission());
	}
}
