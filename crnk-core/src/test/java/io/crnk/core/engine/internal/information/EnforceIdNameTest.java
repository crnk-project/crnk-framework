package io.crnk.core.engine.internal.information;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.mock.repository.RelationIdTestRepository;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import org.junit.Assert;
import org.junit.Test;

public class EnforceIdNameTest {

	@Test
	public void checkEnabled() {
		check(true);
	}

	@Test
	public void checkDisabled() {
		check(false);
	}

	protected void check(boolean enabled) {
		SimpleModule module = new SimpleModule("test");
		module.addRepository(new RelationIdTestRepository());
		module.addRepository(new InMemoryResourceRepository(RenamedIdResource.class));

		CrnkBoot boot = new CrnkBoot();
		boot.setPropertiesProvider(key -> {
			if (key.equals(CrnkProperties.ENFORCE_ID_NAME)) {
				return Boolean.toString(enabled);
			}
			return null;
		});
		boot.addModule(module);
		boot.boot();

		RegistryEntry entry = boot.getResourceRegistry().getEntry(RenamedIdResource.class);
		ResourceField idField = entry.getResourceInformation().getIdField();
		if (enabled) {
			Assert.assertEquals("id", idField.getJsonName());
		} else {
			Assert.assertEquals("notId", idField.getJsonName());
		}
	}


	@JsonApiResource(type = "renamedId")
	public static class RenamedIdResource {

		@JsonApiId
		private String notId;

		public String getNotId() {
			return notId;
		}

		public void setNotId(String notId) {
			this.notId = notId;
		}
	}
}
