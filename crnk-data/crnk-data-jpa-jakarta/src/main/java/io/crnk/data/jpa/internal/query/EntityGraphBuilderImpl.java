package io.crnk.data.jpa.internal.query;

import java.util.Set;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Subgraph;

import io.crnk.meta.model.MetaAttributePath;

public class EntityGraphBuilderImpl implements EntityGraphBuilder {

	@Override
	public <T> void build(EntityManager em, Query criteriaQuery, Class<T> entityClass,
						  Set<MetaAttributePath> fetchPaths) {
		EntityGraph<T> graph = em.createEntityGraph(entityClass);
		for (MetaAttributePath fetchPath : fetchPaths) {
			applyFetchPaths(graph, fetchPath);
		}
		criteriaQuery.setHint("jakarta.persistence.fetchgraph", graph);
	}

	private <T> Subgraph<Object> applyFetchPaths(EntityGraph<T> graph, MetaAttributePath fetchPath) {
		if (fetchPath.length() >= 2) {
			// ensure parent is fetched
			MetaAttributePath parentPath = fetchPath.subPath(0, fetchPath.length() - 1);
			Subgraph<Object> parentGraph = applyFetchPaths(graph, parentPath);
			return parentGraph.addSubgraph(fetchPath.getLast().getName());
		} else {
			return graph.addSubgraph(fetchPath.toString());
		}
	}
}
