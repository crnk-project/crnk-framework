package io.crnk.meta.provider;

import io.crnk.meta.model.MetaElement;

public interface MetaFilter {

	void onInitializing(MetaElement element);

	void onInitialized(MetaElement element);

	/**
	 * Allows to return a customized MetaElement for the currently active request (like accouting for security).
	 *
	 * @param element
	 * @return element, modified copy or null to exclude from request
	 */
	MetaElement adjustForRequest(MetaElement element);
}
