package io.crnk.jpa.mapping;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;

import com.querydsl.core.types.Expression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.jpa.AbstractJpaJerseyTest;
import io.crnk.jpa.JpaModule;
import io.crnk.jpa.JpaRepositoryConfig;
import io.crnk.jpa.JpaRepositoryFilterBase;
import io.crnk.jpa.model.QTestEntity;
import io.crnk.jpa.model.RelatedEntity;
import io.crnk.jpa.model.TestEntity;
import io.crnk.jpa.model.dto.RelatedDTO;
import io.crnk.jpa.model.dto.TestDTO;
import io.crnk.jpa.query.JpaQuery;
import io.crnk.jpa.query.querydsl.QuerydslExpressionFactory;
import io.crnk.jpa.query.querydsl.QuerydslQueryFactory;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaKey;
import io.crnk.meta.model.resource.MetaResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Example of how to do DTO mapping and computed attributes.
 */
public class DtoMappingTest extends AbstractJpaJerseyTest {

	private ResourceRepositoryV2<TestEntity, Long> testRepo;

	private TestDTOMapper dtoMapper;

	@Override
	@Before
	public void setup() {
		super.setup();
		testRepo = client.getRepositoryForType(TestEntity.class);
	}

	@Override
	protected void setupModule(JpaModule module, boolean server) {
		super.setupModule(module, server);

		if (server) {
			EntityManager entityManager = module.getEntityManager();
			dtoMapper = Mockito.spy(new TestDTOMapper(entityManager));
			QuerydslExpressionFactory<QTestEntity> basicComputedValueFactory = new QuerydslExpressionFactory<QTestEntity>() {

				@Override
				public Expression<String> getExpression(QTestEntity parent, JPAQuery<?> jpaQuery) {
					return parent.stringValue.upper();
				}
			};
			QuerydslExpressionFactory<QTestEntity> complexComputedValueFactory = new QuerydslExpressionFactory<QTestEntity>() {

				@Override
				public Expression<Long> getExpression(QTestEntity parent, JPAQuery<?> jpaQuery) {
					QTestEntity root = QTestEntity.testEntity;
					QTestEntity sub = new QTestEntity("subquery");
					return JPAExpressions.select(sub.id.count()).from(sub).where(sub.id.lt(root.id));
				}
			};

			QuerydslQueryFactory queryFactory = (QuerydslQueryFactory) module.getQueryFactory();
			queryFactory.registerComputedAttribute(TestEntity.class, TestDTO.ATTR_COMPUTED_UPPER_STRING_VALUE, String.class,
					basicComputedValueFactory);
			queryFactory.registerComputedAttribute(TestEntity.class, TestDTO.ATTR_COMPUTED_NUMBER_OF_SMALLER_IDS, Long.class,
					complexComputedValueFactory);


			module.addRepository(
					JpaRepositoryConfig.builder(TestEntity.class, TestDTO.class, dtoMapper).build());
			module.addRepository(JpaRepositoryConfig
					.builder(RelatedEntity.class, RelatedDTO.class, new RelatedDTOMapper(entityManager)).build());

			module.addFilter(new JpaRepositoryFilterBase() {

				@Override
				public <T> JpaQuery<T> filterQuery(Object repository, QuerySpec querySpec, JpaQuery<T> query) {
					query.setDistinct(true);
					return query;
				}
			});
		}
	}

	@Test
	public void testDtoMeta() {
		MetaLookup lookup = metaModule.getLookup();
		MetaResource meta = (MetaResource) lookup.getMetaById().get("resources.testDTO");
		MetaKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull(primaryKey);
		Assert.assertEquals(1, primaryKey.getElements().size());
		Assert.assertEquals("id", primaryKey.getElements().get(0).getName());

		MetaAttribute oneRelatedAttr = meta.getAttribute("oneRelatedValue");
		Assert.assertTrue(oneRelatedAttr.isAssociation());
	}

