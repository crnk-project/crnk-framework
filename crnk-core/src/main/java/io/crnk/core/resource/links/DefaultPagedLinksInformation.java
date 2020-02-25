package io.crnk.core.resource.links;

import com.fasterxml.jackson.annotation.JsonInclude;

public class DefaultPagedLinksInformation implements PagedLinksInformation {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Link first;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Link last;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Link next;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Link prev;

	@Override
	public Link getFirst() {
		return first;
	}

	@Override
	public void setFirst(Link first) {
		this.first = first;
	}

	@Override
	public Link getLast() {
		return last;
	}

	@Override
	public void setLast(Link last) {
		this.last = last;
	}

	@Override
	public Link getNext() {
		return next;
	}

	@Override
	public void setNext(Link next) {
		this.next = next;
	}

	@Override
	public Link getPrev() {
		return prev;
	}

	@Override
	public void setPrev(Link prev) {
		this.prev = prev;
	}
}
