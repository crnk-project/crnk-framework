package io.crnk.jpa;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.ManagedType;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.client.CrnkClient;
import io.crnk.client.http.okhttp.OkHttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapterListenerBase;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.queryspec.DefaultQuerySpecDeserializer;
import io.crnk.jpa.meta.JpaMetaProvider;
import io.crnk.jpa.model.CountryTranslationEntity;
import io.crnk.jpa.model.TestEntity;
import io.crnk.jpa.query.AbstractJpaTest;
import io.crnk.jpa.query.querydsl.QuerydslQueryFactory;
import io.crnk.jpa.util.EntityManagerProducer;
import io.crnk.jpa.util.JpaTestConfig;
import io.crnk.jpa.util.SpringTransactionRunner;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.legacy.queryParams.DefaultQueryParamsParser;
import io.crnk.legacy.queryParams.QueryParamsBuilder;
import io.crnk.meta.MetaModule;
import io.crnk.meta.model.MetaEnumType;
import io.crnk.meta.model.resource.MetaJsonObject;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceBase;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.rs.CrnkFeature;
import io.crnk.test.JerseyTestBase;
import okhttp3.OkHttpClient.Builder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public abstract class AbstractJpaJerseyTest extends JerseyTestBase {

	protected CrnkClient client;

	protected QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

	protected AnnotationConfigApplicationContext context;
	protected MetaModule metaModule;
	private boolean useQuerySpec = true;

	public static void setNetworkTimeout(CrnkClient client, final int timeout, final TimeUnit timeUnit) {
		OkHttpAdapter httpAdapter = (OkHttpAdapter) client.getHttpAdapter();
		httpAdapter.addListener(new OkHttpAdapterListenerBase() {

			@Override
			public void onBuild(Builder builder) {
				builder.readTimeout(timeout, timeUnit);
			}
		});
	}

	@Before
	public void setup() {
		client = new CrnkClient(getBaseUri().toString());
		client.setPushAlways(false);

		JpaModule module = JpaModule.newClientModule();
		setupModule(module, false);
		client.addModule(module);

		MetaModule metaModule = MetaModule.create();
		metaModule.addMetaProvider(new ResourceMetaProvider());
		client.addModule(metaModule);

		setNetworkTimeout(client, 10000, TimeUnit.SECONDS);
	}

	protected void setupModule(JpaModule module, boolean server) {
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();

		SpringTransactionRunner transactionRunner = context.getBean(SpringTransactionRunner.class);
		transactionRunner.doInTransaction(new Callable<Object>() {

			@Override
			public Object call() throws Exception {
				EntityManager em = context.getBean(EntityManagerProducer.class).getEntityManager();
				AbstractJpaTest.clear(em);
				return null;
			}
		});

		if (context != null)
			context.destroy();
	}

	@Override
	protected Application configure() {
		return new TestApplication();
	}

	@ApplicationPath("/")
	private class TestApplication extends ResourceConfig {

		public TestApplication() {
			property(CrnkProperties.RESOURCE_SEARCH_PACKAGE, "io.crnk.test.mock");

			Assert.assertNull(context);

			context = new AnnotationConfigApplicationContext(JpaTestConfig.class);
			context.start();
			EntityManagerFactory emFactory = context.getBean(EntityManagerFactory.class);
			EntityManager em = context.getBean(EntityManagerProducer.class).getEntityManager();
			SpringTransactionRunner transactionRunner = context.getBean(SpringTransactionRunner.class);

			CrnkFeature feature;
			if (useQuerySpec) {
				feature = new CrnkFeature(new ObjectMapper(), new QueryParamsBuilder(new DefaultQueryParamsParser()), new SampleJsonServiceLocator());
			} else {
				feature = new CrnkFeature(new ObjectMapper(), new DefaultQuerySpecDeserializer(), new SampleJsonServiceLocator());
			}

			JpaModule module = JpaModule.newServerModule(em, transactionRunner);
			module.setQueryFactory(QuerydslQueryFactory.newInstance());
			setupModule(module, true);

			Set<ManagedType<?>> managedTypes = emFactory.getMetamodel().getManagedTypes();
			for (ManagedType<?> managedType : managedTypes) {
				Class<?> managedJavaType = managedType.getJavaType();
				if (managedJavaType.getAnnotation(Entity.class) != null && managedJavaType != CountryTranslationEntity.class) {
					if (!module.hasRepository(managedJavaType)) {
						module.addRepository(JpaRepositoryConfig.builder(managedJavaType).build());
					}
				}
			}

			feature.addModule(module);

			metaModule = MetaModule.create();
			metaModule.addMetaProvider(new ResourceMetaProvider());
			metaModule.addMetaProvider(new JpaMetaProvider(emFactory));
			feature.addModule(metaModule);

			String managementId = "io.crnk.jpa.resource";
			String dataId = TestEntity.class.getPackage().getName();
			metaModule.putIdMapping(dataId, MetaJsonObject.class, managementId);
			metaModule.putIdMapping(dataId, MetaResourceBase.class, managementId);
			metaModule.putIdMapping(dataId, MetaResource.class, managementId);
			metaModule.putIdMapping(dataId, MetaEnumType.class, managementId);

			register(feature);

		}
	}

}
