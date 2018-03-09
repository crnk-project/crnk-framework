package io.crnk.meta;


import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.utils.Supplier;
import io.crnk.meta.internal.MetaResourceRepositoryImpl;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import org.junit.Before;
import org.junit.Test;

public class MetaResourceRepositoryTest extends AbstractMetaTest {

	private MetaResourceRepositoryImpl<MetaElement> repo;

	private MetaLookup lookup;

	@Before
	public void setup() {
		super.setup();

		ResourceMetaProvider provider = new ResourceMetaProvider();

		lookup = new MetaLookup();
		lookup.setModuleContext(container.getModuleRegistry().getContext());
		lookup.addProvider(provider);
		lookup.initialize();

		repo = new MetaResourceRepositoryImpl(new Supplier<MetaLookup>() {
			@Override
			public MetaLookup get() {
				return lookup;
			}
		}, MetaElement.class);
		repo.setHttpRequestContextProvider(container.getModuleRegistry().getHttpRequestContextProvider());
	}

	@Test(expected = ResourceNotFoundException.class)
	public void checkThrowsNotFoundException() {
		repo.findOne("does not exist", new QuerySpec(MetaElement.class));
	}
}
