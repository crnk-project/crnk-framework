package io.crnk.validation.filter;

import io.crnk.core.engine.filter.RepositoryFilterBase;
import io.crnk.core.engine.filter.RepositoryFilterContext;
import io.crnk.core.engine.filter.RepositoryRequestFilterChain;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.repository.response.JsonApiResponse;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

public class ValidationRepositoryFilter extends RepositoryFilterBase {

	private final Validator validator;

	public ValidationRepositoryFilter(final Validator validator) {
		this.validator = validator;

	}
	@Override
	public JsonApiResponse filterRequest(final RepositoryFilterContext context, final RepositoryRequestFilterChain chain) {
		if (context.getRequest().getMethod() == HttpMethod.POST ||
				context.getRequest().getMethod() == HttpMethod.PATCH || context.getRequest().getMethod() == HttpMethod.PUT) {
			Set<ConstraintViolation<Object>> violations = validator.validate(context.getRequest().getEntity());
			if (!violations.isEmpty()) {
				throw new ConstraintViolationException(violations);
			}
		}

		return super.filterRequest(context, chain);
	}
}
