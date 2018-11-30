package io.crnk.core.queryspec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.ResourceRegistry;

@Deprecated
public interface QuerySpecDeserializerContext {

	ResourceRegistry getResourceRegistry();

	TypeParser getTypeParser();

    ObjectMapper getObjectMapper();
}
