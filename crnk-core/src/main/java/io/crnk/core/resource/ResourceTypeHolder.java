package io.crnk.core.resource;

/**
 * Can be implemented by resources to carry the resource type. Most notable implementation is
 * {@link io.crnk.core.engine.document.Resource}. Usually the implementation class of a resource
 * uniquely identifies a {@link io.crnk.core.engine.registry.RegistryEntry}. If not, this
 * interfaces can be implemented.
 * <p>
 * Currently experimental and not yet fully supported.
 */
public interface ResourceTypeHolder {

	String TYPE_ATTRIBUTE = "type";

	String getType();

	void setType(String type);

}
