package io.crnk.core.engine.error;

import io.crnk.core.engine.document.ErrorData;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorResponseBuilderTest {

	private static final int STATUS = 500;

	@Test
	public void shouldSetStatus() {
		ErrorResponse response = ErrorResponse.builder()
				.setStatus(STATUS)
				.build();

		assertThat(response.getHttpStatus()).isEqualTo(STATUS);
	}

	@Test
	public void shouldSetSingleErrorData() {
		ErrorResponse response = ErrorResponse.builder()
				.setSingleErrorData(ErrorDataMother.fullyPopulatedErrorData())
				.build();

		assertThat((Iterable<ErrorData>) response.getResponse().getErrors())
				.hasSize(1)
				.containsExactly(ErrorDataMother.fullyPopulatedErrorData());
	}

	@Test
	public void shouldSetErrorDataCollection() {
		ErrorResponse response = ErrorResponse.builder()
				.setErrorData(ErrorDataMother.oneSizeCollectionOfErrorData())
				.build();

		assertThat((Iterable<ErrorData>) response.getResponse().getErrors())
				.hasSize(1)
				.containsExactly(ErrorDataMother.fullyPopulatedErrorData());
	}
}
