package io.crnk.core.mock.repository;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;

import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.mock.repository.util.Relation;


public abstract class AbstractRelationShipRepository<T> {


	abstract ConcurrentMap<Relation<T>, Integer> getRepo();

	public void setRelation(T source, Long targetId, String fieldName) {
		removeRelations(source, Arrays.asList(targetId), fieldName);
		if (targetId != null) {
			getRepo().put(new Relation<>(source, targetId, fieldName), 0);
		}
	}

	public void setRelations(T source, Iterable<Long> targetIds, String fieldName) {
		if (targetIds != null) {
			removeRelations(source, targetIds, fieldName);
			for (Long targetId : targetIds) {
				getRepo().put(new Relation<>(source, targetId, fieldName), 0);
			}
		}
	}

	public void addRelations(T source, Iterable<Long> targetIds, String fieldName) {
		for (Long targetId : targetIds) {
			getRepo().put(new Relation<>(source, targetId, fieldName), 0);
		}
	}

	public void removeRelations(T source, Iterable<Long> targetIds, String fieldName) {
		for (Long targetId : targetIds) {
			Iterator<Relation<T>> iterator = getRepo().keySet().iterator();
			while (iterator.hasNext()) {
				Relation<T> next = iterator.next();
				if (sameResource(next.getSource(), source) && next.getFieldName().equals(fieldName) && next.getTargetId()
						.equals
								(targetId)) {
					iterator.remove();
				}
			}
		}
	}

	private boolean sameResource(T source0, T source1) {
		Object id0 = PropertyUtils.getProperty(source0, "id");
		Object id1 = PropertyUtils.getProperty(source1, "id");
		return id0.equals(id1) && source0.getClass().equals(source1.getClass());
	}

	public void removeRelations(String fieldName) {
		Iterator<Relation<T>> iterator = getRepo().keySet().iterator();
		while (iterator.hasNext()) {
			Relation<T> next = iterator.next();
			if (next.getFieldName().equals(fieldName)) {
				iterator.remove();
			}
		}
	}

}
