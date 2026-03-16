package io.crnk.data.jpa.mapping;

import io.crnk.data.jpa.model.SequenceEntity;
import io.crnk.data.jpa.model.dto.SequenceDTO;
import io.crnk.data.jpa.query.Tuple;

import javax.persistence.EntityManager;

public class SequenceDTOMapper implements JpaMapper<SequenceEntity, SequenceDTO> {

	private EntityManager em;

	public SequenceDTOMapper(EntityManager em) {
		this.em = em;
	}

	@Override
	public SequenceDTO map(Tuple tuple) {
		SequenceEntity entity = tuple.get(0, SequenceEntity.class);
		SequenceDTO dto = new SequenceDTO();
		dto.setId(entity.getId());
		dto.setStringValue(entity.getStringValue());
		return dto;
	}

	@Override
	public SequenceEntity unmap(SequenceDTO dto) {
		SequenceEntity entity;
		Long id = dto.getId();
		if (id == null) {
			entity = new SequenceEntity();
		} else {
			entity = em.find(SequenceEntity.class, id);
		}
		entity.setStringValue(dto.getStringValue());
		return entity;
	}
}
