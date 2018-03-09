package io.crnk.servlet.resource;

import io.crnk.client.CrnkClient;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.result.ResultFactory;
import io.crnk.reactive.ReactiveModule;
import io.crnk.reactive.internal.MonoResultFactory;
import io.crnk.servlet.AsyncCrnkServlet;
import io.crnk.test.mock.ClientTestModule;
import io.crnk.test.mock.reactive.ReactiveTestModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@RestController
@SpringBootApplication
public class ReactiveServletTestApplication implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

	private int port;

	private CrnkClient client;

	private AsyncCrnkServlet servlet = new AsyncCrnkServlet();

	private ReactiveTestModule testModule = new ReactiveTestModule();

	@Override
	public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
		port = event.getEmbeddedServletContainer().getPort();
		client = new CrnkClient("http://localhost:" + port + "/api");
		client.addModule(new ClientTestModule());
	}

	public static void main(String[] args) {
		SpringApplication.run(ReactiveServletTestApplication.class, args);
	}


	@Bean
	public ReactiveServletTestContainer testContainer() {
		return new ReactiveServletTestContainer(testModule, () -> client);
	}

	// tag::reactive[]
	@Bean
	public ServletRegistrationBean reactiveServlet() {
		ResultFactory resultFactory = new MonoResultFactory();

		servlet.getBoot().getModuleRegistry().setResultFactory(resultFactory);
		servlet.getBoot().addModule(new ReactiveModule());
		servlet.getBoot().addModule(testModule);

		ServletRegistrationBean bean = new ServletRegistrationBean(servlet, "/api/*");
		bean.setLoadOnStartup(1);
		return bean;
	}
	// end::reactive[]

	@Bean
	public CrnkBoot crnkBoot() {
		return servlet.getBoot();
	}
}
