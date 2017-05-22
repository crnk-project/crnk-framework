package io.crnk.gen.typescript.transform;

import io.crnk.gen.typescript.model.TSElement;
import io.crnk.meta.model.MetaElement;

public interface TSMetaTransformation {

	boolean accepts(MetaElement element);

	TSElement transform(MetaElement element, TSMetaTransformationContext context);
}
