package io.crnk.gen.asciidoc.internal;

import java.util.HashMap;
import java.util.Map;

public class ClassDocModel {

    private String text;

    private Map<String, String> attributes = new HashMap<>();

    protected void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getAttributeText(String name) {
        return attributes.get(name);
    }

    protected void setAttributeText(String fieldName, String comment) {
        attributes.put(fieldName, comment);
    }
}
