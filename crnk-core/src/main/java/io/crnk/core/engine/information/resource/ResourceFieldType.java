package io.crnk.core.engine.information.resource;

import java.util.ArrayList;
import java.util.List;

public enum ResourceFieldType {
	ID, ATTRIBUTE, RELATIONSHIP, META_INFORMATION, LINKS_INFORMATION;

	/**
	 * no replacement, of no use...
	 */
	@Deprecated
	public static ResourceFieldType get(boolean id, boolean linksInfo, boolean metaInfo, boolean association) {
		if (id) {
			return ResourceFieldType.ID;
		}
		if (association) {
			return ResourceFieldType.RELATIONSHIP;
		}
		if (linksInfo) {
			return ResourceFieldType.LINKS_INFORMATION;
		}
		if (metaInfo) {
			return ResourceFieldType.META_INFORMATION;
		}
		return ResourceFieldType.ATTRIBUTE;
	}

	public List<ResourceField> filter(List<ResourceField> fields) {
		ArrayList<ResourceField> results = new ArrayList<>();
		for (ResourceField field : fields) {
			if (field.getResourceFieldType() == this) {
				results.add(field);
			}
		}
		return results;
	}
}