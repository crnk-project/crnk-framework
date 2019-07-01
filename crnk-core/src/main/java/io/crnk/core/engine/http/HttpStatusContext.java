package io.crnk.core.engine.http;

import io.crnk.core.engine.document.Document;

public interface HttpStatusContext {

	Document getResponseDocument();

	HttpMethod getMethod();

}
