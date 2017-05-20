package io.crnk.example.jersey;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.legacy.locator.JsonServiceLocator;
import io.crnk.legacy.queryParams.DefaultQueryParamsParser;
import io.crnk.legacy.queryParams.QueryParamsBuilder;
import io.crnk.rs.CrnkFeature;
import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;

public class CrnkDynamicFeature extends CrnkFeature {

	@Inject
	public CrnkDynamicFeature(ObjectMapper objectMapper, final ServiceLocator ServiceLocator) {
		super(objectMapper, new QueryParamsBuilder(new DefaultQueryParamsParser()), new JsonServiceLocator() {
			@Override
			public <T> T getInstance(Class<T> clazz) {
				return ServiceLocator.getService(clazz);
			}
		});
	}
}
