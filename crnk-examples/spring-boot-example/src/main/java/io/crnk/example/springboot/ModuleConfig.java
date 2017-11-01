package io.crnk.example.springboot;


import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;

import brave.Tracing;
import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.example.springboot.domain.model.ScheduleDto;
import io.crnk.example.springboot.domain.model.ScheduleEntity;
import io.crnk.home.HomeModule;
import io.crnk.jpa.JpaModule;
import io.crnk.jpa.JpaRepositoryConfig;
import io.crnk.jpa.mapping.JpaMapper;
import io.crnk.jpa.query.Tuple;
import io.crnk.jpa.query.criteria.JpaCriteriaExpressionFactory;
import io.crnk.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.crnk.meta.MetaModule;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.monitor.brave.BraveServerModule;
import io.crnk.ui.UIModule;
import io.crnk.ui.UIModuleConfig;
import io.crnk.validation.ValidationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.Endpoint;
import zipkin2.reporter.Reporter;

@Configuration
public class ModuleConfig {

	@Autowired
	private EntityManager em;

	@Autowired
	private TransactionRunner transactionRunner;

	/**
	 * Bean Validation
	 *
	 * @return module
	 */
	@Bean
	public ValidationModule validationModule() {
		return ValidationModule.newInstance();
	}

	/**
	 * Provide list of repositories in root document.
	 *
	 * @return module
	 */
	@Bean
	public HomeModule homeModule() {
		return HomeModule.create();
	}

	/**
	 * Provide list of repositories in root document.
	 *
	 * @return module
	 */
	@Bean
	public UIModule uiModule() {
		return UIModule.create(new UIModuleConfig());
	}

	/**
	 * Makes meta data of all repositories available as repositories.
	 *
	 * @return module
	 */
	@Bean
	public MetaModule metaModule() {
		MetaModule metaModule = MetaModule.create();
		metaModule.addMetaProvider(new ResourceMetaProvider());
		return metaModule;
	}

	/**
	 * Basic monitoring setup with Brave
	 *
	 * @return module
	 */
	@Bean
	public BraveServerModule braveModule() {
		String serviceName = "exampleApp";
		Endpoint localEndpoint = Endpoint.newBuilder().serviceName(serviceName).build();
		Tracing tracing = Tracing.newBuilder().localEndpoint(localEndpoint)
				.spanReporter(Reporter.CONSOLE).build();
		return BraveServerModule.create(tracing);
	}

	/**
	 * Expose JPA entities as repositories.
	 *
	 * @return module
	 */
	@Bean
	public JpaModule jpaModule() {
		JpaModule module = JpaModule.newServerModule(em, transactionRunner);

		// directly expose entity
		module.addRepository(JpaRepositoryConfig.builder(ScheduleEntity.class).build());

		// additionally expose entity as a mapped dto
		module.addRepository(JpaRepositoryConfig.builder(ScheduleEntity.class, ScheduleDto.class, new ScheduleMapper()).build());
		JpaCriteriaQueryFactory queryFactory = (JpaCriteriaQueryFactory) module.getQueryFactory();

		// register a computed a attribute
		// you may consider QueryDSL or generating the Criteria query objects.
		queryFactory.registerComputedAttribute(ScheduleEntity.class, "upperName", String.class,
				new JpaCriteriaExpressionFactory<From<?, ScheduleEntity>>() {

					@SuppressWarnings({"rawtypes", "unchecked"})
					@Override
					public Expression<String> getExpression(From<?, ScheduleEntity> entity, CriteriaQuery<?> query) {
						CriteriaBuilder builder = em.getCriteriaBuilder();
						return builder.upper((Expression) entity.get("name"));
					}
				});
		return module;
	}

	public final class LoggingReporter implements Reporter<zipkin.Span> {

		private Logger logger = LoggerFactory.getLogger("springboot.simple.traceReporter");

		@Override
		public void report(zipkin.Span span) {
			logger.info(span.toString());
		}
	}

	class ScheduleMapper implements JpaMapper<ScheduleEntity, ScheduleDto> {

		@Override
		public ScheduleDto map(Tuple tuple) {
			ScheduleDto dto = new ScheduleDto();

			// first entry in tuple is the queried entity (if not configured otherwise)
			ScheduleEntity entity = tuple.get(0, ScheduleEntity.class);
			dto.setId(entity.getId());
			dto.setName(entity.getName());

			// computed attribute available as additional tuple entry
			dto.setUpperName(tuple.get(1, String.class));
			return dto;
		}

		@Override
		public ScheduleEntity unmap(ScheduleDto dto) {
			// get entity from database if already there
			ScheduleEntity entity = em.find(ScheduleEntity.class, dto.getId());
			if (entity == null) {
				entity = new ScheduleEntity();
				entity.setId(dto.getId());
			}
			entity.setName(dto.getName());
			return entity;
		}

	}
}
