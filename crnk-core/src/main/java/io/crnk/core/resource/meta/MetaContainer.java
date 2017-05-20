package io.crnk.core.resource.meta;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface MetaContainer {

	ObjectNode getMeta();

	void setMeta(ObjectNode meta);
}
