package io.crnk.client.http.apache;

import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Hopefully that can be removed soon again. But brave enforces its custom builder.
 */
@Deprecated
public interface HttpClientBuilderFactory {

	HttpClientBuilder createBuilder();

}
