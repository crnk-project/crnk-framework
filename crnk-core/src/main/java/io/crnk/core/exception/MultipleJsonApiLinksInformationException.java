package io.crnk.core.exception;

/**
 * A resource contains more then one field annotated with {@link io.crnk.core.resource.annotations.JsonApiLinksInformation} annotation.
 */
public class MultipleJsonApiLinksInformationException extends CrnkInitializationException {

	public MultipleJsonApiLinksInformationException(String className) {
		super("Duplicated links fields found in class: " + className);
	}
}
