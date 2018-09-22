package io.crnk.jpa.mapping;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.jpa.model.TestEntity;
import io.crnk.jpa.model.dto.TestDTO;
import io.crnk.jpa.query.Tuple;

import javax.persistence.EntityManager;

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

		// real application may or may not choose to do something
		// with the computed attribute. Usually they do not.
		return entity;
	}

	@Override
	public QuerySpec unmapQuerySpec(QuerySpec querySpec) {
		return querySpec;
	}
}
