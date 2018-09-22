package io.crnk.jpa.internal;

import io.crnk.jpa.internal.query.ComputedAttributeRegistryImpl;
import io.crnk.jpa.meta.JpaMetaProvider;
import io.crnk.jpa.query.ComputedAttributeRegistry;
import io.crnk.jpa.query.JpaQueryFactory;
import io.crnk.jpa.query.JpaQueryFactoryContext;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.provider.MetaPartition;

import javax.persistence.EntityManager;
import java.util.Collections;

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

	protected static JpaQueryFactoryContext createDefaultContext(EntityManager em) {
		JpaMetaProvider jpaMetaProvider = new JpaMetaProvider(Collections.emptySet());
		MetaLookup metaLookup = new MetaLookup();
		metaLookup.addProvider(jpaMetaProvider);
		return new JpaQueryFactoryContext() {
			@Override
			public EntityManager getEntityManager() {
				return em;
			}

			@Override
			public MetaPartition getMetaPartition() {
				return jpaMetaProvider.getPartition();
			}
		};
	}

}
