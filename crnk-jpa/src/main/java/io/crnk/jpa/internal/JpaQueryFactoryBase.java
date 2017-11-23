package io.crnk.jpa.internal;

import io.crnk.jpa.internal.query.ComputedAttributeRegistryImpl;
import io.crnk.jpa.query.ComputedAttributeRegistry;
import io.crnk.jpa.query.JpaQueryFactory;
import io.crnk.jpa.query.JpaQueryFactoryContext;

import javax.persistence.EntityManager;

public abstract class JpaQueryFactoryBase implements JpaQueryFactory {

	protected EntityManager em;

	protected ComputedAttributeRegistryImpl computedAttrs = new ComputedAttributeRegistryImpl();

	protected JpaQueryFactoryContext context;


	@Override
	public void initalize(JpaQueryFactoryContext context) {
		this.em = context.getEntityManager();
		this.computedAttrs.init(context);
		this.context = context;
	}

	public EntityManager getEntityManager() {
		return em;
	}

	public ComputedAttributeRegistry getComputedAttributes() {
		return computedAttrs;
	}
}
