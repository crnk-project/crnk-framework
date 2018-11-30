package io.crnk.core.queryspec.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.queryspec.QuerySpecDeserializerContext;

public interface QuerySpecUrlContext extends QuerySpecDeserializerContext {

    ResourceRegistry getResourceRegistry();

    TypeParser getTypeParser();

    ObjectMapper getObjectMapper();
}
