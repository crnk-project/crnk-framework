package io.crnk.jpa.internal;

import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.utils.Optional;
import io.crnk.meta.information.MetaAwareInformation;
import io.crnk.meta.model.MetaAttribute;

import java.lang.reflect.Type;

public class JpaResourceField extends ResourceFieldImpl implements MetaAwareInformation<MetaAttribute> {

	private MetaAttribute projectedJpaAttribute;

	public JpaResourceField(MetaAttribute projectedJpaAttribute, String jsonName, String underlyingName,
							ResourceFieldType resourceFieldType, Class<?> type, Type genericType, String oppositeResourceType,
							String oppositeName, boolean lazy, boolean includeByDefault, LookupIncludeBehavior lookupIncludeBehavior,
							ResourceFieldAccess access) {
		super(jsonName, underlyingName, resourceFieldType, type, genericType, oppositeResourceType, oppositeName, lazy,
				includeByDefault, lookupIncludeBehavior, access);
		this.projectedJpaAttribute = projectedJpaAttribute;
	}

	@Override
	public Optional<MetaAttribute> getMetaElement() {
		return Optional.empty();
	}

	@Override
	public Optional<MetaAttribute> getProjectedMetaElement() {
		return Optional.of(projectedJpaAttribute);
	}

}
