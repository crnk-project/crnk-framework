package io.crnk.legacy.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.information.resource.ResourceInformationProviderContext;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.utils.Supplier;

public class DefaultResourceInformationProviderContext implements ResourceInformationProviderContext {

    private final InformationBuilder informationBuilder;
    private final Supplier<ObjectMapper> objectMapper;

    private ResourceInformationProvider provider;
    private TypeParser typeParser;

    public DefaultResourceInformationProviderContext(ResourceInformationProvider provider, InformationBuilder informationBuilder,
                                                     TypeParser typeParser, Supplier<ObjectMapper> objectMapper) {
        this.provider = provider;
        this.typeParser = typeParser;
        this.informationBuilder = informationBuilder;
        this.objectMapper = objectMapper;

        PreconditionUtil.assertNotNull("informationBuilder is null", informationBuilder);
        PreconditionUtil.assertNotNull("typeParser is null", typeParser);
        PreconditionUtil.assertNotNull("objectMapper is null", objectMapper);
    }

    @Override
    public String getResourceType(Class<?> clazz) {
        return provider.getResourceType(clazz);
    }

    @Override
    public boolean accept(Class<?> type) {
        return provider.accept(type);
    }

    @Override
    public TypeParser getTypeParser() {
        return typeParser;
    }

    @Override
    public InformationBuilder getInformationBuilder() {
        return informationBuilder;
    }

    @Override
    public ObjectMapper getObjectMapper() {
        PreconditionUtil.assertNotNull("objectMapper is null", objectMapper);
        return objectMapper.get();
    }
}
