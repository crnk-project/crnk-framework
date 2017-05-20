package io.crnk.meta.information;

import io.crnk.core.utils.Optional;
import io.crnk.meta.model.MetaElement;

/**
 * resource, repository or field information backed by meta data.
 */
public interface MetaAwareInformation<T extends MetaElement> {

	/**
	 * @return meta element of this resource field
	 */
	Optional<T> getMetaElement();

	/**
	 * @return meta element this information element was derived from. Like a JPA attribute mapped to a JsonApi field.
	 */
	Optional<T> getProjectedMetaElement();
}
