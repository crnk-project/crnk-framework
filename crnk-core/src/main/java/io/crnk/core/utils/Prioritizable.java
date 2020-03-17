package io.crnk.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Priority for registered objects like filters and request processors.
 * <p>
 * Objects not implementing Prioritizable have priority 0. If two objects have the same priority, the original registration order is preserved.
 */
public interface Prioritizable {

	public static final int HIGHEST_PRIORITY = 1;
	public static final int HIGH_PRIORITY = 10;
	public static final int MEDIUM_PRIORITY = 50;
	public static final int LOW_PRIORITY = 100;
	public static final int NO_PRIORITY = Integer.MAX_VALUE;

	/**
	 * The higher the returned value, the later it will be used.
	 * e.g. 1 as first priority, 2 as second priority, etc.
	 */
	int getPriority();

	static <T> List<T> prioritze(List<T> list) {
		Map<Object, Integer> indexMap = new HashMap<>();
		int index = -list.size() + 1;
		for (T item : list) {
			indexMap.put(item, index++);
		}

		ArrayList<T> results = new ArrayList<>(list);
		Collections.sort(results, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				int p1 = getPriority(o1);
				int p2 = getPriority(o2);
				if (p1 == p2) {
					p1 = indexMap.get(o1);
					p2 = indexMap.get(o2);
				}
				return p1 - p2;
			}

			private int getPriority(T o1) {
				if (o1 instanceof Prioritizable) {
					return ((Prioritizable) o1).getPriority();
				}
				return NO_PRIORITY;
			}
		});
		return results;
	}
}
