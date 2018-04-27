package io.crnk.core.queryspec.mapper;

public interface UnkonwnMappingAware {

	boolean getAllowUnknownAttributes();

	void setAllowUnknownAttributes(boolean allowUnknownAttributes);

	boolean isAllowUnknownParameters();

	void setAllowUnknownParameters(final boolean allowUnknownParameters);

}
