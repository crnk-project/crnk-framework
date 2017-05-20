package io.crnk.core.engine.information.resource;

import io.crnk.core.engine.parser.TypeParser;

public interface ResourceInformationBuilderContext {

	String getResourceType(Class<?> clazz);

	boolean accept(Class<?> type);

	TypeParser getTypeParser();
}
