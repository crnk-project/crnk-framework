package io.crnk.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.queryspec.DefaultQuerySpecDeserializer;
import io.crnk.legacy.locator.JsonServiceLocator;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.legacy.queryParams.QueryParamsBuilder;
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

	public CrnkTestFeature(
			ObjectMapper objectMapper,
			QueryParamsBuilder queryParamsBuilder,
			JsonServiceLocator jsonServiceLocator) {
		super(objectMapper, queryParamsBuilder, jsonServiceLocator);
		testRequestFilter = new TestRequestFilter();
	}

	public CrnkTestFeature(
			ObjectMapper objectMapper,
			DefaultQuerySpecDeserializer defaultQuerySpecDeserializer,
			SampleJsonServiceLocator jsonServiceLocator) {
		super(objectMapper, defaultQuerySpecDeserializer, jsonServiceLocator);
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
	 *
	 * @return
	 */
	public TestRequestFilter getTestFilter() {
		return testRequestFilter;
	}
}
