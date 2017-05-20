package io.crnk.jpa.merge;

import io.crnk.jpa.mapping.JpaMapper;
import io.crnk.jpa.model.RelatedEntity;
import io.crnk.jpa.model.TestEntity;
import io.crnk.jpa.query.Tuple;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class MergedResourceMapper implements JpaMapper<TestEntity, MergedResource> {

	private EntityManager em;

	public MergedResourceMapper(EntityManager em) {
		this.em = em;
	}

	@Override
	public MergedResource map(Tuple tuple) {
		TestEntity test = tuple.get(0, TestEntity.class);

		MergedResource merged = new MergedResource();
		merged.setId(test.getId());
		merged.setStringValue(test.getStringValue());
		merged.setOneRelatedValue(map(test.getOneRelatedValue()));
		merged.setManyRelatedValues(map(test.getManyRelatedValues()));
		return merged;
	}

	private RelatedEntity map(RelatedEntity related) {
		RelatedEntity merged = new RelatedEntity();
		merged.setId(related.getId());
		merged.setStringValue(related.getStringValue());
		return merged;
	}

	private List<RelatedEntity> map(List<RelatedEntity> values) {
		List<RelatedEntity> list = new ArrayList<>();
		for (RelatedEntity value : values) {
			list.add(map(value));
		}
		return list;
	}

	@Override
	public TestEntity unmap(MergedResource merged) {
		TestEntity entity = em.find(TestEntity.class, merged.getId());
		if (entity == null) {
			entity = new TestEntity();
			entity.setId(merged.getId());
		}
		entity.setStringValue(merged.getStringValue());
		entity.setOneRelatedValue(unmap(merged.getOneRelatedValue()));
		entity.setManyRelatedValues(unmap(merged.getManyRelatedValues()));

		// in the chosen setup the owning side is RelatedEntity and
		// needs to be updated as well. Might be moved into the setters.
		for (RelatedEntity related : entity.getManyRelatedValues()) {
			related.setTestEntity(entity);
		}

		return entity;
	}

	private List<RelatedEntity> unmap(List<RelatedEntity> values) {
		List<RelatedEntity> list = new ArrayList<>();
		for (RelatedEntity value : values) {
			list.add(unmap(value));
		}
		return list;
	}

	private RelatedEntity unmap(RelatedEntity merged) {
		if (merged != null) {
			RelatedEntity related = em.find(RelatedEntity.class, merged.getId());
			if (related == null) {
				related = new RelatedEntity();
				related.setId(merged.getId());
			}
			related.setStringValue(merged.getStringValue());
			return related;
		} else {
			return null;
		}
	}

}
