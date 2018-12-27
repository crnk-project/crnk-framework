package io.crnk.jpa.meta.internal;

import io.crnk.meta.internal.MetaIdProvider;
import io.crnk.meta.internal.typed.TypedMetaPartitionBase;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.provider.MetaPartitionContext;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;

public class JpaMetaPartition extends TypedMetaPartitionBase {


	private final MetaIdProvider idProvider;

	private final Set<Class> jpaTypes;

	public JpaMetaPartition(Set<Class> jpaTypes, MetaIdProvider idProvider) {
		super();
		this.idProvider = idProvider;

		this.jpaTypes = jpaTypes;

		this.addFactory(new EmbeddableMetaFactory());
		this.addFactory(new MappedSuperclassMetaFactory());
		this.addFactory(new EntityMetaProvider());
	}


	@Override
	public void init(MetaPartitionContext context) {
		super.init(context);
		this.parent = context.getBasePartition();
	}

	@Override
	protected Optional<MetaElement> addElement(Type type, MetaElement element) {
		if (element instanceof MetaType) {
			MetaType typeElement = element.asType();
			if (!element.hasId()) {
				element.setId(computeId(typeElement)); //idProvider.computeIdPrefixFromPackage(implClass, element) + element.getName());
			}
		}
		return super.addElement(type, element);
	}

	private String computeId(MetaType element) {
		Class<?> implementationClass = element.getImplementationClass();
		return idProvider.computeIdPrefixFromPackage(implementationClass, element) + element.getName();
	}


	@Override
	public void discoverElements() {
		for (Class jpaType : jpaTypes) {
			allocateMetaElement(jpaType);
		}
	}

}