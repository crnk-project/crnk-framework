package io.crnk.data.jpa.mapping;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.data.jpa.model.RelatedEntity;
import io.crnk.data.jpa.model.TestEntity;
import io.crnk.data.jpa.model.dto.RelatedDTO;
import io.crnk.data.jpa.model.dto.TestDTO;
import io.crnk.data.jpa.query.Tuple;

import jakarta.persistence.EntityManager;

/**
 * you may consider the use of MapStructor or similar tooling to
 * automate this in a real application.
 */
public class TestDTOMapper implements JpaMapper<TestEntity, TestDTO> {

	private EntityManager em;

	public TestDTOMapper(EntityManager em) {
		this.em = em;
	}

	@Override
	public TestDTO map(Tuple tuple) {
		TestDTO dto = new TestDTO();
		TestEntity entity = tuple.get(0, TestEntity.class);
		dto.setId(entity.getId());
		dto.setStringValue(entity.getStringValue());
		dto.setComputedUpperStringValue(tuple.get("computedUpperStringValue", String.class));
		dto.setComputedNumberOfSmallerIds(tuple.get("computedNumberOfSmallerIds", Long.class));

		RelatedEntity oneRelatedValue = entity.getOneRelatedValue();
		if(oneRelatedValue != null){
			RelatedDTOMapper relatedMapper = new RelatedDTOMapper(em);
			dto.setOneRelatedValue(relatedMapper.map(oneRelatedValue));
		}
		return dto;
	}

	@Override
	public TestEntity unmap(TestDTO dto) {
		TestEntity entity = em.find(TestEntity.class, dto.getId());
		if (entity == null) {
			entity = new TestEntity();
			entity.setId(dto.getId());
		}
		entity.setStringValue(dto.getStringValue());

		RelatedDTO oneRelatedValue = dto.getOneRelatedValue();
		if(oneRelatedValue != null){
			RelatedEntity relatedEntity = em.find(RelatedEntity.class, oneRelatedValue.getId());
			entity.setOneRelatedValue(relatedEntity);
		}

		// real application may or may not choose to do something
		// with the computed attribute. Usually they do not.
		return entity;
	}

	@Override
	public QuerySpec unmapQuerySpec(QuerySpec querySpec) {
		return querySpec;
	}
}
