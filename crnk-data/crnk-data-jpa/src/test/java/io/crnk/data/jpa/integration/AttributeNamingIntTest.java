package io.crnk.data.jpa.integration;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.data.jpa.AbstractJpaJerseyTest;
import io.crnk.data.jpa.model.NamingTestEntity;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;

public class AttributeNamingIntTest extends AbstractJpaJerseyTest {

	@Test
	public void testCanStoreBasicAttributeValues() {
		ResourceRepository<NamingTestEntity, Serializable> repo = client.getRepositoryForType(NamingTestEntity.class);

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
