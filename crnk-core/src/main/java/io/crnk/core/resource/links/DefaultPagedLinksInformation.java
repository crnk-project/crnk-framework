package io.crnk.core.resource.links;

import com.fasterxml.jackson.annotation.JsonInclude;

public class DefaultPagedLinksInformation implements PagedLinksInformation {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String first;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String last;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String next;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String prev;

	@Override
	public String getFirst() {
		return first;
	}

	@Override
	public void setFirst(String first) {
		this.first = first;
	}

	@Override
	public String getLast() {
		return last;
	}

	@Override
	public void setLast(String last) {
		this.last = last;
	}

	@Override
	public String getNext() {
		return next;
	}

	@Override
	public void setNext(String next) {
		this.next = next;
	}

	@Override
	public String getPrev() {
		return prev;
	}

	@Override
	public void setPrev(String prev) {
		this.prev = prev;
	}
}
