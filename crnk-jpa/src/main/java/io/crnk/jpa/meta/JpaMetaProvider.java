package io.crnk.jpa.meta;

import io.crnk.jpa.meta.internal.JpaMetaFilter;
import io.crnk.jpa.meta.internal.JpaMetaPartition;
import io.crnk.meta.internal.MetaIdProvider;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.provider.MetaFilter;
import io.crnk.meta.provider.MetaPartition;
import io.crnk.meta.provider.MetaProviderBase;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

public class JpaMetaProvider extends MetaProviderBase {

	private final JpaMetaPartition partition;

	public JpaMetaProvider(Set<Class> jpaTypes) {
		// nothing to do here
		this.partition = new JpaMetaPartition(jpaTypes, new MetaIdProvider());
	}

	public JpaMetaProvider(EntityManagerFactory entityManagerFactory) {
		this(toTypes(entityManagerFactory));
	}

	private static Set<Class> toTypes(EntityManagerFactory entityManagerFactory) {
		Set<Class> set = new HashSet<>();

		Set<EmbeddableType<?>> embeddables = entityManagerFactory.getMetamodel().getEmbeddables();
		for (EmbeddableType<?> embeddable : embeddables) {
			set.add(embeddable.getJavaType());
		}

		Set<EntityType<?>> entities = entityManagerFactory.getMetamodel().getEntities();
		for (EntityType<?> entity : entities) {
			set.add(entity.getJavaType());
		}
		return set;
	}


	@Override
	public Collection<MetaFilter> getFilters() {
		return Arrays.asList((MetaFilter) new JpaMetaFilter(partition, context));
	}

	@Override
	public Collection<MetaPartition> getPartitions() {
		return Arrays.asList((MetaPartition) partition);
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

	public boolean hasMeta(Class<?> clazz) {
		return partition.hasMeta(clazz);
	}

	public <T extends MetaElement> T getMeta(Class<?> clazz) {
		return (T) partition.getMeta(clazz);
	}

	public JpaMetaPartition getPartition() {
		return partition;
	}

	public <T extends MetaElement> T discoverMeta(final Class<?> clazz) {
		if (hasMeta(clazz)) {
			return getMeta(clazz);
		}
		return context.runDiscovery(new Callable<T>() {

			@Override
			public T call() {
				return (T) partition.allocateMetaElement(clazz).get();
			}
		});
	}
}
