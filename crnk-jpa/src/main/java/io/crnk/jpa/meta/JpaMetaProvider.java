package io.crnk.jpa.meta;

import io.crnk.jpa.meta.internal.EmbeddableMetaProvider;
import io.crnk.jpa.meta.internal.EntityMetaProvider;
import io.crnk.jpa.meta.internal.MappedSuperclassMetaProvider;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.provider.MetaProvider;
import io.crnk.meta.provider.MetaProviderBase;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class JpaMetaProvider extends MetaProviderBase {

	private EntityManagerFactory entityManagerFactory;

	public JpaMetaProvider() {
		// nothing to do here
	}

	public JpaMetaProvider(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public Collection<MetaProvider> getDependencies() {
		return Arrays.asList((MetaProvider) new EntityMetaProvider(), new EmbeddableMetaProvider(), new MappedSuperclassMetaProvider());
	}

	@Override
	public Set<Class<? extends MetaElement>> getMetaTypes() {
		Set<Class<? extends MetaElement>> set = new HashSet<>();
		set.add(MetaEntity.class);
		set.add(MetaEmbeddable.class);
		set.add(MetaMappedSuperclass.class);
		set.add(MetaEntityAttribute.class);
		set.add(MetaEmbeddableAttribute.class);
		set.add(MetaJpaDataObject.class);
		return set;
	}

	@Override
	public void discoverElements() {
		if (entityManagerFactory != null) {
			Set<EmbeddableType<?>> embeddables = entityManagerFactory.getMetamodel().getEmbeddables();
			for (EmbeddableType<?> embeddable : embeddables) {
				context.getLookup().getMeta(embeddable.getJavaType(), MetaJpaDataObject.class);
			}

			Set<EntityType<?>> entities = entityManagerFactory.getMetamodel().getEntities();
			for (EntityType<?> entity : entities) {
				context.getLookup().getMeta(entity.getJavaType(), MetaJpaDataObject.class);
			}
		}
	}

}
