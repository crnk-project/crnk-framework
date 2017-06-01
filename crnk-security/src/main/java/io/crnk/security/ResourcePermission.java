package io.crnk.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.internal.utils.PreconditionUtil;

import java.util.EnumMap;
import java.util.Map;

public class ResourcePermission {

	public static final ResourcePermission EMPTY = new ResourcePermission(false, false, false, false);

	public static final ResourcePermission ALL = new ResourcePermission(true, true, true, true);

	public static final ResourcePermission GET = new ResourcePermission(false, true, false, false);

	public static final ResourcePermission POST = new ResourcePermission(true, false, false, false);

	public static final ResourcePermission PATCH = new ResourcePermission(false, false, true, false);

	public static final ResourcePermission DELETE = new ResourcePermission(false, false, false, true);

	private static final Map<HttpMethod, ResourcePermission> METHODS = new EnumMap<>(HttpMethod.class);

	static {
		METHODS.put(HttpMethod.GET, GET);
		METHODS.put(HttpMethod.PATCH, PATCH);
		METHODS.put(HttpMethod.POST, POST);
		METHODS.put(HttpMethod.DELETE, DELETE);
	}

	private boolean postAllowed;

	private boolean getAllowed;

	private boolean patchAllowed;

	private boolean deleteAllowed;

	/**
	 * constructor for serialization
	 */
	protected ResourcePermission() {
	}

	private ResourcePermission(boolean post, boolean get, boolean patch, boolean delete) {
		super();
		this.postAllowed = post;
		this.getAllowed = get;
		this.patchAllowed = patch;
		this.deleteAllowed = delete;
	}

	public static final ResourcePermission create(boolean push, boolean get, boolean patch, boolean delete) {
		return new ResourcePermission(push, get, patch, delete);
	}

	public static ResourcePermission fromMethod(HttpMethod method) {
		ResourcePermission permission = METHODS.get(method);
		PreconditionUtil.assertNotNull("unknown method", permission);
		return permission;
	}

	public boolean isPostAllowed() {
		return postAllowed;
	}

	public boolean isGetAllowed() {
		return getAllowed;
	}

	public boolean isPatchAllowed() {
		return patchAllowed;
	}

	public boolean isDeleteAllowed() {
		return deleteAllowed;
	}

	@JsonIgnore
	public boolean isEmpty() {
		return !(postAllowed || getAllowed || patchAllowed || deleteAllowed);
	}

	public ResourcePermission or(ResourcePermission other) {
		boolean mergePush = postAllowed || other.postAllowed;
		boolean mergeGet = getAllowed || other.getAllowed;
		boolean mergePatch = patchAllowed || other.patchAllowed;
		boolean mergeDelete = deleteAllowed || other.deleteAllowed;
		return new ResourcePermission(mergePush, mergeGet, mergePatch, mergeDelete);
	}

	public ResourcePermission and(ResourcePermission other) {
		boolean mergePush = postAllowed && other.postAllowed;
		boolean mergeGet = getAllowed && other.getAllowed;
		boolean mergePatch = patchAllowed && other.patchAllowed;
		boolean mergeDelete = deleteAllowed && other.deleteAllowed;
		return new ResourcePermission(mergePush, mergeGet, mergePatch, mergeDelete);
	}

	public ResourcePermission xor(ResourcePermission other) {
		boolean mergePush = postAllowed ^ other.postAllowed;
		boolean mergeGet = getAllowed ^ other.getAllowed;
		boolean mergePatch = patchAllowed ^ other.patchAllowed;
		boolean mergeDelete = deleteAllowed ^ other.deleteAllowed;
		return new ResourcePermission(mergePush, mergeGet, mergePatch, mergeDelete);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (deleteAllowed ? 1231 : 1237);
		result = prime * result + (getAllowed ? 1231 : 1237);
		result = prime * result + (patchAllowed ? 1231 : 1237);
		result = prime * result + (postAllowed ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == ResourcePermission.class) {
			ResourcePermission p = (ResourcePermission) obj;
			return p.deleteAllowed == deleteAllowed &&
					p.getAllowed == getAllowed &&
					p.patchAllowed == patchAllowed &&
					p.postAllowed == postAllowed;

		}
		return false;
	}

	@Override
	public String toString() {
		return "ResourcePermission[post=" + postAllowed + ", get=" + getAllowed + ", patch=" + patchAllowed + ", delete="
				+ deleteAllowed + "]";
	}
}
