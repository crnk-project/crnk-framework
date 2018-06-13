package io.crnk.jpa;

import java.util.List;
import javax.persistence.EntityManager;

import io.crnk.client.legacy.ResourceRepositoryStub;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.jpa.internal.JpaResourceInformationProvider;
import io.crnk.jpa.model.RelatedEntity;
import io.crnk.jpa.model.TestEntity;
import io.crnk.legacy.queryParams.QueryParams;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class JpaPartialEntityExposureTest extends AbstractJpaJerseyTest {

	protected ResourceRepositoryStub<TestEntity, Long> testRepo;
	private JpaModule module;

	@Override
	@Before
	public void setup() {
		super.setup();
		testRepo = client.getQueryParamsRepository(TestEntity.class);
	}

	@Override
	protected void setupModule(JpaModule module, boolean server) {
		super.setupModule(module, server);
		this.module = module;
		this.module.removeRepository(RelatedEntity.class);
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testCrud() {
		TestEntity test = new TestEntity();
		test.setId(2L);
		test.setStringValue("test");
		testRepo.save(test);

		List<TestEntity> tests = testRepo.findAll(new QueryParams());
		Assert.assertEquals(1, tests.size());
		test = tests.get(0);
		Assert.assertEquals(2L, test.getId().longValue());
		Assert.assertNull(test.getOneRelatedValue());
		Assert.assertNull(test.getEagerRelatedValue());
		Assert.assertTrue(test.getManyRelatedValues().isEmpty());

		testRepo.delete(test.getId());
		tests = testRepo.findAll(new QueryParams());
		Assert.assertEquals(0, tests.size());
	}

	@Test
	public void testInformationBuilder() {
		EntityManager em = null;
		JpaResourceInformationProvider builder = new JpaResourceInformationProvider(new NullPropertiesProvider(), () -> new OffsetLimitPagingBehavior());
		ResourceInformation info = builder.build(TestEntity.class);
		List<ResourceField> relationshipFields = info.getRelationshipFields();
		Assert.assertEquals(0, relationshipFields.size());
	}

}
