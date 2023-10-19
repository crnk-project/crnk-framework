package io.crnk.data.jpa.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class EntityManagerProducer {

	@PersistenceContext
	private EntityManager em;

	public EntityManager getEntityManager() {
		return em;
	}

}
