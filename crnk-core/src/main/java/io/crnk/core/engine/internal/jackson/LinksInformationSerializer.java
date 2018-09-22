package io.crnk.core.engine.internal.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.bean.BeanInformation;
import io.crnk.core.engine.internal.utils.SerializerUtil;
import io.crnk.core.resource.links.LinksInformation;

import java.io.IOException;

/**
 * Serializes {@link LinksInformation} objects as JSON objects instead of simple JSON attributes.
 */
public class LinksInformationSerializer extends JsonSerializer<LinksInformation> {

	@Override
	public void serialize(LinksInformation value, JsonGenerator gen, SerializerProvider serializers)
			throws IOException {

		gen.writeStartObject();

		BeanInformation info = BeanInformation.get(value.getClass());

		for (String attrName : info.getAttributeNames()) {
			BeanAttributeInformation attribute = info.getAttribute(attrName);
			Object linkValue = attribute.getValue(value);
			if (linkValue != null) {
				gen.writeObjectFieldStart(attrName);
				if (linkValue instanceof String) {
					gen.writeStringField(SerializerUtil.HREF, linkValue.toString());
				} else {
					gen.writeObject(linkValue);
				}
				gen.writeEndObject();
			}
		}

		gen.writeEndObject();

	}

	@Override
	public Class<LinksInformation> handledType() {
		return LinksInformation.class;
	}

}
