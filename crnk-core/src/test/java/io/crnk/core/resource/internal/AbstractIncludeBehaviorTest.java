package io.crnk.core.resource.internal;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.internal.document.mapper.IncludeBehavior;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.mock.models.HierarchicalTask;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractIncludeBehaviorTest extends AbstractDocumentMapperTest {

	protected HierarchicalTask h;
	protected HierarchicalTask h0;
	protected HierarchicalTask h1;
	protected HierarchicalTask h11;

	@Override
	protected PropertiesProvider getPropertiesProvider() {
		return new PropertiesProvider() {

			@Override
			public String getProperty(String key) {
				if (key.equals(CrnkProperties.INCLUDE_BEHAVIOR))
					return IncludeBehavior.PER_TYPE.toString();
				return null;
			}
		};
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Before
	public void setup() {

		super.setup();

		ResourceRepositoryAdapter hierarchicalTaskRepository = resourceRegistry.findEntry(HierarchicalTask.class).getResourceRepository(null);

		h = new HierarchicalTask();
		h.setId(1L);
		h.setName("");

		h0 = new HierarchicalTask();
		h0.setId(2L);
		h0.setName("0");
		h0.setParent(h);

		h1 = new HierarchicalTask();
		h1.setId(3L);
		h1.setName("1");
		h1.setParent(h);

		h11 = new HierarchicalTask();
		h11.setId(4L);
		h11.setName("11");
		h11.setParent(h1);

		h.setChildren(Arrays.asList(h0, h1));
		h0.setChildren(new ArrayList<HierarchicalTask>());
		h1.setChildren(Arrays.asList(h11));
		h11.setChildren(new ArrayList<HierarchicalTask>());

		hierarchicalTaskRepository.create(h, null);
		hierarchicalTaskRepository.create(h0, null);
		hierarchicalTaskRepository.create(h1, null);
		hierarchicalTaskRepository.create(h11, null);
	}
}
