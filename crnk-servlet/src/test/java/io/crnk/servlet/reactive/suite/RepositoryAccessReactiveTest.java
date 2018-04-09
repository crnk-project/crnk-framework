package io.crnk.servlet.reactive.suite;

import io.crnk.servlet.resource.ReactiveServletTestApplication;
import io.crnk.servlet.resource.ReactiveServletTestContainer;
import io.crnk.test.suite.BasicRepositoryAccessTestBase;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReactiveServletTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RepositoryAccessReactiveTest extends BasicRepositoryAccessTestBase {

	@Autowired
	public void setTestContainer(ReactiveServletTestContainer testContainer) {
		this.testContainer = testContainer;
	}
}
