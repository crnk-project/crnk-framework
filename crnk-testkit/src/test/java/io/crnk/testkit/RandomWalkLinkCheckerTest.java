package io.crnk.testkit;


import java.util.Set;

import io.crnk.client.http.inmemory.InMemoryHttpAdapter;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.home.HomeModule;
import io.crnk.test.mock.models.BulkTask;
import io.crnk.test.mock.models.HistoricTask;
import org.junit.Assert;
import org.junit.Test;

public class RandomWalkLinkCheckerTest {

	@Test
	public void test() {
		SimpleModule testModule = new SimpleModule("test");
		testModule.addRepository(new InMemoryResourceRepository<>(BulkTask.class));
		testModule.addRepository(new InMemoryResourceRepository<>(HistoricTask.class));

		CrnkBoot boot = new CrnkBoot();
		boot.addModule(HomeModule.create());
		boot.addModule(testModule);
		boot.boot();

		String baseUrl = "http://localhost";
		InMemoryHttpAdapter adapter = new InMemoryHttpAdapter(boot, baseUrl);

		RandomWalkLinkChecker checker = new RandomWalkLinkChecker(adapter);
		checker.addStartUrl(baseUrl + "/");
		Set<String> visited = checker.performCheck();
		Assert.assertTrue(visited.contains("http://localhost/tasks/history"));
		Assert.assertTrue(visited.contains("http://localhost/"));
		Assert.assertTrue(visited.contains("http://localhost/bulkTasks"));
		Assert.assertTrue(visited.contains("http://localhost/tasks/"));
		Assert.assertEquals(4, visited.size());
	}

}
