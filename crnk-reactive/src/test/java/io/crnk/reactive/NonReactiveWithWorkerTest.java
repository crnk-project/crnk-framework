package io.crnk.reactive;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.result.ResultFactory;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.reactive.model.SlowResourceRepository;
import io.crnk.reactive.model.SlowTask;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class NonReactiveWithWorkerTest extends ReactiveTestBase {

	private SlowResourceRepository repository = new SlowResourceRepository();

	@Override
	protected void setup(CrnkBoot boot) {
		SimpleModule module = new SimpleModule("slow");
		module.addRepository(repository);
		boot.addModule(module);
	}

	@Test
	public void shouldRunInParallelWithElasticWorkerThreads() {
		RegistryEntry entry = boot.getResourceRegistry().getEntry(SlowTask.class);
		ResourceRepositoryAdapter adapter = entry.getResourceRepository(null);

		QueryAdapter queryAdapter = new QuerySpecAdapter(new QuerySpec(SlowTask.class), boot.getResourceRegistry(), queryContext);

		List<Result<JsonApiResponse>> results = new ArrayList<>();
		int n = 40;
		for (int i = 0; i < n; i++) {
			results.add(adapter.findAll(queryAdapter));
		}

		ResultFactory resultFactory = boot.getModuleRegistry().getResultFactory();
		long s = System.currentTimeMillis();

		Result<List<JsonApiResponse>> zipped = resultFactory.zip(results);
		HttpRequestContextProvider httpRequestContextProvider = boot.getModuleRegistry().getHttpRequestContextProvider();
		zipped = httpRequestContextProvider.attach(zipped);

		List<JsonApiResponse> responses = zipped.get();
		long dt = System.currentTimeMillis() - s;
		Assert.assertEquals(n, responses.size());

		// should incur delay only once since all n items are run in parallel
		int cpuIgnoreMargin = 1000;
		Assert.assertTrue("dt=" + dt, dt < SlowResourceRepository.DELAY + cpuIgnoreMargin);
		Assert.assertTrue("dt=" + dt, dt > SlowResourceRepository.DELAY - cpuIgnoreMargin);

		for (JsonApiResponse response : responses) {
			ResourceList list = (ResourceList) response.getEntity();
			Assert.assertEquals(1, list.size());
		}
	}

}
