package io.crnk.core.engine.internal.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.bean.BeanInformation;
import io.crnk.core.engine.internal.utils.SerializerUtil;
import io.crnk.core.resource.links.DefaultLink;
import io.crnk.core.resource.links.Link;
import io.crnk.core.resource.links.LinksInformation;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Serializes {@link LinksInformation} objects as JSON objects instead of simple JSON attributes.
 */
public class LinksInformationSerializer extends JsonSerializer<LinksInformation> {

	private Boolean serializeLinksAsObjects;

	LinksInformationSerializer(Boolean serializeLinksAsObjects) {
		this.serializeLinksAsObjects = serializeLinksAsObjects;
	}

	@Override
	public void serialize(LinksInformation value, JsonGenerator gen, SerializerProvider serializers)
			throws IOException {

		gen.writeStartObject();

		BeanInformation info = BeanInformation.get(value.getClass());

		for (String attrName : info.getAttributeNames()) {
			BeanAttributeInformation attribute = info.getAttribute(attrName);
			Object objLinkValue = attribute.getValue(value);
			String name = attribute.getJsonName();
			Link linkValue = objLinkValue instanceof String ? new DefaultLink((String) objLinkValue) : (Link) objLinkValue;
			if (linkValue != null) {
				if (!serializeLinksAsObjects && !shouldSerializeLink(linkValue)) { // Return a simple String link
					gen.writeStringField(name, linkValue.getHref());
				} else {
					gen.writeObjectField(name, linkValue);
				}
			}
		}

		gen.writeEndObject();

	}

	@Override
	public Class<LinksInformation> handledType() {
		return LinksInformation.class;
	}

	private Boolean shouldSerializeLink(Link link) {
		return link.getRel() != null || link.getAnchor() != null || link.getParams() != null || link.getDescribedby() != null || link.getMeta() != null;
	}
}
