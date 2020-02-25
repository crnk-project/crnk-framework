package io.crnk.core.resource.links;

import com.fasterxml.jackson.annotation.JsonInclude;

public class DefaultLink implements Link {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String href;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String rel;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String anchor;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private LinkParams params;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String describedby;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private LinkMeta meta;

	public DefaultLink() {
		this(null);
	}

	public DefaultLink(String href) {
		this.href = href;
	}

	@Override
	public String getHref() {
		return href;
	}

	@Override
	public void setHref(String href) {
		this.href = href;
	}

	@Override
	public String getRel() {
		return rel;
	}

	@Override
	public void setRel(String rel) {
		this.rel = rel;
	}

	@Override
	public String getAnchor() {
		return anchor;
	}

	@Override
	public void setAnchor(String anchor) {
		this.anchor = anchor;
	}

	@Override
	public LinkParams getParams() {
		return params;
	}

	@Override
	public void setParams(LinkParams params) {
		this.params = params;
	}

	@Override
	public String getDescribedby() {
		return describedby;
	}

	@Override
	public void setDescribedby(String describedby) {
		this.describedby = describedby;
	}

	@Override
	public LinkMeta getMeta() {
		return meta;
	}

	@Override
	public void setMeta(LinkMeta meta) {
		this.meta = meta;
	}
}
