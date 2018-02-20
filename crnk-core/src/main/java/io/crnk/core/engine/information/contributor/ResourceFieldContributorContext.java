package io.crnk.core.engine.information.contributor;

import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.resource.ResourceInformation;

public interface ResourceFieldContributorContext {

	ResourceInformation getResourceInformation();

	InformationBuilder getInformationBuilder();

}
