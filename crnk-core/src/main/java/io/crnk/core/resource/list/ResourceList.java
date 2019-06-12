package io.crnk.core.resource.list;

import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;

import java.util.List;

/**
 * Holds links and meta information next to the actual list. Can be returned by findAll and findRelations repository operation.
 */
public interface ResourceList<T> extends List<T> {

	LinksInformation getLinks();

	MetaInformation getMeta();

	/**
	 * @param linksClass to return
	 * @return links of the given type or null if not available
	 */
	<L extends LinksInformation> L getLinks(Class<L> linksClass);

	/**
	 * @param metaClass to return
	 * @return meta information of the given type or null if not available
	 */
	<M extends MetaInformation> M getMeta(Class<M> metaClass);
}
