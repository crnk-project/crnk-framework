package io.crnk.client;

import io.crnk.rs.CrnkFeature;

import javax.ws.rs.core.FeatureContext;

/**
 * Test-specific subclass of the {@link CrnkFeature} used to
 * configure extra test-specific JAX-RS functionality.
 *
 * @author Craig Setera
 */
public class CrnkTestFeature extends CrnkFeature {

	private TestRequestFilter testRequestFilter;

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
