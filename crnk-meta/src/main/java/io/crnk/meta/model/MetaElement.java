package io.crnk.meta.model;

import java.util.*;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.crnk.core.engine.internal.utils.ExceptionUtil;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.SerializeType;

/**
 * Root of the meta model. Elements are identified by id. Have a name. All except the root have a parent and
 * may have children.
 * <p>
 * Relationships are defined with LookupIncludeBehavior.AUTOMATICALLY_ALWAYS to allow to customize the
 * meta model towards the request (hide elements, update mutation information, etc.)
 */
@JsonApiResource(type = "meta/element")
public class MetaElement implements Cloneable {

	@JsonApiId
	private String id;

	private String name;

	@JsonApiRelation(serialize = SerializeType.LAZY, opposite = "children", lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS)
	private MetaElement parent;

	@JsonApiRelation(opposite = "parent", lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS)
	private List<MetaElement> children = new ArrayList<>();

	/**
	 * Additional feature flags for meta elements
	 */
	private Map<String, MetaNature> natures = new HashMap<>();

	public MetaElement getParent() {
		return parent;
	}

	public void setParent(MetaElement parent) {
		this.parent = parent;
	}

	public List<MetaElement> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public void setChildren(List<MetaElement> children) {
		this.children = children;
	}

	public void addChild(MetaElement child) {
		children.add(child);
	}

	public Map<String, MetaNature> getNatures() {
		return natures;
	}

	public void setNatures(Map<String, MetaNature> natures) {
		this.natures = natures;
	}

	public MetaType asType() {
		if (!(this instanceof MetaType)) {
			throw new IllegalStateException(getName() + " not a MetaEntity");
		}
		return (MetaType) this;
	}

	public MetaDataObject asDataObject() {
		if (!(this instanceof MetaDataObject)) {
			throw new IllegalStateException(getName() + " not a MetaDataObject");
		}
		return (MetaDataObject) this;
	}

	public void setParent(MetaElement parent, boolean attach) {
		this.parent = parent;

		if (parent != null && attach) {
			parent.addChild(this);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=" + getName() + "]";
	}

	public final String getId() {
		if (id == null) {
			throw new UnsupportedOperationException("id not available for " + toString());
		}
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonIgnore
	public boolean hasId() {
		return id != null;
	}

	public MetaElement duplicate() {
		return ExceptionUtil.wrapCatchedExceptions(new Callable<MetaElement>() {

			@Override
			public MetaElement call() throws Exception {
				return (MetaElement) MetaElement.this.clone();
			}
		});
	}
}
