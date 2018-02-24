package io.crnk.core.queryspec;

import io.crnk.core.engine.information.repository.RepositoryMethodAccess;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.repository.ResourceRepositoryInformationImpl;
import io.crnk.core.engine.internal.registry.ResourceRegistryImpl;
import io.crnk.core.engine.registry.DefaultResourceRegistryPart;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.legacy.internal.DirectResponseResourceEntry;
import io.crnk.legacy.queryParams.params.IncludedFieldsParams;
import io.crnk.legacy.queryParams.params.IncludedRelationsParams;
import io.crnk.legacy.queryParams.params.TypedParams;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class QuerySpecAdapterTest {

	@Test
	public void test() {
		ModuleRegistry moduleRegistry = new ModuleRegistry();
		ResourceRegistry resourceRegistry = new ResourceRegistryImpl(new DefaultResourceRegistryPart(), moduleRegistry);
		ResourceInformation resourceInformation =
				new ResourceInformation(moduleRegistry.getTypeParser(), Task.class, "tasks", null, null, new OffsetLimitPagingBehavior());
		resourceRegistry.addEntry(new RegistryEntry(new DirectResponseResourceEntry(null, new ResourceRepositoryInformationImpl("tasks",
						resourceInformation, RepositoryMethodAccess.ALL))));

		QuerySpec spec = new QuerySpec(Task.class);
		spec.includeField(Arrays.asList("test"));
		spec.includeRelation(Arrays.asList("relation"));
		QuerySpecAdapter adapter = new QuerySpecAdapter(spec, resourceRegistry);
		Assert.assertEquals(Task.class, adapter.getResourceInformation().getResourceClass());
		Assert.assertEquals(spec, adapter.getQuerySpec());

		TypedParams<IncludedFieldsParams> includedFields = adapter.getIncludedFields();
		IncludedFieldsParams includedFieldsParams = includedFields.getParams().get("tasks");
		Assert.assertEquals(1, includedFieldsParams.getParams().size());
		Assert.assertEquals("test", includedFieldsParams.getParams().iterator().next());
		TypedParams<IncludedRelationsParams> includedRelations = adapter.getIncludedRelations();
		IncludedRelationsParams includedRelationsParams = includedRelations.getParams().get("tasks");
		Assert.assertEquals(1, includedRelationsParams.getParams().size());
		Assert.assertEquals("relation", includedRelationsParams.getParams().iterator().next().getPath());

		Assert.assertEquals(new OffsetLimitPagingSpec(), adapter.getPagingSpec());
	}
}
