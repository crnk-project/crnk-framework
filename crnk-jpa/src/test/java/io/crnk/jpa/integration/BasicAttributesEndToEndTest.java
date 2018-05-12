package io.crnk.jpa.integration;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.jpa.AbstractJpaJerseyTest;
import io.crnk.jpa.model.BasicAttributesTestEntity;
import io.crnk.jpa.model.JpaTransientTestEntity;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.List;

public class BasicAttributesEndToEndTest extends AbstractJpaJerseyTest {

	@Test
	public void testCanStoreBasicAttributeValues() throws InstantiationException, IllegalAccessException {
		ResourceRepositoryV2<BasicAttributesTestEntity, Serializable> repo = client.getRepositoryForType(BasicAttributesTestEntity.class);

		BasicAttributesTestEntity test = new BasicAttributesTestEntity();
		test.setId(1L);
		test.setLongValue(15);
		test.setNullableBooleanValue(false);
		test.setBooleanValue(true);
		test.setLongValue(13L);
		test.setNullableLongValue(14L);
		repo.create(test);

		ResourceList<BasicAttributesTestEntity> list = repo.findAll(new QuerySpec(BasicAttributesTestEntity.class));
		Assert.assertEquals(1, list.size());
		BasicAttributesTestEntity saved = list.get(0);
		Assert.assertEquals(saved.getLongValue(), test.getLongValue());
		Assert.assertEquals(saved.getNullableBooleanValue(), test.getNullableBooleanValue());
		Assert.assertEquals(saved.getBooleanValue(), test.getBooleanValue());
		Assert.assertEquals(saved.getNullableBooleanValue(), test.getNullableBooleanValue());
	}

	@Test
	public void testJpaTransientFieldIgnored() {
		QuerySpec querySpec = new QuerySpec(JpaTransientTestEntity.class);

		ResourceRepositoryV2<JpaTransientTestEntity, Serializable> repo = client.getRepositoryForType(JpaTransientTestEntity.class);

		JpaTransientTestEntity entity = new JpaTransientTestEntity();
		entity.setId(12L);
		repo.create(entity);

		List<JpaTransientTestEntity> list = repo.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		entity = list.get(0);
		Assert.assertNotNull(entity);

		repo.delete(entity.getId());
		list = repo.findAll(querySpec);
		Assert.assertEquals(0, list.size());
	}

}
