package io.crnk.ui.presentation.factory;

import io.crnk.ui.presentation.PresentationEnvironment;
import io.crnk.ui.presentation.element.PresentationElement;

public interface PresentationElementFactory {

    boolean accepts(PresentationEnvironment env);

    PresentationElement create(PresentationEnvironment env);
}
