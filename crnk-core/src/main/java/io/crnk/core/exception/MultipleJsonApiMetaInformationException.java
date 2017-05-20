package io.crnk.core.exception;

/**
 * A resource contains more then one field annotated with {@link io.crnk.core.resource.annotations.JsonApiMetaInformation} annotation.
 */
public class MultipleJsonApiMetaInformationException extends CrnkInitializationException {

	public MultipleJsonApiMetaInformationException(String className) {
		super("Duplicated meta fields found in class: " + className);
	}
}
