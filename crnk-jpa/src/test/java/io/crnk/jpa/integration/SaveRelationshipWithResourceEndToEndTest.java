package io.crnk.jpa.integration;

import com.google.common.collect.Sets;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.jpa.AbstractJpaJerseyTest;
import io.crnk.jpa.JpaModule;
import io.crnk.jpa.model.ManyToManyOppositeEntity;
import io.crnk.jpa.model.ManyToManyTestEntity;
import io.crnk.jpa.model.OneToOneTestEntity;
import io.crnk.jpa.model.RelatedEntity;
import io.crnk.jpa.query.criteria.JpaCriteriaQueryFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;

public class SaveRelationshipWithResourceEndToEndTest extends AbstractJpaJerseyTest {


	@Override
	protected void setupModule(JpaModule module, boolean server) {
		if (server) {
			module.setQueryFactory(JpaCriteriaQueryFactory.newInstance());
		}
	}

	@Test
	public void testOneToOne() throws InstantiationException, IllegalAccessException {
		RelatedEntity related = new RelatedEntity();
		related.setId(12L);
		ResourceRepositoryV2<RelatedEntity, Serializable> relatedRepo = client.getRepositoryForType(RelatedEntity.class);
		relatedRepo.create(related);

		OneToOneTestEntity test = new OneToOneTestEntity();
		test.setId(11L);
		test.setOneRelatedValue(related);
		ResourceRepositoryV2<OneToOneTestEntity, Serializable> testRepo = client.getRepositoryForType(OneToOneTestEntity.class);
		testRepo.create(test);

		QuerySpec querySpec = new QuerySpec(OneToOneTestEntity.class);
		querySpec.includeRelation(Arrays.asList("oneRelatedValue"));
		ResourceList<OneToOneTestEntity> list = testRepo.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		OneToOneTestEntity testCopy = list.get(0);
		Assert.assertNotNull(testCopy.getOneRelatedValue());
		Assert.assertEquals(12L, testCopy.getOneRelatedValue().getId().longValue());
	}

	@Test
	public void testManyToMany() throws InstantiationException, IllegalAccessException {
		ManyToManyOppositeEntity related = new ManyToManyOppositeEntity();
		related.setId(12L);
		ResourceRepositoryV2<ManyToManyOppositeEntity, Serializable> relatedRepo =
				client.getRepositoryForType(ManyToManyOppositeEntity.class);
		relatedRepo.create(related);

		ManyToManyTestEntity test = new ManyToManyTestEntity();
		test.setId(11L);
		test.setOpposites(Sets.newHashSet(related));
		ResourceRepositoryV2<ManyToManyTestEntity, Serializable> testRepo =
				client.getRepositoryForType(ManyToManyTestEntity.class);
		testRepo.create(test);

		QuerySpec querySpec = new QuerySpec(ManyToManyTestEntity.class);
		querySpec.includeRelation(Arrays.asList("opposites"));
		ResourceList<ManyToManyTestEntity> list = testRepo.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		ManyToManyTestEntity testCopy = list.get(0);
		Assert.assertEquals(1, testCopy.getOpposites().size());
	}
}
