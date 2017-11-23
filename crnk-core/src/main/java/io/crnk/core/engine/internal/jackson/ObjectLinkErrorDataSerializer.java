package io.crnk.core.engine.internal.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import io.crnk.core.engine.document.ErrorData;

/**
 * @author AdNovum Informatik AG
 */
public class ObjectLinkErrorDataSerializer extends ErrorDataSerializer {

	@Override
	void writeAboutLink(ErrorData errorData, JsonGenerator gen) throws IOException {
		if (errorData.getAboutLink() != null) {
			gen.writeObjectFieldStart(ErrorDataSerializer.LINKS);
			gen.writeObjectFieldStart(ErrorDataSerializer.ABOUT_LINK);
			gen.writeStringField(LinksInformationSerializer.HREF, errorData.getAboutLink());
			gen.writeEndObject();
			gen.writeEndObject();
		}
	}

}