	@Test
	public void testReadAndUpdateFromEntity() throws InstantiationException, IllegalAccessException {
		// create as regular entity
		TestEntity test = new TestEntity();
		test.setId(2L);
		test.setStringValue("test");
		testRepo.create(test);

		// query as regular entity (you may want to disable that in a real application)
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		List<TestEntity> list = testRepo.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		Mockito.verify(dtoMapper, Mockito.times(0)).unmapQuerySpec(Mockito.any(QuerySpec.class));

		// query the mapped DTO
		ResourceRepositoryV2<TestDTO, Serializable> dtoRepo = client.getRepositoryForType(TestDTO.class);
		List<TestDTO> dtos = dtoRepo.findAll(new QuerySpec(TestDTO.class));
		Assert.assertEquals(1, dtos.size());
		TestDTO dto = dtos.get(0);
		Assert.assertEquals(2L, dto.getId().longValue());
		Assert.assertEquals("test", dto.getStringValue());
		Assert.assertEquals("TEST", dto.getComputedUpperStringValue());
		Mockito.verify(dtoMapper, Mockito.times(1)).unmapQuerySpec(Mockito.eq(querySpec));

		// update the mapped dto
		dto.setStringValue("newValue");
		dtoRepo.save(dto);

		// read again
		dto = dtoRepo.findOne(2L, new QuerySpec(TestDTO.class));
		Assert.assertEquals(2L, dto.getId().longValue());
		Assert.assertEquals("newValue", dto.getStringValue());
		Assert.assertEquals("NEWVALUE", dto.getComputedUpperStringValue());

	}

	@Test
	public void testMappedOneRelation() {
		ResourceRepositoryV2<TestDTO, Serializable> testRepo = client.getRepositoryForType(TestDTO.class);
		ResourceRepositoryV2<RelatedDTO, Serializable> relatedRepo = client.getRepositoryForType(RelatedDTO.class);
		RelationshipRepositoryV2<TestDTO, Serializable, RelatedDTO, Serializable> relRepo = client
				.getRepositoryForType(TestDTO.class, RelatedDTO.class);

		TestDTO test = new TestDTO();
		test.setId(2L);
		test.setStringValue("createdDto");
		test = testRepo.create(test);

		RelatedDTO related = new RelatedDTO();
		related.setId(3L);
		related.setStringValue("createdDto");
		related = relatedRepo.create(related);

		relRepo.setRelation(test, related.getId(), TestEntity.ATTR_oneRelatedValue);

		// test relationship access
		RelatedDTO actualRelated = relRepo.findOneTarget(test.getId(), TestEntity.ATTR_oneRelatedValue,
				new QuerySpec(RelatedDTO.class));
		Assert.assertNotNull(actualRelated);
		Assert.assertEquals(related.getId(), actualRelated.getId());

		// test include
		QuerySpec querySpec = new QuerySpec(TestDTO.class);
		querySpec.includeRelation(Arrays.asList(TestEntity.ATTR_oneRelatedValue));
		ResourceList<TestDTO> list = testRepo.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		TestDTO actualTest = list.get(0);
		actualRelated = actualTest.getOneRelatedValue();
		Assert.assertNotNull(actualRelated);
		Assert.assertEquals(related.getId(), actualRelated.getId());
	}

