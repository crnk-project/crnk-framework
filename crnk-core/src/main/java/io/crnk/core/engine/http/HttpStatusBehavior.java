package io.crnk.core.engine.http;

/**
 * Used to compute HTTP status codes for responses. By default covered by {@link DefaultHttpStatusBehavior}.
 * Applications may register additional status behaviors with
 * {@link io.crnk.core.module.Module.ModuleContext#addHttpStatusBehavior(HttpStatusBehavior)}.
 * The class may implement {@link io.crnk.core.utils.Prioritizable}. The first behavior returning a
 * non-null status code is used.
 * <p>
 * This class is (at least currently) not involved
 * for providing HTTP error status codes, this is done by {@link io.crnk.core.engine.error.ExceptionMapper}.
 */
public interface HttpStatusBehavior {

	/**
	 * @return status code or null if this behavior cannot provide one.
	 */
	Integer getStatus(HttpStatusContext context);

}
