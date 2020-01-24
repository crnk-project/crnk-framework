package io.crnk.validation.filter;

import io.crnk.core.engine.filter.RepositoryFilterBase;
import io.crnk.core.engine.filter.RepositoryFilterContext;
import io.crnk.core.engine.filter.RepositoryRequestFilterChain;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.repository.response.JsonApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Set;

public class ValidationRepositoryFilter extends RepositoryFilterBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationRepositoryFilter.class);

	private final Validator validator;

	public ValidationRepositoryFilter(final Validator validator) {
		this.validator = validator;
		LOGGER.debug("validation filter setup");
	}

	@Override
	public JsonApiResponse filterRequest(final RepositoryFilterContext context, final RepositoryRequestFilterChain chain) {
		if (context.getRequest().getRelationshipField() == null) {
			if (context.getRequest().getMethod() == HttpMethod.POST ||
					context.getRequest().getMethod() == HttpMethod.PATCH) {
				Object entity = context.getRequest().getEntity();
				Set<ConstraintViolation<Object>> violations = validator.validate(entity);
				LOGGER.debug("performing validation check, {} violations for entity {}", violations.size(), entity);
				if (!violations.isEmpty()) {
					throw new ConstraintViolationException(violations);
				}
			}
		}

		return super.filterRequest(context, chain);
	}
}
