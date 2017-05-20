package io.crnk.core.resource.list;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface LinksContainer {

	ObjectNode getLinks();

	void setLinks(ObjectNode meta);
}
