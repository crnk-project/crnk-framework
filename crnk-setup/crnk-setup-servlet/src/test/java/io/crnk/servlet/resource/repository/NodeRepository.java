package io.crnk.servlet.resource.repository;

import io.crnk.servlet.resource.model.Node;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class NodeRepository extends AbstractRepo<Node, Long> {
	private static final Map<Long, Node> NODE_REPO = new ConcurrentHashMap<>();

	@Override
	protected Map<Long, Node> getRepo() {
		return NODE_REPO;
	}
}
