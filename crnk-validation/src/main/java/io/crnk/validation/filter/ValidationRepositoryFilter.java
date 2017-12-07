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
import javax.validation.ValidatorFactory;

public class ValidationRepositoryFilter extends RepositoryFilterBase {

	private final ValidatorFactory validatorFactory;

	public ValidationRepositoryFilter(final ValidatorFactory validatorFactory) {
		this.validatorFactory = validatorFactory;

	}
	@Override
	public JsonApiResponse filterRequest(final RepositoryFilterContext context, final RepositoryRequestFilterChain chain) {
		if (context.getRequest().getMethod() == HttpMethod.POST ||
				context.getRequest().getMethod() == HttpMethod.PATCH || context.getRequest().getMethod() == HttpMethod.PUT) {
			Validator validator = validatorFactory.getValidator();
			Set<ConstraintViolation<Object>> violations = validator.validate(context.getRequest().getEntity());
			if (!violations.isEmpty()) {
				throw new ConstraintViolationException(violations);
			}
		}

		return super.filterRequest(context, chain);
	}
}
