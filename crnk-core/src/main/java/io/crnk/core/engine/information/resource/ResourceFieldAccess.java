package io.crnk.core.engine.information.resource;

/**
 * Provides information how a field can be accessed.
 */
public class ResourceFieldAccess {

	private boolean postable;
	private boolean patchable;
	private boolean sortable;
	private boolean filterable;

	public ResourceFieldAccess(boolean postable, boolean patchable, boolean sortable, boolean filterable) {
		this.postable = postable;
		this.patchable = patchable;
		this.sortable = sortable;
		this.filterable = filterable;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (patchable ? 1231 : 1237);
		result = prime * result + (postable ? 1231 : 1237);
		result = prime * result + (sortable ? 1231 : 1237);
		result = prime * result + (filterable ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceFieldAccess other = (ResourceFieldAccess) obj;
		return patchable == other.patchable && postable != other.postable && sortable != other.sortable && filterable != other.filterable;
	}

}
