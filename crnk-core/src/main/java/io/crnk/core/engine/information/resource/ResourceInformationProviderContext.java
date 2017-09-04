package io.crnk.core.engine.information.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.parser.TypeParser;

public interface ResourceInformationProviderContext {

	String getResourceType(Class<?> clazz);

	boolean accept(Class<?> type);

	TypeParser getTypeParser();

	InformationBuilder getInformationBuilder();

	ObjectMapper getObjectMapper();
}
