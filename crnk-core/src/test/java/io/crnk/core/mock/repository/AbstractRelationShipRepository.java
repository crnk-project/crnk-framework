package io.crnk.core.mock.repository;

import io.crnk.core.mock.repository.util.Relation;

import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;


public abstract class AbstractRelationShipRepository<T> {


	abstract ConcurrentMap<Relation<T>, Integer> getRepo();

	public void setRelation(T source, Long targetId, String fieldName) {
		removeRelations(targetId, fieldName);
		if (targetId != null) {
			getRepo().put(new Relation<>(source, targetId, fieldName), 0);
		}
	}

	public void setRelations(T source, Iterable<Long> targetIds, String fieldName) {
		if (targetIds != null) {
			for (Long targetId : targetIds) {
				removeRelations(targetId, fieldName);
			}
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
				if (next.getFieldName().equals(fieldName) && next.getTargetId().equals(targetId)) {
					iterator.remove();
				}
			}
		}
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

	public void removeRelations(Long targetId, String fieldName) {
		Iterator<Relation<T>> iterator = getRepo().keySet().iterator();
		while (iterator.hasNext()) {
			Relation<T> next = iterator.next();
			if (next.getFieldName().equals(fieldName) && next.getTargetId().equals(targetId)) {
				iterator.remove();
			}
		}
	}

}
