package io.crnk.operations;

import io.crnk.client.CrnkClient;
import io.crnk.client.action.JerseyActionStubFactory;
import io.crnk.client.http.okhttp.OkHttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapterListenerBase;
import io.crnk.jpa.JpaModule;
import io.crnk.jpa.JpaRepositoryConfig;
import io.crnk.jpa.meta.JpaMetaProvider;
import io.crnk.jpa.query.JpaQueryFactory;
import io.crnk.jpa.query.JpaQueryFactoryContext;
import io.crnk.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.MetaModule;
import io.crnk.meta.provider.MetaPartition;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.operations.model.MovieEntity;
import io.crnk.operations.model.PersonEntity;
import io.crnk.operations.server.OperationsModule;
import io.crnk.operations.server.TransactionOperationFilter;
import io.crnk.rs.CrnkFeature;
import io.crnk.spring.internal.SpringServiceDiscovery;
import io.crnk.spring.jpa.SpringTransactionRunner;
import io.crnk.test.JerseyTestBase;
import io.crnk.validation.ValidationModule;
import okhttp3.OkHttpClient.Builder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.ManagedType;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public abstract class AbstractOperationsTest extends JerseyTestBase {

	protected CrnkClient client;

	protected AnnotationConfigApplicationContext context;

	protected OperationsModule operationsModule;

	public static void setNetworkTimeout(CrnkClient client, final int timeout, final TimeUnit timeUnit) {
		OkHttpAdapter httpAdapter = (OkHttpAdapter) client.getHttpAdapter();
		httpAdapter.addListener(new OkHttpAdapterListenerBase() {

			@Override
			public void onBuild(Builder builder) {
				builder.readTimeout(timeout, timeUnit);
			}
		});
	}

	public static void clear(EntityManager em) {
		clear(em, JpaCriteriaQueryFactory.newInstance());
	}

	public static void clear(final EntityManager em, JpaQueryFactory factory) {
		factory.initalize(new JpaQueryFactoryContext() {
			@Override
			public MetaPartition getMetaPartition() {
				MetaLookup metaLookup = new MetaLookup();
				JpaMetaProvider metaProvider = new JpaMetaProvider(em.getEntityManagerFactory());
				metaLookup.addProvider(metaProvider);
				metaLookup.initialize();
				return metaProvider.getPartition();
			}

			@Override
			public EntityManager getEntityManager() {
				return em;
			}
		});
		clear(em, factory.query(MovieEntity.class).buildExecutor().getResultList());
		clear(em, factory.query(PersonEntity.class).buildExecutor().getResultList());
		em.flush();
		em.clear();
	}

	private static void clear(EntityManager em, List<?> list) {
		for (Object obj : list) {
			em.remove(obj);
		}
	}

	@Before
	public void setup() {
		clear();
		client = new CrnkClient(getBaseUri().toString());
		client.setActionStubFactory(JerseyActionStubFactory.newInstance());
		client.getHttpAdapter().setReceiveTimeout(10000000, TimeUnit.MILLISECONDS);

		MetaModule metaModule = MetaModule.create();
		metaModule.addMetaProvider(new ResourceMetaProvider());
		client.addModule(metaModule);

		JpaModule module = JpaModule.newClientModule();
		setupModule(module, false);
		client.addModule(module);

		setNetworkTimeout(client, 10000, TimeUnit.SECONDS);
	}

	protected MovieEntity newMovie(String title) {
		MovieEntity movie = new MovieEntity();
		movie.setId(UUID.randomUUID());
		movie.setImdbId(title);
		movie.setTitle(title);
		return movie;
	}

	protected PersonEntity newPerson(String name) {
		PersonEntity person = new PersonEntity();
		person.setId(UUID.randomUUID());
		person.setName(name);
		return person;
	}

	protected void setupModule(JpaModule module, boolean server) {
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();

		clear();

		if (context != null) {
			context.destroy();
		}
	}

	protected void clear() {
		SpringTransactionRunner transactionRunner = context.getBean(SpringTransactionRunner.class);
		transactionRunner.doInTransaction(() -> {
			EntityManager em = context.getBean(EntityManagerProducer.class).getEntityManager();
			clear(em);
			return null;
		});
	}

	@Override
	protected Application configure() {
		return new TestApplication();
	}

	@ApplicationPath("/")
	private class TestApplication extends ResourceConfig {


		public TestApplication() {
			Assert.assertNull(context);

			context = new AnnotationConfigApplicationContext(io.crnk.operations.OperationsTestConfig.class);
			context.start();
			EntityManagerFactory emFactory = context.getBean(EntityManagerFactory.class);
			EntityManager em = context.getBean(io.crnk.operations.EntityManagerProducer.class).getEntityManager();
			SpringServiceDiscovery serviceDiscovery = context.getBean(SpringServiceDiscovery.class);
			SpringTransactionRunner transactionRunner = context.getBean(SpringTransactionRunner.class);

			CrnkFeature feature = new CrnkFeature();
			feature.getBoot().setServiceDiscovery(serviceDiscovery);

			JpaModule jpaModule = JpaModule.newServerModule(em, transactionRunner);
			setupModule(jpaModule, true);

			Set<ManagedType<?>> managedTypes = emFactory.getMetamodel().getManagedTypes();
			for (ManagedType<?> managedType : managedTypes) {
				Class<?> managedJavaType = managedType.getJavaType();
				if (managedJavaType.getAnnotation(Entity.class) != null) {
					if (!jpaModule.hasRepository(managedJavaType)) {
						jpaModule.addRepository(JpaRepositoryConfig.builder(managedJavaType).build());
					}
				}
			}

			operationsModule = OperationsModule.create();

			// tag::transaction[]
			operationsModule.addFilter(new TransactionOperationFilter());
			// end::transaction[]

			feature.addModule(jpaModule);
			feature.addModule(operationsModule);
			feature.addModule(ValidationModule.create());
			register(feature);
		}
	}

}
