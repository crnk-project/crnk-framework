package io.crnk.ui.presentation;

import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaType;
import io.crnk.ui.presentation.element.PresentationElement;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;

public class PresentationEnvironment {

    private MetaElement element;

    private MetaType type;

    private ArrayDeque<MetaAttribute> attributePath;

    private boolean editable;

    private List<PresentationType> acceptedTypes = Collections.emptyList();

    private PresentationManager factory;

    public PresentationElement createElement(PresentationEnvironment env) {
        return factory.createElement(env);
    }

    public MetaElement getElement() {
        return element;
    }

    public void setElement(MetaElement element) {
        this.element = element;
    }

    public MetaType getType() {
        return type;
    }

    public void setType(MetaType type) {
        this.type = type;
    }

    public ArrayDeque<MetaAttribute> getAttributePath() {
        return attributePath;
    }

    public void setAttributePath(ArrayDeque<MetaAttribute> attributePath) {
        this.attributePath = attributePath;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public List<PresentationType> getAcceptedTypes() {
        return acceptedTypes;
    }

    public void setAcceptedTypes(List<PresentationType> acceptedTypes) {
        this.acceptedTypes = acceptedTypes;
    }

    public PresentationManager getFactory() {
        return factory;
    }

    public void setFactory(PresentationManager factory) {
        this.factory = factory;
    }
}

