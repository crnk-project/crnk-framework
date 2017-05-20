package io.crnk.meta.model;

/**
 * Allows to modifier the behavior of looking up attributes. Used to add
 * "non-existing" virtual attributes to data objects.
 */
public interface MetaAttributeFinder {

	MetaAttribute getAttribute(MetaDataObject meta, String name);

}
