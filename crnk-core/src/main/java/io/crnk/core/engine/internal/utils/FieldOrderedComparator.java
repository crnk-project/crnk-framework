package io.crnk.core.engine.internal.utils;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.crnk.core.engine.information.resource.ResourceField;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Is used when a resource class is annotated with {@link JsonPropertyOrder}.
 */
public class FieldOrderedComparator implements Comparator<ResourceField> {

	private final Map<String, Integer> fieldNames;
	private final boolean alphabetic;

	public FieldOrderedComparator(String[] orderedValues, boolean alphabetic) {
		this.fieldNames = new HashMap<>();
		this.alphabetic = alphabetic;

		init(orderedValues);
	}

	private void init(String[] orderedValues) {
		for (int i = 0; i < orderedValues.length; i++) {
			this.fieldNames.put(orderedValues[i], i);
		}
	}

	@Override
	public int compare(ResourceField o1, ResourceField o2) {
		if (fieldNames.containsKey(o1.getJsonName())) {
			if (fieldNames.containsKey(o2.getJsonName())) {
				return fieldNames.get(o1.getJsonName()) - fieldNames.get(o2.getJsonName());
			} else {
				return -1;
			}
		} else {
			if (alphabetic) {
				return o1.getJsonName().compareToIgnoreCase(o2.getJsonName());
			} else {
				return 1;
			}
		}
	}
}
