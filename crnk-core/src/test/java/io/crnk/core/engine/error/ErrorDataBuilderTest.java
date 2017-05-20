package io.crnk.core.engine.error;

import io.crnk.core.engine.document.ErrorData;
import org.assertj.core.data.MapEntry;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorDataBuilderTest {

	@Test
	public void shouldSetDetail() throws Exception {
		ErrorData error = ErrorData.builder()
				.setDetail(ErrorDataMother.DETAIL)
				.build();
		assertThat(error.getDetail()).isEqualTo(ErrorDataMother.DETAIL);
	}

	@Test
	public void shouldSetCode() throws Exception {
		ErrorData error = ErrorData.builder()
				.setCode(ErrorDataMother.CODE)
				.build();
		assertThat(error.getCode()).isEqualTo(ErrorDataMother.CODE);
	}

	@Test
	public void shouldSetAboutLink() throws Exception {
		ErrorData error = ErrorData.builder()
				.setAboutLink(ErrorDataMother.ABOUT_LINK)
				.build();
		assertThat(error.getAboutLink()).isEqualTo(ErrorDataMother.ABOUT_LINK);
	}

	@Test
	public void shouldSetId() throws Exception {
		ErrorData error = ErrorData.builder()
				.setId(ErrorDataMother.ID)
				.build();
		assertThat(error.getId()).isEqualTo(ErrorDataMother.ID);
	}

	@Test
	public void shouldSetStatus() throws Exception {
		ErrorData error = ErrorData.builder()
				.setStatus(ErrorDataMother.STATUS)
				.build();
		assertThat(error.getStatus()).isEqualTo(ErrorDataMother.STATUS);
	}

	@Test
	public void shouldSetTitle() throws Exception {
		ErrorData error = ErrorData.builder()
				.setTitle(ErrorDataMother.TITLE)
				.build();
		assertThat(error.getTitle()).isEqualTo(ErrorDataMother.TITLE);
	}

	@Test
	public void shouldSetSourcePointer() throws Exception {
		ErrorData error = ErrorData.builder()
				.setSourcePointer(ErrorDataMother.POINTER)
				.build();
		assertThat(error.getSourcePointer()).isEqualTo(ErrorDataMother.POINTER);
	}

	@Test
	public void shouldSetPaths() throws Exception {
		ErrorData error = ErrorData.builder()
				.setSourceParameter(ErrorDataMother.PARAMETER)
				.build();
		assertThat(error.getSourceParameter()).isEqualTo(ErrorDataMother.PARAMETER);
	}

	@Test
	public void shouldSetMeta() throws Exception {
		ErrorData error = ErrorData.builder()
				.setMeta(ErrorDataMother.META)
				.build();
		assertThat(error.getMeta()).contains(MapEntry.entry(ErrorDataMother.META_KEY, ErrorDataMother.META_VALUE));
	}
}