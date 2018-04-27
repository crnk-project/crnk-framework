package io.crnk.client;

import javax.ws.rs.core.FeatureContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.legacy.locator.JsonServiceLocator;
import io.crnk.legacy.queryParams.QueryParamsBuilder;
import io.crnk.rs.CrnkFeature;

/**
 * Test-specific subclass of the {@link CrnkFeature} used to
 * configure extra test-specific JAX-RS functionality.
 *
 * @author Craig Setera
 */
public class CrnkTestFeature extends CrnkFeature {

	private TestRequestFilter testRequestFilter;

	public CrnkTestFeature(
			ObjectMapper objectMapper,
			QueryParamsBuilder queryParamsBuilder,
			JsonServiceLocator jsonServiceLocator) {
		super(objectMapper, queryParamsBuilder, jsonServiceLocator);
		testRequestFilter = new TestRequestFilter();
	}

	public CrnkTestFeature() {
		testRequestFilter = new TestRequestFilter();
	}

	/*
	 * (non-Javadoc)
	 * @see io.crnk.rs.CrnkFeature#configure(javax.ws.rs.core.FeatureContext)
	 */
	@Override
	public boolean configure(FeatureContext context) {
		boolean result = super.configure(context);
		context.register(testRequestFilter);
		return result;
	}

	/**
	 * Return the {@link TestRequestFilter} that is registered
	 * with the feature.
	 */
	public TestRequestFilter getTestFilter() {
		return testRequestFilter;
	}
}
