package io.crnk.ui.presentation.factory;

import io.crnk.ui.presentation.PresentationEnvironment;
import io.crnk.ui.presentation.element.PresentationElement;

public class DefaultDisplayElementFactory implements PresentationElementFactory {
    @Override
    public boolean accepts(PresentationEnvironment env) {
        return true;
    }

    @Override
    public PresentationElement create(PresentationEnvironment env) {
        PresentationElement element = new PresentationElement();
        element.setComponentId("display");
        return element;
    }
}
