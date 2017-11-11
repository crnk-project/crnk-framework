package io.crnk.core.engine.information.resource;

/**
 * Provides information how a field can be accessed.
 */
public class ResourceFieldAccess {

	private final boolean readable;

	private final boolean postable;

	private final boolean patchable;

	private final boolean sortable;

	private final boolean filterable;

	public ResourceFieldAccess(boolean readable, boolean postable, boolean patchable, boolean sortable, boolean filterable) {
		this.readable = readable;
		this.postable = postable;
		this.patchable = patchable;
		this.sortable = sortable;
		this.filterable = filterable;
	}

	/**
	 * @return true if the field can be read by a GET request.
	 */
	public boolean isReadable() {
		return readable;
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
	 * @return true if the field can be sorted by a GET request.
	 */
	public boolean isSortable() {
		return sortable;
	}

	/**
	 * @return true if the field can be filtered by a GET request.
	 */
	public boolean isFilterable() {
		return filterable;
	}

	public ResourceFieldAccess and(ResourceFieldAccess other) {
		boolean readable = isReadable() && other.isReadable();
		boolean postable = isPostable() && other.isPostable();
		boolean patchable = isPatchable() && other.isPatchable();
		boolean sortable = isSortable() && other.isSortable();
		boolean filterable = isFilterable() && other.isFilterable();
		return new ResourceFieldAccess(readable, postable, patchable, sortable, filterable);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (readable ? 1231 : 1237);
		result = prime * result + (patchable ? 1231 : 1237);
		result = prime * result + (postable ? 1231 : 1237);
		result = prime * result + (sortable ? 1231 : 1237);
		result = prime * result + (filterable ? 1231 : 1237);
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
		ResourceFieldAccess other = (ResourceFieldAccess) obj;
		return readable == other.readable && patchable == other.patchable && postable == other.postable && sortable == other.sortable
				&& filterable == other.filterable;
	}

}
