package io.crnk.example.springboot.simple;

import com.jayway.restassured.RestAssured;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.example.springboot.SpringBootExampleApplication;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpStatus.OK;

/**
 * Makes sure links are generated based on {@link io.crnk.core.engine.url.ServiceUrlProvider} implementation
 */
@SpringBootTest(
	classes = {
		SpringBootExampleApplication.class,
		SpringBootCustomServiceUrlProviderApplicationTests.TestConfiguration.class
	},
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class SpringBootCustomServiceUrlProviderApplicationTests extends BaseTest {

	@Configuration
	static class TestConfiguration {

		@Component
		class TenantCrnkBootInitBeanPostProcessor implements BeanPostProcessor {

			@Override
			public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
				if (bean instanceof CrnkBoot) {
					((CrnkBoot) bean).getModuleRegistry().getHttpRequestContextProvider().setServiceUrlProvider(() -> "http://some.org");
				}

				return bean;
			}
		}
	}

    @Test
    public void testFindMany() {
	    RestAssured.given().contentType("application/json").when().get("/api/tasks")
			    .then()
			    .body("links.self", equalTo("http://some.org/tasks"))
			    .body("data[0].links.self", equalTo("http://some.org/tasks/1"))
			    .body("data[1].links.self", equalTo("http://some.org/tasks/2"))
			    .body("data[2].links.self", equalTo("http://some.org/tasks/3"));
    }

	@Test
	public void testFindOne() {
		RestAssured.given().contentType("application/json").when().get("/api/tasks/1")
				.then()
				.body("links.self", equalTo("http://some.org/tasks/1"));
	}
}
