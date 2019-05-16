package io.crnk.data.jpa.internal;

import io.crnk.data.jpa.internal.query.ComputedAttributeRegistryImpl;
import io.crnk.data.jpa.meta.JpaMetaProvider;
import io.crnk.data.jpa.query.ComputedAttributeRegistry;
import io.crnk.data.jpa.query.JpaQueryFactory;
import io.crnk.data.jpa.query.JpaQueryFactoryContext;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.provider.MetaPartition;

import javax.persistence.EntityManager;
import java.util.Collections;

public abstract class JpaQueryFactoryBase implements JpaQueryFactory {

	protected ComputedAttributeRegistryImpl computedAttrs = new ComputedAttributeRegistryImpl();

	protected JpaQueryFactoryContext context;

	@Override
	public void initalize(JpaQueryFactoryContext context) {
		this.computedAttrs.init(context);
		this.context = context;
	}

	public EntityManager getEntityManager() {
		return context.getEntityManager();
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
