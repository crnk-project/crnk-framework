package io.crnk.jpa.mapping;

import io.crnk.jpa.model.RelatedEntity;
import io.crnk.jpa.model.dto.RelatedDTO;
import io.crnk.jpa.query.Tuple;

import javax.persistence.EntityManager;

public class RelatedDTOMapper implements JpaMapper<RelatedEntity, RelatedDTO> {

	private EntityManager em;

	public RelatedDTOMapper(EntityManager em) {
		this.em = em;
	}

	@Override
	public RelatedDTO map(Tuple tuple) {
		RelatedDTO dto = new RelatedDTO();
		RelatedEntity entity = tuple.get(0, RelatedEntity.class);
		dto.setId(entity.getId());
		dto.setStringValue(entity.getStringValue());
		return dto;
	}

	@Override
	public RelatedEntity unmap(RelatedDTO dto) {
		RelatedEntity entity = em.find(RelatedEntity.class, dto.getId());
		if (entity == null) {
			entity = new RelatedEntity();
			entity.setId(dto.getId());
		}
		entity.setStringValue(dto.getStringValue());
		return entity;
	}
}