	@Test
	public void testMappedManyRelation() {
		ResourceRepositoryV2<TestDTO, Serializable> testRepo = client.getRepositoryForType(TestDTO.class);
		ResourceRepositoryV2<RelatedDTO, Serializable> relatedRepo = client.getRepositoryForType(RelatedDTO.class);
		RelationshipRepositoryV2<TestDTO, Long, RelatedDTO, Long> relRepo = client
				.getRepositoryForType(TestDTO.class, RelatedDTO.class);

		TestDTO test = new TestDTO();
		test.setId(2L);
		test.setStringValue("createdDto");
		test = testRepo.create(test);

		RelatedDTO related1 = new RelatedDTO();
		related1.setId(1L);
		related1.setStringValue("related1");
		related1 = relatedRepo.create(related1);

		RelatedDTO related2 = new RelatedDTO();
		related2.setId(2L);
		related2.setStringValue("related2");
		related2 = relatedRepo.create(related2);

		Assert.assertEquals(1, testRepo.findAll(new QuerySpec(TestDTO.class)).size());
		relRepo.addRelations(test, Arrays.asList(related1.getId(), related2.getId()), TestEntity.ATTR_manyRelatedValues);
		Assert.assertEquals(1, testRepo.findAll(new QuerySpec(TestDTO.class)).size());

		// test relationship access
		List<RelatedDTO> actualRelatedList = relRepo.findManyTargets(test.getId(), TestEntity.ATTR_manyRelatedValues,
				new QuerySpec(RelatedDTO.class));
		Assert.assertEquals(2, actualRelatedList.size());

		// test include
		Assert.assertEquals(1, testRepo.findAll(new QuerySpec(TestDTO.class)).size());

		// TODO distinct problem in H2 to investigate
		//		QuerySpec querySpec = new QuerySpec(TestDTO.class);
		//		querySpec.includeRelation(Arrays.asList(TestEntity.ATTR_manyRelatedValues));
		//		ResourceList<TestDTO> list = testRepo.findAll(querySpec);
		//		Assert.assertEquals(1, list.size());
		//		TestDTO actualTest = list.get(0);
		//		actualRelatedList = actualTest.getManyRelatedValues();
		//		Assert.assertEquals(2, actualRelatedList.size());

		// test removal
		// TODO DELETE request with body not supported by jersey?
		//		relRepo.removeRelations(test, Arrays.asList(related2.getId()), TestEntity.ATTR_manyRelatedValues);
		//		actualRelatedList = relRepo.findManyTargets(test.getId(), TestEntity.ATTR_manyRelatedValues,
		//				new QuerySpec(RelatedDTO.class));
		//		Assert.assertEquals(1, actualRelatedList.size());
		//		Assert.assertEquals(related1.getId(), actualRelatedList.get(0).getId());
	}

	@Test
	public void testInsertDeleteDto() {
		ResourceRepositoryV2<TestDTO, Serializable> dtoRepo = client.getRepositoryForType(TestDTO.class);

		// create a entity with a DTO and check properly saved
		TestDTO dto = new TestDTO();
		dto.setId(2L);
		dto.setStringValue("createdDto");
		dto = dtoRepo.create(dto);
		Assert.assertEquals("createdDto", dto.getStringValue());
		Assert.assertEquals("CREATEDDTO", dto.getComputedUpperStringValue());

		// check both exists
		ResourceList<TestDTO> dtos = dtoRepo.findAll(new QuerySpec(TestDTO.class));
		Assert.assertEquals(1, dtos.size());
		dto = dtos.get(0);
		Assert.assertEquals("createdDto", dto.getStringValue());
		Assert.assertEquals("CREATEDDTO", dto.getComputedUpperStringValue());

		// test delete
		dtoRepo.delete(dto.getId());
		dtos = dtoRepo.findAll(new QuerySpec(TestDTO.class));
		Assert.assertEquals(0, dtos.size());
	}

	@Test
	public void testSubQueryComputation() {
		ResourceRepositoryV2<TestDTO, Serializable> dtoRepo = client.getRepositoryForType(TestDTO.class);

		int n = 5;
		for (long i = 0; i < n; i++) {
			TestDTO dto = new TestDTO();
			dto.setId(i + 100);
			dto.setStringValue(Long.toString(i));
			dtoRepo.create(dto);
		}

		// select, sort, filter by complex subquery
		QuerySpec querySpec = new QuerySpec(TestDTO.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList(TestDTO.ATTR_COMPUTED_NUMBER_OF_SMALLER_IDS), FilterOperator.LT, 4));

		// TODO enable querySpec parser
		// querySpec.addSort(new SortSpec(Arrays.asList(TestDTO.ATTR_COMPUTED_NUMBER_OF_SMALLER_IDS), Direction.DESC));

		ResourceList<TestDTO> dtos = dtoRepo.findAll(querySpec);
		Assert.assertEquals(4, dtos.size());
		for (int i = 0; i < dtos.size(); i++) {
			TestDTO dto = dtos.get(i);
			int j = i;// 4 - i;
			Assert.assertEquals(100 + j, dto.getId().longValue());
			Assert.assertEquals(j, dto.getComputedNumberOfSmallerIds());
		}
	}
}
