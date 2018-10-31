package io.crnk.format.plainjson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.crnk.core.module.Module;
import io.crnk.format.plainjson.internal.PlainJsonDocument;
import io.crnk.format.plainjson.internal.PlainJsonDocumentDeserializer;
import io.crnk.format.plainjson.internal.PlainJsonDocumentSerializer;
import io.crnk.format.plainjson.internal.PlainJsonRequestProcessor;

public class PlainJsonFormatModule implements Module {

	@Override
	public String getModuleName() {
		return "plain-json";
	}

	@Override
	public void setupModule(ModuleContext context) {
		SimpleModule jacksonModule = new SimpleModule("plain-json", new Version(1, 0, 0, null, null, null));
		jacksonModule.addSerializer(new PlainJsonDocumentSerializer());
		jacksonModule.addDeserializer(PlainJsonDocument.class, new PlainJsonDocumentDeserializer(context.getObjectMapper()));
		context.getObjectMapper().registerModule(jacksonModule);

		context.addHttpRequestProcessor(
				new PlainJsonRequestProcessor(context));

	}
}
