package io.crnk.data.jpa.mapping;

import io.crnk.data.jpa.model.RelatedEntity;
import io.crnk.data.jpa.model.dto.RelatedDTO;
import io.crnk.data.jpa.query.Tuple;

import javax.persistence.EntityManager;

public class RelatedDTOMapper implements JpaMapper<RelatedEntity, RelatedDTO> {

    private EntityManager em;

    public RelatedDTOMapper(EntityManager em) {
        this.em = em;
    }

    @Override
    public RelatedDTO map(Tuple tuple) {
        RelatedEntity entity = tuple.get(0, RelatedEntity.class);
        return map(entity);
    }

    public RelatedDTO map(RelatedEntity entity) {
        RelatedDTO dto = new RelatedDTO();
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
