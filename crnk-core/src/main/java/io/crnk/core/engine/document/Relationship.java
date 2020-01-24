package io.crnk.core.engine.document;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.engine.internal.jackson.NullableSerializer;
import io.crnk.core.engine.internal.jackson.RelationshipDataDeserializer;
import io.crnk.core.engine.internal.utils.CompareUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.resource.list.LinksContainer;
import io.crnk.core.resource.meta.MetaContainer;
import io.crnk.core.utils.Nullable;

public class Relationship implements MetaContainer, LinksContainer {

	@JsonInclude(Include.NON_EMPTY)
	@JsonSerialize(using = NullableSerializer.class)
	@JsonDeserialize(using = RelationshipDataDeserializer.class)
	private Nullable<Object> data = Nullable.empty();

	@JsonInclude(Include.NON_NULL)
	private ObjectNode links;

	@JsonInclude(Include.NON_NULL)
	private ObjectNode meta;

	public Relationship() {
	}

	public Relationship(ResourceIdentifier resourceId) {
		this.data = Nullable.of(resourceId);
	}

	public Relationship(List<ResourceIdentifier> resourceIds) {
		this.data = Nullable.of(resourceIds);
	}

	@Override
	public ObjectNode getMeta() {
		return meta;
	}

	@Override
	public void setMeta(ObjectNode meta) {
		this.meta = meta;
	}

	public Nullable<Object> getData() {
		return data;
	}

	public void setData(Nullable<Object> data) {
		PreconditionUtil.verify(data != null, "make use of Nullable, null not allowed");
		if (data.isPresent()) {
			Object value = data.get();
			if (value instanceof Collection) {
				Collection<?> col = (Collection<?>) value;
				if (!col.isEmpty()) {
					Object object = col.iterator().next();
					PreconditionUtil.verify(object instanceof ResourceIdentifier, "relationship data must be an instanceof of ResourceIdentifier, got %s", object);
					PreconditionUtil.verify(!(object instanceof Resource), "relationship data cannot be a Resource, must be a ResourceIdentifier");
				}
			}
			else {
				PreconditionUtil.verify(value == null || value
						instanceof ResourceIdentifier, "value must be a ResourceIdentifier, null or collection, got %s", value);
			}

		}

		this.data = data;
	}

	@Override
	public ObjectNode getLinks() {
		return links;
	}

	@Override
	public void setLinks(ObjectNode links) {
		this.links = links;
	}

	@JsonIgnore
	public Nullable<ResourceIdentifier> getSingleData() {
		return (Nullable<ResourceIdentifier>) (Nullable) data;
	}

	@JsonIgnore
	public Nullable<List<ResourceIdentifier>> getCollectionData() {
		if (!data.isPresent()) {
			return Nullable.empty();
		}
		Object value = data.get();
		if (!(value instanceof Iterable)) {
			return Nullable.of((Collections.singletonList((ResourceIdentifier) value)));
		}
		return Nullable.of((List<ResourceIdentifier>) value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(data, links, meta);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(getClass())) {
			return false;
		}
		Relationship other = (Relationship) obj;
		return CompareUtils.isEquals(data, other.data) // NOSONAR
				&& CompareUtils.isEquals(meta, other.meta) && CompareUtils.isEquals(links, other.links);
	}
}