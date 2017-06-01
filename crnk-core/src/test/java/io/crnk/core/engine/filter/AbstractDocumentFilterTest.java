package io.crnk.core.engine.filter;

import org.junit.Test;
import org.mockito.Mockito;

public class AbstractDocumentFilterTest {

	@Test
	public void test() {
		DocumentFilterContext context = Mockito.mock(DocumentFilterContext.class);
		DocumentFilterChain chain = Mockito.mock(DocumentFilterChain.class);

		AbstractDocumentFilter filter = new AbstractDocumentFilter();
		filter.filter(context, chain);

		Mockito.verify(chain, Mockito.times(1)).doFilter(Mockito.eq(context));
	}
}
