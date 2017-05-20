package io.crnk.core.resource.list;

import io.crnk.core.engine.internal.utils.CastableInformation;
import io.crnk.core.engine.internal.utils.WrappedList;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation for {@link ResourceList}
 *
 * @param <T> resource type
 */
public class DefaultResourceList<T> extends WrappedList<T> implements ResourceList<T> {

	protected LinksInformation links;

	protected MetaInformation meta;

	public DefaultResourceList() {
		this(null, null);
	}

	public DefaultResourceList(MetaInformation meta, LinksInformation links) {
		this(new ArrayList<T>(), meta, links);
	}

	public DefaultResourceList(List<T> list, MetaInformation meta, LinksInformation links) {
		super(list);
		this.meta = meta;
		this.links = links;
	}

	@SuppressWarnings("unchecked")
	public <L extends LinksInformation> L getLinks(Class<L> linksClass) {
		if (links == null) {
			return null;
		}
		if (linksClass.isInstance(links)) {
			return (L) links;
		} else if (links instanceof CastableInformation) {
			return ((CastableInformation<LinksInformation>) links).as(linksClass);
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public <M extends MetaInformation> M getMeta(Class<M> metaClass) {
		if (meta == null) {
			return null;
		}
		if (metaClass.isInstance(meta)) {
			return (M) meta;
		} else if (meta instanceof CastableInformation) {
			return ((CastableInformation<MetaInformation>) meta).as(metaClass);
		} else {
			return null;
		}
	}

	@Override
	public LinksInformation getLinks() {
		return links;
	}

	public void setLinks(LinksInformation links) {
		this.links = links;
	}

	@Override
	public MetaInformation getMeta() {
		return meta;
	}

	public void setMeta(MetaInformation meta) {
		this.meta = meta;
	}

	/**
	 * @Deprecated Make use of getLinks.
	 */
	@Deprecated
	public LinksInformation getLinksInformation() {
		return getLinks();
	}

	/**
	 * @Deprecated Make use of getMeta.
	 */
	@Deprecated
	public MetaInformation getMetaInformation() {
		return getMeta();
	}

	/**
	 * @Deprecated Make use of getLinks.
	 */
	@Deprecated
	public <L extends LinksInformation> L getLinksInformation(Class<L> linksClass) {
		return getLinks(linksClass);
	}

	/**
	 * @Deprecated Make use of getMeta.
	 */
	@Deprecated
	public <M extends MetaInformation> M getMetaInformation(Class<M> metaClass) {
		return getMeta(metaClass);
	}
}
