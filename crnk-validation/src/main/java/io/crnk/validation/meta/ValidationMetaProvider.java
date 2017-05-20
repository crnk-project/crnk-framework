package io.crnk.validation.meta;

import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.provider.MetaProviderBase;

import javax.validation.constraints.NotNull;

/**
 * Provides an integration into crnk-meta. Currently features:
 * <p>
 * <ul>
 * <li>
 * disable nullability for attributes annotated with {@link javax.validation.constraints.NotNull}.
 * <li>
 * </ul>
 * <p>
 * More features to follow (constraints, etc.).
 */
public class ValidationMetaProvider extends MetaProviderBase {

	public void onInitialized(MetaElement element) {
		if (element instanceof MetaAttribute) {
			MetaAttribute attr = (MetaAttribute) element;

			NotNull notNull = attr.getAnnotation(NotNull.class);
			if (notNull != null) {
				attr.setNullable(false);
			}
		}
	}
}
