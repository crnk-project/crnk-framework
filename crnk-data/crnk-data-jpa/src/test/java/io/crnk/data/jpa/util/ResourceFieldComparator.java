package io.crnk.data.jpa.util;

import io.crnk.core.engine.information.resource.ResourceField;

import java.util.Comparator;

public class ResourceFieldComparator implements Comparator<ResourceField> {

	public static final ResourceFieldComparator INSTANCE = new ResourceFieldComparator();

	@Override
	public int compare(ResourceField o1, ResourceField o2) {
		return o1.getJsonName().compareTo(o2.getJsonName());
	}
}