package io.crnk.servlet.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import io.crnk.servlet.internal.BufferedRequestWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BufferedRequestWrapperTest {

	@Mock
	HttpServletRequest request;

	@Test
	public void onDataInRequestShouldReturnThisData() throws Exception {
		// GIVEN
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("hello".getBytes());
		when(request.getInputStream()).thenReturn(new ServletInputStream() {
			@Override
			public boolean isFinished() {
				return false;
			}

			@Override
			public boolean isReady() {
				return false;
			}

			@Override
			public void setReadListener(ReadListener readListener) {

			}

			@Override
			public int read() throws IOException {
				return byteArrayInputStream.read();
			}
		});

		// WHEN
		BufferedRequestWrapper sut = new BufferedRequestWrapper(request);
		ServletInputStream inputStream = sut.getInputStream();

		// THEN
		assertThat(inputStream).hasSameContentAs(new ByteArrayInputStream("hello".getBytes()));
	}

	@Test
	public void onNullDataInRequestShouldReturnNull() throws Exception {
		// GIVEN
		when(request.getInputStream()).thenReturn(null);

		// WHEN
		BufferedRequestWrapper sut = new BufferedRequestWrapper(request);
		ServletInputStream inputStream = sut.getInputStream();

		// THEN
		assertThat(inputStream).isNull();
	}
}
