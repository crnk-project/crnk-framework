package io.crnk.data.jpa.internal.query;

import io.crnk.meta.model.MetaAttributePath;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Subgraph;
import java.util.Set;

public class EntityGraphBuilderImpl implements EntityGraphBuilder {

	@Override
	public <T> void build(EntityManager em, Query criteriaQuery, Class<T> entityClass,
						  Set<MetaAttributePath> fetchPaths) {
		EntityGraph<T> graph = em.createEntityGraph(entityClass);
		for (MetaAttributePath fetchPath : fetchPaths) {
			applyFetchPaths(graph, fetchPath);
		}
		criteriaQuery.setHint("javax.persistence.fetchgraph", graph);
	}

	private <T> Subgraph<Object> applyFetchPaths(EntityGraph<T> graph, MetaAttributePath fetchPath) {
		if (fetchPath.length() >= 2) {
			// ensure parent is fetched
			MetaAttributePath parentPath = fetchPath.subPath(0, fetchPath.length() - 1);
			Subgraph<Object> parentGraph = applyFetchPaths(graph, parentPath);
			return parentGraph.addSubgraph(fetchPath.toString());
		} else {
			return graph.addSubgraph(fetchPath.toString());
		}
	}
}
