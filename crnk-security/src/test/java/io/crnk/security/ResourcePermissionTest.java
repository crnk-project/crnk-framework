package io.crnk.security;

import io.crnk.core.engine.http.HttpMethod;
import org.junit.Assert;
import org.junit.Test;


public class ResourcePermissionTest {

	@Test
	public void xor() {
		Assert.assertTrue(ResourcePermission.DELETE.xor(ResourcePermission.DELETE).isEmpty());
		Assert.assertTrue(ResourcePermission.GET.xor(ResourcePermission.GET).isEmpty());
		Assert.assertEquals(ResourcePermission.create(false, true, false, true),
				ResourcePermission.GET.xor(ResourcePermission.DELETE));
	}

	@Test
	public void and() {
		Assert.assertEquals(ResourcePermission.DELETE, ResourcePermission.DELETE.or(ResourcePermission.DELETE));
		Assert.assertEquals(ResourcePermission.GET, ResourcePermission.GET.or(ResourcePermission.GET));
		Assert.assertTrue(ResourcePermission.GET.and(ResourcePermission.DELETE).isEmpty());
	}

	@Test
	public void or() {
		Assert.assertEquals(ResourcePermission.DELETE, ResourcePermission.DELETE.or(ResourcePermission.DELETE));
		Assert.assertEquals(ResourcePermission.GET, ResourcePermission.GET.or(ResourcePermission.GET));
		Assert.assertEquals(ResourcePermission.create(false, true, false, true),
				ResourcePermission.GET.or(ResourcePermission.DELETE));
	}

	@Test
	public void fromMethod() {
		Assert.assertEquals(ResourcePermission.GET, ResourcePermission.fromMethod(HttpMethod.GET));
		Assert.assertEquals(ResourcePermission.POST, ResourcePermission.fromMethod(HttpMethod.POST));
		Assert.assertEquals(ResourcePermission.DELETE, ResourcePermission.fromMethod(HttpMethod.DELETE));
		Assert.assertEquals(ResourcePermission.PATCH, ResourcePermission.fromMethod(HttpMethod.PATCH));
	}

	@Test
	public void string() {
		Assert.assertEquals("ResourcePermission[post=false, get=true, patch=false, delete=false]",
				ResourcePermission.GET.toString());
	}
}
