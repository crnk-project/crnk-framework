package io.crnk.meta.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "metaKey", resourcePath = "meta/key")
public class MetaKey extends MetaElement {

	public static final String ID_ELEMENT_SEPARATOR = "-";

	@JsonApiRelation
	private List<MetaAttribute> elements;

	private boolean unique;

	private static String toEmbeddableKeyString(MetaDataObject embType, Object id) {
		StringBuilder builder = new StringBuilder();
		List<? extends MetaAttribute> embAttrs = embType.getAttributes();
		for (int i = 0; i < embAttrs.size(); i++) {
			MetaAttribute embAttr = embAttrs.get(i);
			Object idElement = embAttr.getValue(id);
			if (i > 0) {
				builder.append(ID_ELEMENT_SEPARATOR);
			}
			builder.append(idElement);
		}
		return builder.toString();
	}

	public List<MetaAttribute> getElements() {
		return elements;
	}

	public void setElements(List<MetaAttribute> elements) {
		this.elements = elements;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	@JsonIgnore
	public MetaAttribute getUniqueElement() {
		if (elements.size() != 1) {
			throw new IllegalStateException(getName() + " must contain a single primary key attribute");
		}
		return elements.get(0);
	}

	public String toKeyString(Object id) {
		if (id == null) {
			return null;
		}
		// => support compound keys with unique ids
		PreconditionUtil.assertEquals("compound primary key not supported", 1, elements.size());
		MetaAttribute keyAttr = elements.get(0);
		MetaType keyType = keyAttr.getType();
		if (keyType instanceof MetaDataObject) {
			MetaDataObject embType = (MetaDataObject) keyType;
			return toEmbeddableKeyString(embType, id);
		} else {
			return id.toString();
		}
	}

}
