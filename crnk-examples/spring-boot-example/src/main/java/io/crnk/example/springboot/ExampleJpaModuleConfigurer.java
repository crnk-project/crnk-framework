package io.crnk.example.springboot;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;

import io.crnk.example.springboot.domain.model.ScheduleDto;
import io.crnk.example.springboot.domain.model.ScheduleEntity;
import io.crnk.jpa.JpaModuleConfig;
import io.crnk.jpa.JpaRepositoryConfig;
import io.crnk.jpa.mapping.JpaMapper;
import io.crnk.jpa.query.Tuple;
import io.crnk.jpa.query.criteria.JpaCriteriaExpressionFactory;
import io.crnk.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.crnk.spring.setup.boot.jpa.JpaModuleConfigurer;
import org.springframework.stereotype.Component;

@Component
public class ExampleJpaModuleConfigurer implements JpaModuleConfigurer {

	@PersistenceContext
	private EntityManager em;

	/**
	 * Expose JPA entities as repositories.
	 *
	 * @return module
	 */

	@Override
	public void configure(JpaModuleConfig config) {
		// directly expose entity
		config.addRepository(JpaRepositoryConfig.builder(ScheduleEntity.class).build());

		// additionally expose entity as a mapped dto
		config.addRepository(
				JpaRepositoryConfig.builder(ScheduleEntity.class, ScheduleDto.class, new ScheduleMapper()).build());

		JpaCriteriaQueryFactory queryFactory = (JpaCriteriaQueryFactory) config.getQueryFactory();

		// register a computed a attribute
		// you may consider QueryDSL or generating the Criteria query objects.
		queryFactory.registerComputedAttribute(ScheduleEntity.class, "upperName", String.class,
				new JpaCriteriaExpressionFactory<From<?, ScheduleEntity>>() {

					@SuppressWarnings({ "rawtypes", "unchecked" })
					@Override
					public Expression<String> getExpression(From<?, ScheduleEntity> entity, CriteriaQuery<?> query) {
						CriteriaBuilder builder = em.getCriteriaBuilder();
						return builder.upper((Expression) entity.get("name"));
					}
				});
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
