package io.crnk.jpa.meta.internal;

import io.crnk.jpa.meta.MetaJpaDataObject;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaJsonObject;
import io.crnk.meta.provider.MetaProviderBase;

import java.util.List;

public class JpaResourceMetaEnricher extends MetaProviderBase {


	private MetaLookup jpaMetaLookup;

	public JpaResourceMetaEnricher(MetaLookup jpaMetaLookup) {
		this.jpaMetaLookup = jpaMetaLookup;
	}

	@Override
	public void onInitialized(MetaElement element) {

		if (element instanceof MetaJsonObject) {
			MetaJsonObject jsonDataObject = (MetaJsonObject) element;
			Class<?> implementationClass = jsonDataObject.getImplementationClass();

			MetaJpaDataObject jpaDataObject = jpaMetaLookup.getMeta(implementationClass, MetaJpaDataObject.class, true);
			if (jpaDataObject != null) {

				if (jpaDataObject.getPrimaryKey() != null && jsonDataObject.getPrimaryKey() != null) {
					jsonDataObject.getPrimaryKey().setGenerated(jpaDataObject.getPrimaryKey().isGenerated());
				}

				List<? extends MetaAttribute> declaredAttributes = jsonDataObject.getDeclaredAttributes();
				for (MetaAttribute declaredAttribute : declaredAttributes) {
					String name = declaredAttribute.getName();
					if (jpaDataObject.hasAttribute(name)) {
						MetaAttribute jpaAttribute = jpaDataObject.getAttribute(name);

						declaredAttribute.setLob(jpaAttribute.isLob());
						declaredAttribute.setVersion(jpaAttribute.isVersion());
						declaredAttribute.setNullable(jpaAttribute.isNullable());
						declaredAttribute.setCascaded(jpaAttribute.isCascaded());
					}
				}
			}
		}
	}
}