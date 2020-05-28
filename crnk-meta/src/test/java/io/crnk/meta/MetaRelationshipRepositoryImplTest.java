package io.crnk.meta;


import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.utils.Supplier;
import io.crnk.meta.internal.MetaRelationshipRepositoryImpl;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaKey;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MetaRelationshipRepositoryImplTest extends AbstractMetaTest {

	private MetaRelationshipRepositoryImpl repo;

	private MetaLookupImpl lookup;

	@Before
	public void setup() {
		super.setup();

		resourceProvider = new ResourceMetaProvider();

		lookup = new MetaLookupImpl();
		lookup.setModuleContext(container.getModuleRegistry().getContext());
		lookup.addProvider(resourceProvider);
		lookup.initialize();

		repo = new MetaRelationshipRepositoryImpl(new Supplier<MetaLookup>() {
			@Override
			public MetaLookup get() {
				return lookup;
			}
		}, MetaElement.class, MetaElement.class);
		repo.setHttpRequestContextProvider(container.getModuleRegistry().getHttpRequestContextProvider());
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
		MetaResource resource = resourceProvider.getMeta(Task.class);

		MetaKey key = (MetaKey) repo.findOneTarget(resource.getId(), "primaryKey", new QuerySpec(MetaElement.class));
		Assert.assertNotNull(key);
		Assert.assertEquals("id", key.getUniqueElement().getName());
	}

	@Test
	public void findOneTargetReturnsNull() {
		MetaResource resource = resourceProvider.getMeta(Task.class);
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
		MetaResource resource = resourceProvider.getMeta(Task.class);

		ResourceList<MetaElement> children = repo.findManyTargets(resource.getId(), "children", new QuerySpec(MetaElement
				.class));
		Assert.assertNotEquals(0, children.size());
	}


	@Test(expected = ClassCastException.class)
	public void findManyTargetCannotBeUsedForSingeValuesRelations() {
		MetaResource resource = resourceProvider.getMeta(Task.class);
		repo.findManyTargets(resource.getId(), "primaryKey", new QuerySpec(MetaElement
				.class));
	}


	@Test(expected = ResourceNotFoundException.class)
	public void findManyTargetReturnsExceptionWhenSourceNotFound() {
		repo.findManyTargets("does not exist", "children", new QuerySpec(MetaElement.class));
	}

}
