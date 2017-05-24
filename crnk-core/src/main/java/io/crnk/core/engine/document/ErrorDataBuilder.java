package io.crnk.core.engine.document;

import java.util.HashMap;
import java.util.Map;

public class ErrorDataBuilder {
	private String id;
	private String aboutLink;
	private String status;
	private String code;
	private String title;
	private String detail;
	private String sourcePointer;
	private String sourceParameter;
	private Map<String, Object> meta;

	/**
	 * @param id A unique identifier for this particular occurrence of the problem.
	 * @return ErrorDataBuilder
	 */
	public ErrorDataBuilder setId(@SuppressWarnings("SameParameterValue") String id) {
		this.id = id;
		return this;
	}

	/**
	 * A link that leads to further details about this particular occurrence of the problem. Wrapped in "links" object.
	 *
	 * @param aboutLink information about an error
	 * @return builder instance
	 */
	public ErrorDataBuilder setAboutLink(@SuppressWarnings("SameParameterValue") String aboutLink) {
		this.aboutLink = aboutLink;
		return this;
	}

	/**
	 * The HTTP status code applicable to this problem, expressed as a string value.
	 *
	 * @param status HTTP status code
	 * @return builder instance
	 */
	public ErrorDataBuilder setStatus(String status) {
		this.status = status;
		return this;
	}

	/**
	 * An application-specific error code, expressed as a string value.
	 *
	 * @param code application-specific error code
	 * @return builder instance
	 */
	public ErrorDataBuilder setCode(@SuppressWarnings("SameParameterValue") String code) {
		this.code = code;
		return this;
	}

	/**
	 * A short, human-readable summary of the problem.
	 * It SHOULD NOT change from occurrence to occurrence of the problem, except for purposes of localization.
	 *
	 * @param title title of an error
	 * @return builder instance
	 */
	public ErrorDataBuilder setTitle(String title) {
		this.title = title;
		return this;
	}

	/**
	 * A human-readable explanation specific to this occurrence of the problem.
	 *
	 * @param detail error details
	 * @return builder instance
	 */
	public ErrorDataBuilder setDetail(String detail) {
		this.detail = detail;
		return this;
	}

	/**
	 * A JSON Pointer [RFC6901] to the associated entity in the request resource
	 * [e.g. "/data" for a primary data object, or "/data/attributes/title" for a specific attribute].
	 * <p>
	 * Wrapped in "source" object.
	 *
	 * @param sourcePointer pointer to the error origin
	 * @return builder instance
	 */
	public ErrorDataBuilder setSourcePointer(String sourcePointer) {
		this.sourcePointer = sourcePointer;
		return this;
	}

	/**
	 * A string indicating which query parameter caused the error.
	 * <p>
	 * Wrapped in "source" object.
	 *
	 * @param sourceParameter source parameter
	 * @return builder instance
	 */
	public ErrorDataBuilder setSourceParameter(String sourceParameter) {
		this.sourceParameter = sourceParameter;
		return this;
	}

	/**
	 * A meta object containing non-standard meta-information about the error.
	 *
	 * @param meta meta information
	 * @return builder instance
	 */
	public ErrorDataBuilder setMeta(@SuppressWarnings("SameParameterValue") Map<String, Object> meta) {
		this.meta = meta;
		return this;
	}

	public ErrorDataBuilder addMetaField(String key, Object value) {
		if (meta == null) {
			meta = new HashMap<>();
		}
		meta.put(key, value);
		return this;
	}

	public ErrorData build() {
		ErrorData data = new ErrorData();
		data.id = id;
		data.aboutLink = aboutLink;
		data.status = status;
		data.code = code;
		data.title = title;
		data.detail = detail;
		data.sourcePointer = sourcePointer;
		data.sourceParameter = sourcePointer;
		data.meta = meta;
		return data;
	}
}