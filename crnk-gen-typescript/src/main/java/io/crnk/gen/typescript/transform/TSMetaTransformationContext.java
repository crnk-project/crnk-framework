package io.crnk.gen.typescript.transform;

import io.crnk.gen.typescript.TSResourceFormat;
import io.crnk.gen.typescript.model.TSElement;
import io.crnk.gen.typescript.model.TSSource;
import io.crnk.meta.model.MetaElement;

public interface TSMetaTransformationContext {

	String getDirectory(MetaElement meta);

	String getNpmPackage(MetaElement meta);

	void putMapping(MetaElement metaElement, TSElement tsElement);

	void addSource(TSSource source);

	TSElement transform(MetaElement metaElement, TSMetaTransformationOptions options);

	MetaElement getMeta(Class<?> implClass);

	MetaElement getMeta(String metaId);

	TSResourceFormat getResourceFormat();
}
