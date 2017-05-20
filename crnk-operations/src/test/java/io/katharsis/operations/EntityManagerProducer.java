package io.crnk.operations;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class EntityManagerProducer {

	@PersistenceContext
	private EntityManager em;

	public EntityManager getEntityManager() {
		return em;
	}

}
