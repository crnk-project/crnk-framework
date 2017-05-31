package io.crnk.core.resource.links;


/**
 * Interface declaration for any LinksInformation object holding
 * paging information. {@link DefaultPagedLinksInformation}
 * provides a default implementation.
 */
public interface PagedLinksInformation extends LinksInformation {

	String getFirst();

	void setFirst(String first);

	String getLast();

	void setLast(String last);

	String getNext();

	void setNext(String next);

	String getPrev();

	void setPrev(String prev);
}
