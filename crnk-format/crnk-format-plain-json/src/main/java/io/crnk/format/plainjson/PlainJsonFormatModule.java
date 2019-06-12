package io.crnk.format.plainjson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.crnk.core.module.InitializingModule;
import io.crnk.format.plainjson.internal.PlainJsonDocument;
import io.crnk.format.plainjson.internal.PlainJsonDocumentDeserializer;
import io.crnk.format.plainjson.internal.PlainJsonDocumentSerializer;
import io.crnk.format.plainjson.internal.PlainJsonRequestProcessor;

/**
 * Support for a simplified JSON format does includes relationships directly rather than through a normalized include section and does also not
 * have wrapper elements like &qout;attributes&qout; and &qout;relationships&qout;.
 */
public class PlainJsonFormatModule implements InitializingModule {

    private ModuleContext context;

    @Override
    public String getModuleName() {
        return "plain-json";
    }

    @Override
    public void setupModule(ModuleContext context) {
        this.context = context;
        context.addHttpRequestProcessor(
                new PlainJsonRequestProcessor(context));

    }

    @Override
    public void init() {
        SimpleModule jacksonModule = new SimpleModule("plain-json", new Version(1, 0, 0, null, null, null));

        ObjectMapper objectMapper = context.getObjectMapper();
        jacksonModule.addSerializer(new PlainJsonDocumentSerializer());
        jacksonModule.addDeserializer(PlainJsonDocument.class, new PlainJsonDocumentDeserializer(objectMapper));
        objectMapper.registerModule(jacksonModule);
    }
}
