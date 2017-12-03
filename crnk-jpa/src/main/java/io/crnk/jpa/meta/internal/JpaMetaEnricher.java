package io.crnk.jpa.meta.internal;

import io.crnk.jpa.meta.JpaMetaProvider;
import io.crnk.jpa.meta.MetaJpaDataObject;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaJsonObject;
import io.crnk.meta.provider.MetaFilter;
import io.crnk.meta.provider.MetaFilterBase;
import io.crnk.meta.provider.MetaProvider;
import io.crnk.meta.provider.MetaProviderBase;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class JpaMetaEnricher extends MetaFilterBase {


	private JpaMetaProvider metaProvider;

	public JpaMetaEnricher() {
	}

	public MetaProvider getProvider() {
		return new MetaProviderBase() {

			@Override
			public Collection<MetaFilter> getFilters() {
				return Arrays.asList((MetaFilter) JpaMetaEnricher.this);
			}
		};
	}

	@Override
	public void onInitialized(MetaElement element) {
		if (element instanceof MetaJsonObject) {
			MetaJsonObject jsonDataObject = (MetaJsonObject) element;
			Class<?> implementationClass = jsonDataObject.getImplementationClass();

			if (metaProvider.hasMeta(implementationClass)) {
				MetaJpaDataObject jpaDataObject = metaProvider.getMeta(implementationClass);

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

	public void setMetaProvider(JpaMetaProvider metaProvider) {
		this.metaProvider = metaProvider;
	}
}
