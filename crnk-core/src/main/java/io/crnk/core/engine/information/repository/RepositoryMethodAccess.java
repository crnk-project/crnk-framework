package io.crnk.core.engine.information.repository;

/**
 * Provides information how a field can be accessed.
 */
public class RepositoryMethodAccess {

	public static final RepositoryMethodAccess ALL = new RepositoryMethodAccess(true, true, true, true);

	private final boolean postable;

	private final boolean patchable;

	private final boolean readable;

	private final boolean deletable;

	public RepositoryMethodAccess(boolean postable, boolean patchable, boolean readable, boolean deletable) {
		this.postable = postable;
		this.patchable = patchable;
		this.readable = readable;
		this.deletable = deletable;
	}

	/**
	 * @return true if the field can be set by a POST request.
	 */
	public boolean isPostable() {
		return postable;
	}

	/**
	 * @return true if the field can be changed by a PATCH request.
	 */
	public boolean isPatchable() {
		return patchable;
	}

	/**
	 * @return true if the field can be read by a GET request.
	 */
	public boolean isReadable() {
		return readable;
	}

	/**
	 * @return true if the field can be deleted by a DELETE request.
	 */
	public boolean isDeletable() {
		return deletable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (patchable ? 1231 : 1237);
		result = prime * result + (postable ? 1231 : 1237);
		result = prime * result + (readable ? 1231 : 1237);
		result = prime * result + (deletable ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RepositoryMethodAccess other = (RepositoryMethodAccess) obj;
		return patchable == other.patchable && postable == other.postable && readable == other.readable && deletable == other
				.deletable;
	}
}
