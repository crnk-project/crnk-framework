package io.crnk.gen.typescript.transform;

import io.crnk.gen.typescript.model.TSElement;
import io.crnk.meta.model.MetaElement;

public interface TSMetaTransformation {

	/**
	 * @return true if this implementation can transform the given element.
	 */
	boolean accepts(MetaElement element);

	TSElement transform(MetaElement element, TSMetaTransformationContext context, TSMetaTransformationOptions options);

	/**
	 * Executed after all transformations done. Typically used for linking.
	 */
	void postTransform(TSElement element, TSMetaTransformationContext context);

	/**
	 * @return whether the given object can initiate a transformation process, denoted as a
	 * root element. Root elements may then further transform elements they depend upon.
	 * The use of such root elements allows to eliminate the transformation of unused elements.
	 */
	boolean isRoot(MetaElement element);
}
