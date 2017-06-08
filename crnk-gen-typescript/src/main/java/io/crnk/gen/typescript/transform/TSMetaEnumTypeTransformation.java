package io.crnk.gen.typescript.transform;

import io.crnk.gen.typescript.internal.TypescriptUtils;
import io.crnk.gen.typescript.model.TSElement;
import io.crnk.gen.typescript.model.TSEnumLiteral;
import io.crnk.gen.typescript.model.TSEnumType;
import io.crnk.gen.typescript.model.TSSource;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaEnumType;

public class TSMetaEnumTypeTransformation implements TSMetaTransformation {

	@Override
	public boolean accepts(MetaElement element) {
		return element instanceof MetaEnumType;
	}

	@Override
	public TSElement transform(MetaElement elementObj, TSMetaTransformationContext context, TSMetaTransformationOptions
			options) {
		MetaEnumType element = (MetaEnumType) elementObj;

		TSSource source = new TSSource();
		source.setName(TypescriptUtils.toFileName(element.getName()));
		source.setNpmPackage(context.getNpmPackage(element));
		source.setDirectory(context.getDirectory(element));

		Class<?> implementationClass = element.getImplementationClass();
		TSEnumType enumType = new TSEnumType();
		enumType.setExported(true);
		enumType.setParent(source);
		enumType.setName(element.getName());
		for (Object literal : implementationClass.getEnumConstants()) {
			enumType.getLiterals().add(new TSEnumLiteral(literal.toString()));
		}
		source.getElements().add(enumType);

		context.putMapping(element, enumType);
		context.addSource(source);

		return enumType;
	}

	@Override
	public boolean isRoot(MetaElement element) {
		return false;
	}
}
