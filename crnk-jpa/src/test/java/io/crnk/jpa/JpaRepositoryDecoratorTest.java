package io.crnk.jpa;

import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.repository.decorate.ResourceRepositoryDecoratorBase;
import io.crnk.jpa.JpaRepositoryConfig.Builder;
import io.crnk.jpa.model.RelatedEntity;
import io.crnk.jpa.model.TestEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class JpaRepositoryDecoratorTest extends AbstractJpaJerseyTest {

	private ResourceRepositoryV2<TestEntity, Long> testRepo;

	private ResourceRepositoryDecoratorBase<TestEntity, Long> resourceDecorator;

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
			module.removeRepository(TestEntity.class);

			resourceDecorator = Mockito.spy(new ResourceRepositoryDecoratorBase<TestEntity, Long>() {
			});
			Builder<TestEntity> configBuilder = JpaRepositoryConfig.builder(TestEntity.class);
			configBuilder.setRepositoryDecorator(resourceDecorator);
			module.addRepository(configBuilder.build());
		}
	}

	@Test
	public void test() {
		addTestWithOneRelation();

		Mockito.verify(resourceDecorator, Mockito.timeout(1)).create(Mockito.any(TestEntity.class));
	}

	private TestEntity addTestWithOneRelation() {
		ResourceRepositoryV2<RelatedEntity, Long> relatedRepo = client.getRepositoryForType(RelatedEntity.class);
		RelatedEntity related = new RelatedEntity();
		related.setId(1L);
		related.setStringValue("project");
		relatedRepo.create(related);

		TestEntity test = new TestEntity();
		test.setId(2L);
		test.setStringValue("test");
		testRepo.create(test);

		RelationshipRepositoryV2<TestEntity, Long, RelatedEntity, Long> relRepo = client
				.getRepositoryForType(TestEntity.class, RelatedEntity.class);
		relRepo.setRelation(test, related.getId(), TestEntity.ATTR_oneRelatedValue);

		return test;
	}
}