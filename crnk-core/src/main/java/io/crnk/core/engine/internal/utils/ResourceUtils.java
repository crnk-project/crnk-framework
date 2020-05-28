package io.crnk.core.engine.internal.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.information.resource.ResourceInformation;

public class ResourceUtils {


	/**
	 * allow relationships to make use of of ResourceIdentifier rather than proper types
	 */
	public static Object toTypedId(ResourceInformation resourceInformation, Object id) {
		if (id instanceof ResourceIdentifier && resourceInformation.getIdField().getType() != ResourceIdentifier.class) {
			String strId = ((ResourceIdentifier) id).getId();
			return resourceInformation.parseIdString(strId);
		}
		return id;
	}

	/**
	 * allow relationships to make use of of ResourceIdentifier rather than proper types
	 */
	public static Collection toTypedIds(ResourceInformation resourceInformation, Collection ids) {
		if (!ids.isEmpty()) {
			Object firstId = ids.iterator().next();
			if (firstId instanceof ResourceIdentifier && resourceInformation.getIdField().getType() != ResourceIdentifier.class) {
				List typedIds = new ArrayList();
				for (Object id : ids) {
					String strId = ((ResourceIdentifier) id).getId();
					typedIds.add(resourceInformation.parseIdString(strId));
				}
				return typedIds;
			}
		}
		return ids;
	}

}
