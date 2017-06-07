package io.crnk.gen.typescript.transform;

import io.crnk.gen.typescript.model.TSElement;
import io.crnk.meta.model.MetaElement;

public interface TSMetaTransformation {

	/**
	 * @return true if this implementation can transform the given element.
	 */
	public boolean accepts(MetaElement element);


	public TSElement transform(MetaElement element, TSMetaTransformationContext context, TSMetaTransformationOptions options);

	/**
	 * @return whether the given object can initiate a transformation process, denoted as a
	 * root element. Root elements may then further transform elements they depend upon.
	 * The use of such root elements allows to eliminate the transformation of unused elements.
	 */
	public boolean isRoot(MetaElement element);
}
