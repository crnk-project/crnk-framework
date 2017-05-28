package io.crnk.security;

import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.http.HttpMethod;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Assert;
import org.junit.Test;


public class ResourcePermissionTest {

	@Test
	public void testEquals() throws NoSuchFieldException {
		EqualsVerifier.forClass(ResourceIdentifier.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();

		Assert.assertEquals(ResourcePermission.ALL, ResourcePermission.ALL);
		Assert.assertEquals(ResourcePermission.DELETE, ResourcePermission.DELETE);
		Assert.assertEquals(ResourcePermission.GET, ResourcePermission.GET);
		Assert.assertEquals(ResourcePermission.POST, ResourcePermission.POST);
		Assert.assertNotEquals(ResourcePermission.DELETE, ResourcePermission.ALL);
		Assert.assertNotEquals(ResourcePermission.DELETE, ResourcePermission.GET);
		Assert.assertNotEquals(ResourcePermission.DELETE, ResourcePermission.PATCH);
		Assert.assertNotEquals(ResourcePermission.DELETE, ResourcePermission.POST);
		Assert.assertNotEquals(ResourcePermission.DELETE, "not a resource permission");
	}

	@Test
	public void testHashCode() throws NoSuchFieldException {
		Assert.assertEquals(ResourcePermission.ALL.hashCode(), ResourcePermission.ALL.hashCode());
		Assert.assertNotEquals(ResourcePermission.DELETE.hashCode(), ResourcePermission.ALL.hashCode());
		Assert.assertNotEquals(ResourcePermission.GET.hashCode(), ResourcePermission.ALL.hashCode());
		Assert.assertNotEquals(ResourcePermission.POST.hashCode(), ResourcePermission.PATCH.hashCode());
		Assert.assertNotEquals(ResourcePermission.POST.hashCode(), ResourcePermission.GET.hashCode());
	}


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
