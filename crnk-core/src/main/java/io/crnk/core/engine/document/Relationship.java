package io.crnk.core.engine.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.engine.internal.jackson.NullableSerializer;
import io.crnk.core.engine.internal.jackson.RelationshipDataDeserializer;
import io.crnk.core.resource.list.LinksContainer;
import io.crnk.core.resource.meta.MetaContainer;
import io.crnk.core.utils.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
		this.data = Nullable.of((Object) resourceId);
	}

	public Relationship(List<ResourceIdentifier> resourceIds) {
		this.data = Nullable.of((Object) resourceIds);
	}

	@Override
	public ObjectNode getLinks() {
		return links;
	}

	@Override
	public void setLinks(ObjectNode links) {
		this.links = links;
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
		if (data == null) {
			throw new NullPointerException("make use of Nullable");
		}
		if (data.isPresent()) {
			Object value = data.get();
			if (value instanceof Collection) {
				Collection<?> col = (Collection<?>) value;
				if (!col.isEmpty()) {
					Object object = col.iterator().next();
					if (object instanceof Resource) {
						throw new IllegalArgumentException();
					}
				}
			} else if (value != null && value instanceof Resource) {
				throw new IllegalArgumentException();
			}

		}

		this.data = data;
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
		if (!(obj instanceof Relationship))
			return false;
		Relationship other = (Relationship) obj;
		return Objects.equals(data, other.data) // NOSONAR
				&& Objects.equals(meta, other.meta) && Objects.equals(links, other.links);
	}
}