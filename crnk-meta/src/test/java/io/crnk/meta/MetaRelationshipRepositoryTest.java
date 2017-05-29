package io.crnk.meta;


import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.meta.internal.MetaRelationshipRepository;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaKey;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MetaRelationshipRepositoryTest extends AbstractMetaTest {

	private MetaRelationshipRepository repo;

	private MetaLookup lookup;

	@Before
	public void setup() {
		super.setup();

		ResourceMetaProvider provider = new ResourceMetaProvider();

		lookup = new MetaLookup();
		lookup.setModuleContext(boot.getModuleRegistry().getContext());
		lookup.addProvider(provider);
		lookup.putIdMapping("io.crnk.test.mock.models", "app");
		lookup.putIdMapping("io.crnk.test.mock.repository", "app");
		lookup.initialize();

		repo = new MetaRelationshipRepository(lookup, MetaElement.class, MetaElement.class);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void checkReadOnly1() {
		repo.setRelation(null, null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void checkReadOnly2() {
		repo.setRelations(null, null, null);
	}


	@Test(expected = UnsupportedOperationException.class)
	public void checkReadOnly3() {
		repo.addRelations(null, null, null);
	}


	@Test(expected = UnsupportedOperationException.class)
	public void checkReadOnly4() {
		repo.removeRelations(null, null, null);
	}


	@Test
	public void findOneTargetReturnsResult() {
		MetaResource resource = lookup.getMeta(Task.class, MetaResource.class);

		MetaKey key = (MetaKey) repo.findOneTarget(resource.getId(), "primaryKey", new QuerySpec(MetaElement.class));
		Assert.assertNotNull(key);
		Assert.assertEquals("id", key.getUniqueElement().getName());
	}

	@Test
	public void findOneTargetReturnsNull() {
		MetaResource resource = lookup.getMeta(Task.class, MetaResource.class);
		resource.setPrimaryKey(null);

		MetaKey key = (MetaKey) repo.findOneTarget(resource.getId(), "primaryKey", new QuerySpec(MetaElement.class));
		Assert.assertNull(key);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void findOneTargetReturnsExceptionWhenSourceNotFound() {
		repo.findOneTarget("does not exist", "primaryKey", new QuerySpec(MetaElement.class));
	}

	@Test
	public void findManyTargetReturnsResult() {
		MetaResource resource = lookup.getMeta(Task.class, MetaResource.class);

		ResourceList<MetaElement> children = repo.findManyTargets(resource.getId(), "children", new QuerySpec(MetaElement
				.class));
		Assert.assertNotEquals(0, children.size());
	}


	@Test(expected = IllegalStateException.class)
	public void findManyTargetCannotBeUsedForSingeValuesRelations() {
		MetaResource resource = lookup.getMeta(Task.class, MetaResource.class);
		repo.findManyTargets(resource.getId(), "primaryKey", new QuerySpec(MetaElement
				.class));
	}


	@Test(expected = ResourceNotFoundException.class)
	public void findManyTargetReturnsExceptionWhenSourceNotFound() {
		repo.findManyTargets("does not exist", "children", new QuerySpec(MetaElement.class));
	}

}
