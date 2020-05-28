package io.crnk.gen.asciidoc.internal;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Factory.to;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Records;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.Node;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaResource;

public class GraphBuilder {

	public void generate(Collection<MetaResource> resources, File file) throws IOException {
		Graph g = graph("example1");
		g = g.directed();
		g = g.graphAttr().with(Rank.dir(Rank.RankDir.LEFT_TO_RIGHT));

		Map<String, Node> nodeMap = new HashMap<>();
		for (MetaResource resource : resources) {
			String label = Records.rec(resource.getResourceType());
			Node node = node(label);
			nodeMap.put(resource.getResourceType(), node);
		}

		Map<String, Link> linkMap = new HashMap<>();
		for (MetaResource resource : resources) {
			Node node = nodeMap.get(resource.getResourceType());
			for (MetaAttribute attribute : resource.getDeclaredAttributes()) {
				if (!attribute.isAssociation()) continue;
				String key = getKey(attribute);
				if (!linkMap.containsKey(key)) {
					MetaResource oppositeType = (MetaResource) attribute.getType().getElementType();
					Node oppositeNode = nodeMap.get(oppositeType.getResourceType());
					if (oppositeNode != null) {
						Link link = to(oppositeNode).with("a", "b");
						node = node.link(link);
						linkMap.put(key, link);
					}
				}
			}
			g = g.with(node);
		}
		Graphviz.fromGraph(g).height(800).width(1000).render(Format.SVG).toFile(file);
	}

	private String getKey(MetaAttribute attribute) {
		MetaType oppositeType = attribute.getType().getElementType();
		String id = attribute.getParent().getId();
		String oppositeId = oppositeType.getId();
		return attribute.isOwner() ? id + "->" + oppositeId : oppositeId + "->" + id;
	}

}
