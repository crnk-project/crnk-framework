package io.crnk.core.exception;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.internal.exception.CrnkExceptionMapper;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class CrnkExceptionMapperTest {

	private static final String TITLE1 = "title1";

	private static final String DETAIL1 = "detail1";

	@Test
	public void shouldMapToErrorResponse() {
		CrnkExceptionMapper mapper = new CrnkExceptionMapper();
		ErrorResponse response = mapper.toErrorResponse(new SampleCrnkException());

		assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
		assertThat((Iterable<?>) response.getResponse().getErrors()).hasSize(1).extracting("title", "detail")
				.containsExactly(tuple(TITLE1, DETAIL1));
	}

	@Test
	public void shouldUseTitleIfDetailIsMissing() {
		CrnkExceptionMapper mapper = new CrnkExceptionMapper();
		ErrorResponse response = mapper.toErrorResponse(new SampleCrnkExceptionWithEmptyDetail());

		assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
		assertThat((Iterable<?>) response.getResponse().getErrors()).hasSize(1);

		ErrorData errorData = response.getResponse().getErrors().get(0);
		assertThat(errorData.getTitle()).isEqualTo(TITLE1);
		assertThat(errorData.getDetail()).isNull();
	}

	@Test
	public void internalServerError() {
		CrnkExceptionMapper mapper = new CrnkExceptionMapper();
		ErrorResponse response = mapper.toErrorResponse(new InternalServerErrorException("testMessage"));
		assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
		assertThat(mapper.accepts(response)).isTrue();
		CrnkMappableException exception = mapper.fromErrorResponse(response);
		assertThat(exception).isInstanceOf(InternalServerErrorException.class);
		assertThat(exception.getMessage()).isEqualTo("testMessage");
	}

	@Test
	public void methodNotAllowed() {
		CrnkExceptionMapper mapper = new CrnkExceptionMapper();
		ErrorResponse response = mapper.toErrorResponse(new MethodNotAllowedException("GET"));
		assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED_405);
		assertThat(mapper.accepts(response)).isTrue();
		CrnkMappableException exception = mapper.fromErrorResponse(response);
		assertThat(exception).isInstanceOf(MethodNotAllowedException.class);
		assertThat(exception.getMessage()).isEqualTo("method not allowed: GET");
	}


	@Test(expected = IllegalStateException.class)
	public void invalidExceptionNotManagedByMapper() {
		CrnkExceptionMapper mapper = new CrnkExceptionMapper();
		mapper.fromErrorResponse(new ErrorResponse(null, 123));
	}

	@Test
	public void badRequest() {
		CrnkExceptionMapper mapper = new CrnkExceptionMapper();
		ErrorResponse response = mapper.toErrorResponse(new BadRequestException("testMessage"));
		assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400);
		assertThat(mapper.accepts(response)).isTrue();
		CrnkMappableException exception = mapper.fromErrorResponse(response);
		assertThat(exception).isInstanceOf(BadRequestException.class);
		assertThat(exception.getMessage()).isEqualTo("testMessage");
	}

	@Test
	public void notAuthorized() {
		CrnkExceptionMapper mapper = new CrnkExceptionMapper();
		ErrorResponse response = mapper.toErrorResponse(new UnauthorizedException("testMessage"));
		assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
		assertThat(mapper.accepts(response)).isTrue();
		CrnkMappableException exception = mapper.fromErrorResponse(response);
		assertThat(exception).isInstanceOf(UnauthorizedException.class);
		assertThat(exception.getMessage()).isEqualTo("testMessage");
	}

	@Test
	public void forbidden() {
		CrnkExceptionMapper mapper = new CrnkExceptionMapper();
		ErrorResponse response = mapper.toErrorResponse(new ForbiddenException("testMessage"));
		assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN_403);
		assertThat(mapper.accepts(response)).isTrue();
		CrnkMappableException exception = mapper.fromErrorResponse(response);
		assertThat(exception).isInstanceOf(ForbiddenException.class);
		assertThat(exception.getMessage()).isEqualTo("testMessage");
	}

	private static class SampleCrnkException extends CrnkMappableException {

		SampleCrnkException() {
			super(HttpStatus.INTERNAL_SERVER_ERROR_500, ErrorData.builder().setTitle(TITLE1).setDetail(DETAIL1)
					.setStatus(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR_500)).build());
		}
	}

	private static class SampleCrnkExceptionWithEmptyDetail extends CrnkMappableException {

		SampleCrnkExceptionWithEmptyDetail() {
			super(HttpStatus.INTERNAL_SERVER_ERROR_500, ErrorData.builder().setTitle(TITLE1)
					.setStatus(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR_500)).build());
		}
	}
}
