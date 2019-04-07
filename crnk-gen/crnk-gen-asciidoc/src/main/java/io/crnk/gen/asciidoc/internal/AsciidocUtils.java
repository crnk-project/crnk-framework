package io.crnk.gen.asciidoc.internal;

import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaResource;

public class AsciidocUtils {
    public static String fromHtml(String html) {
        if (html == null) {
            return "";
        }

        String text = html;
        text = text.replace("<b>", "*");
        text = text.replace("</b>", "*");
        return text;
    }

    public static String toAnchor(String name) {
        return name.toLowerCase().replace(" ", "_").replace("/", "_");
    }

    public static String getAnchor(MetaResource resource) {
        return AsciidocUtils.toAnchor("resources_" + resource.getResourceType());
    }

    public static String getAnchor(MetaType type) {
        if (type instanceof MetaResource) {
            return getAnchor((MetaResource) type);
        }
        return AsciidocUtils.toAnchor("types_" + type.getName());
    }

}
