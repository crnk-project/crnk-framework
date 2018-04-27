package io.crnk.servlet.resource.repository;

import io.crnk.servlet.resource.model.NodeComment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NodeCommentRepository extends AbstractRepo<NodeComment, Long> {
	private static final Map<Long, NodeComment> NODE_COMMENT_REPO = new ConcurrentHashMap<>();

	@Override
	protected Map<Long, NodeComment> getRepo() {
		return NODE_COMMENT_REPO;
	}
}
