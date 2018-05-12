package io.crnk.servlet.resource.repository;

import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.RelationshipRepository;
import io.crnk.servlet.resource.model.Node;

public class NodeToNodeRepository implements RelationshipRepository<Node, Long, Node, Long> {

	private NodeRepository nodeRepository = new NodeRepository();

	@Override
	public void setRelation(Node source, Long targetId, String fieldName) {

	}

	@Override
	public void setRelations(Node source, Iterable<Long> targetIds, String fieldName) {

	}

	@Override
	public void addRelations(Node source, Iterable<Long> targetIds, String fieldName) {

	}

	@Override
	public void removeRelations(Node source, Iterable<Long> targetIds, String fieldName) {

	}

	@Override
	public Node findOneTarget(Long sourceId, String fieldName, QueryParams queryParams) {
		Node node = nodeRepository.findOne(sourceId, null);
		return node.getParent();
	}

	@Override
	public Iterable<Node> findManyTargets(Long sourceId, String fieldName, QueryParams queryParams) {
		Node node = nodeRepository.findOne(sourceId, null);
		return node.getChildren();
	}
}
