package io.crnk.client.suite;

import io.crnk.client.internal.proxy.ObjectProxy;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;
import io.crnk.test.suite.InheritanceAccessTestBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class InheritanceClientTest extends InheritanceAccessTestBase {

	public InheritanceClientTest() {
		ClientTestContainer testContainer = new ClientTestContainer();
		this.testContainer = testContainer;
	}

	@Test
	public void testIncludePoloymorphCollectionWithoutInclude() {
		doTestIncludePoloymorphCollection(false);
	}

	@Test
	public void testIncludePoloymorphCollectionWithInclude() {
		doTestIncludePoloymorphCollection(true);
	}

	private void doTestIncludePoloymorphCollection(boolean include) {
		QuerySpec querySpec = new QuerySpec(Project.class);
		if (include) {
			querySpec.includeRelation(Arrays.asList("tasks"));
		}
		List<Project> projects = projectRepo.findAll(querySpec);
		Assert.assertEquals(1, projects.size());
		Project project = projects.get(0);

		List<Task> tasks = project.getTasks();
		if (include) {
			Assert.assertFalse(tasks instanceof ObjectProxy);
		} else {
			ObjectProxy proxy = (ObjectProxy) tasks;
			Assert.assertFalse(proxy.isLoaded());
		}

		if (tasks.get(0).getName().equals("baseTask")) {
			Assert.assertEquals("baseTask", tasks.get(0).getName());
			Assert.assertEquals("taskSubType", tasks.get(1).getName());
		} else {
			Assert.assertEquals("baseTask", tasks.get(1).getName());
			Assert.assertEquals("taskSubType", tasks.get(0).getName());
		}
	}

}
