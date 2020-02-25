package io.crnk.core.queryspec.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.ResourceRegistry;

public interface QuerySpecUrlContext {

    ResourceRegistry getResourceRegistry();

    TypeParser getTypeParser();

    ObjectMapper getObjectMapper();

    UrlBuilder getUrlBuilder();
}
