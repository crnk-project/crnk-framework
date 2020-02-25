package io.crnk.core.resource.links;


/**
 * Interface declaration for any LinksInformation object holding
 * paging information. {@link DefaultPagedLinksInformation}
 * provides a default implementation.
 */
public interface PagedLinksInformation extends LinksInformation {

	Link getFirst();

	void setFirst(Link first);

	default void setFirst(String first) {
		setFirst(new DefaultLink(first));
	}

	Link getLast();

	void setLast(Link last);

	default void setLast(String last) {
		setLast(new DefaultLink(last));
	}

	Link getNext();

	void setNext(Link next);

	default void setNext(String next) {
		setNext(new DefaultLink(next));
	}

	Link getPrev();

	void setPrev(Link prev);

	default void setPrev(String prev) {
		setPrev(new DefaultLink(prev));
	}
}
