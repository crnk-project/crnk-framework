package io.crnk.core.engine.error;

import io.crnk.core.engine.document.ErrorData;
import org.assertj.core.data.MapEntry;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorDataBuilderTest {

	@Test
	public void shouldSetDetail() {
		ErrorData error = ErrorData.builder()
				.setDetail(ErrorDataMother.DETAIL)
				.build();
		assertThat(error.getDetail()).isEqualTo(ErrorDataMother.DETAIL);
	}

	@Test
	public void shouldSetCode() {
		ErrorData error = ErrorData.builder()
				.setCode(ErrorDataMother.CODE)
				.build();
		assertThat(error.getCode()).isEqualTo(ErrorDataMother.CODE);
	}

	@Test
	public void shouldSetAboutLink() {
		ErrorData error = ErrorData.builder()
				.setAboutLink(ErrorDataMother.ABOUT_LINK)
				.build();
		assertThat(error.getAboutLink()).isEqualTo(ErrorDataMother.ABOUT_LINK);
	}

	@Test
	public void shouldSetId() {
		ErrorData error = ErrorData.builder()
				.setId(ErrorDataMother.ID)
				.build();
		assertThat(error.getId()).isEqualTo(ErrorDataMother.ID);
	}

	@Test
	public void shouldSetStatus() {
		ErrorData error = ErrorData.builder()
				.setStatus(ErrorDataMother.STATUS)
				.build();
		assertThat(error.getStatus()).isEqualTo(ErrorDataMother.STATUS);
	}

	@Test
	public void shouldSetTitle() {
		ErrorData error = ErrorData.builder()
				.setTitle(ErrorDataMother.TITLE)
				.build();
		assertThat(error.getTitle()).isEqualTo(ErrorDataMother.TITLE);
	}

	@Test
	public void shouldSetSourcePointer() {
		ErrorData error = ErrorData.builder()
				.setSourcePointer(ErrorDataMother.POINTER)
				.build();
		assertThat(error.getSourcePointer()).isEqualTo(ErrorDataMother.POINTER);
	}

	@Test
	public void shouldSetPaths() {
		ErrorData error = ErrorData.builder()
				.setSourceParameter(ErrorDataMother.PARAMETER)
				.build();
		assertThat(error.getSourceParameter()).isEqualTo(ErrorDataMother.PARAMETER);
	}

	@Test
	public void shouldSetMeta() {
		ErrorData error = ErrorData.builder()
				.setMeta(ErrorDataMother.META)
				.build();
		assertThat(error.getMeta()).contains(MapEntry.entry(ErrorDataMother.META_KEY, ErrorDataMother.META_VALUE));
	}

	@Test
	public void shouldAddMeta() {
		ErrorData error = ErrorData.builder()
				.addMetaField("a", "b")
				.addMetaField("c", "d")
				.build();
		assertThat(error.getMeta()).contains(MapEntry.entry("a", "b"));
		assertThat(error.getMeta()).contains(MapEntry.entry("c", "d"));
	}
}