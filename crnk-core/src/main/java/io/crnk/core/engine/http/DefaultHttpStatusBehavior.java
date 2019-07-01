package io.crnk.core.engine.http;

import io.crnk.core.engine.document.Document;
import io.crnk.core.utils.Prioritizable;

public class DefaultHttpStatusBehavior implements HttpStatusBehavior, Prioritizable {

	private static final int PRIORITY = 1000;

	@Override
	public Integer getStatus(HttpStatusContext context) {
		HttpMethod method = context.getMethod();
		Document responseDocument = context.getResponseDocument();

		if (responseDocument == null) {
			return HttpStatus.NO_CONTENT_204;
		}
		if (method == HttpMethod.POST) {
			return HttpStatus.CREATED_201;
		}
		return HttpStatus.OK_200;
	}

	@Override
	public int getPriority() {
		// make it have a low priority. behaviors not implementing priorizable will come first.
		return PRIORITY;
	}
}
