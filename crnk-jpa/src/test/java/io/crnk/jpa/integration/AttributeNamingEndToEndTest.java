package io.crnk.jpa.integration;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.jpa.AbstractJpaJerseyTest;
import io.crnk.jpa.model.NamingTestEntity;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;

public class AttributeNamingEndToEndTest extends AbstractJpaJerseyTest {

	@Test
	public void testCanStoreBasicAttributeValues() {
		ResourceRepositoryV2<NamingTestEntity, Serializable> repo = client.getRepositoryForType(NamingTestEntity.class);

		NamingTestEntity test = new NamingTestEntity();
		test.setId(1L);
		test.setSEcondUpperCaseValue(13L);
		repo.create(test);

		ResourceList<NamingTestEntity> list = repo.findAll(new QuerySpec(NamingTestEntity.class));
		Assert.assertEquals(1, list.size());
		NamingTestEntity saved = list.get(0);
		Assert.assertEquals(saved.getSEcondUpperCaseValue(), test.getSEcondUpperCaseValue());
	}
}
