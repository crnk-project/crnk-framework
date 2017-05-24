package io.crnk.core.engine.internal.exception;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.utils.Optional;
import org.junit.Test;

import java.nio.file.ClosedFileSystemException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ExceptionMapperRegistryTest {

	//Reused in RequestDispatcherTest
	public static final ExceptionMapperRegistry exceptionMapperRegistry = new ExceptionMapperRegistry(exceptionMapperTypeSet());

	private static Set<ExceptionMapperType> exceptionMapperTypeSet() {
		Set<ExceptionMapperType> types = new HashSet<>();
		types.add(new ExceptionMapperType(IllegalStateException.class, new IllegalStateExceptionMapper()));
		types.add(new ExceptionMapperType(SomeIllegalStateException.class, new SomeIllegalStateExceptionMapper()));
		return types;
	}

	@Test
	public void shouldReturnIntegerMAXForNotRelatedClassesFromException() {
		int distance = exceptionMapperRegistry.getDistanceBetweenExceptions(Exception.class, SomeException.class);
		assertThat(distance).isEqualTo(Integer.MAX_VALUE);
	}

	@Test
	public void shouldReturn0DistanceBetweenSameClassFromException() throws Exception {
		int distance = exceptionMapperRegistry.getDistanceBetweenExceptions(Exception.class, Exception.class);
		assertThat(distance).isEqualTo(0);
	}

	@Test
	public void shouldReturn1AsADistanceBetweenSameClassFromException() throws Exception {
		int distance = exceptionMapperRegistry.getDistanceBetweenExceptions(SomeException.class, Exception.class);
		assertThat(distance).isEqualTo(1);
	}

	@Test
	public void shouldNotFindMapperIfSuperClassIsNotMappedFromException() throws Exception {
		Optional<JsonApiExceptionMapper> mapper = exceptionMapperRegistry.findMapperFor(RuntimeException.class);
		assertThat(mapper.isPresent()).isFalse();
	}

	@Test
	public void shouldFindDirectExceptionMapperFromException() throws Exception {
		Optional<JsonApiExceptionMapper> mapper = exceptionMapperRegistry.findMapperFor(IllegalStateException.class);
		assertThat(mapper.isPresent()).isTrue();
		assertThat(mapper.get()).isExactlyInstanceOf(IllegalStateExceptionMapper.class);
	}

	@Test
	public void shouldFindDescendantExceptionMapperFromException() throws Exception {
		Optional<JsonApiExceptionMapper> mapper = exceptionMapperRegistry.findMapperFor(ClosedFileSystemException.class);
		assertThat(mapper.isPresent()).isTrue();
		assertThat(mapper.get()).isExactlyInstanceOf(IllegalStateExceptionMapper.class);
	}

	@Test
	public void shouldFindDirectExceptionMapperFromError() throws Exception {
		ErrorResponse response = ErrorResponse.builder().setStatus(HttpStatus.BAD_REQUEST_400).build();
		Optional<ExceptionMapper<?>> mapper = (Optional)exceptionMapperRegistry.findMapperFor(response);
		assertThat(mapper.isPresent()).isTrue();
		assertThat(mapper.get()).isExactlyInstanceOf(IllegalStateExceptionMapper.class);
		Throwable throwable = mapper.get().fromErrorResponse(response);
		assertThat(throwable).isExactlyInstanceOf(IllegalStateException.class);
	}

	@Test
	public void shouldFindDescendantExceptionMapperFromError() throws Exception {
		// two exception mapper will match (IllegalStateException and SomeIllegalStateException)
		// subtype should be choosen.
		ErrorData errorData = ErrorData.builder().setId("someId").build();
		ErrorResponse response = ErrorResponse.builder().setStatus(HttpStatus.BAD_REQUEST_400).setSingleErrorData(errorData).build();
		Optional<ExceptionMapper<?>> mapper = (Optional)exceptionMapperRegistry.findMapperFor(response);
		assertThat(mapper.isPresent()).isTrue();
		assertThat(mapper.get()).isExactlyInstanceOf(SomeIllegalStateExceptionMapper.class);
		Throwable throwable = mapper.get().fromErrorResponse(response);
		assertThat(throwable).isExactlyInstanceOf(SomeIllegalStateException.class);
	}

	@Test
	public void shouldNotFindDescendantExceptionMapperFromError() throws Exception {
		ErrorData errorData = ErrorData.builder().setId("someOtherId").build();
		ErrorResponse response = ErrorResponse.builder().setStatus(HttpStatus.BAD_REQUEST_400).setSingleErrorData(errorData).build();
		Optional<ExceptionMapper<?>> mapper = (Optional)exceptionMapperRegistry.findMapperFor(response);
		assertThat(mapper.isPresent()).isTrue();
		assertThat(mapper.get()).isExactlyInstanceOf(IllegalStateExceptionMapper.class);
	}

	private static class SomeException extends Exception {

		private static final long serialVersionUID = 1L;
	}

	private static class SomeIllegalStateException extends IllegalStateException {

		private static final long serialVersionUID = 1L;
	}

	public static class IllegalStateExceptionMapper implements ExceptionMapper<IllegalStateException> {
		@Override
		public ErrorResponse toErrorResponse(IllegalStateException exception) {
			return ErrorResponse.builder().setStatus(HttpStatus.BAD_REQUEST_400).build();
		}

		@Override
		public IllegalStateException fromErrorResponse(ErrorResponse errorResponse) {
			return new IllegalStateException();
		}

		@Override
		public boolean accepts(ErrorResponse errorResponse) {
			return errorResponse.getHttpStatus() == HttpStatus.BAD_REQUEST_400;
		}
	}

	public static class SomeIllegalStateExceptionMapper implements ExceptionMapper<SomeIllegalStateException> {
		@Override
		public ErrorResponse toErrorResponse(SomeIllegalStateException exception) {
			ErrorData errorData = ErrorData.builder().setId("someId").build();
			return ErrorResponse.builder().setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500).setSingleErrorData(errorData).build();
		}

		@Override
		public SomeIllegalStateException fromErrorResponse(ErrorResponse errorResponse) {
			return new SomeIllegalStateException();
		}

		@Override
		public boolean accepts(ErrorResponse errorResponse) {
			if (errorResponse.getHttpStatus() == HttpStatus.BAD_REQUEST_400 && errorResponse.getErrors() != null) {
				Iterator<ErrorData> errors = errorResponse.getErrors().iterator();
				return errors.hasNext() && "someId".equals(errors.next().getId());
			}
			return false;
		}
	}
}
