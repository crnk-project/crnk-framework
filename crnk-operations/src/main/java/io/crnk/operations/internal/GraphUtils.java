package io.crnk.operations.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

public class GraphUtils {

	private GraphUtils(){
	}

	public static List<Node> sort(Collection<Node> nodes) {
		//result <- Empty list that will contain the sorted elements
		ArrayList<Node> result = new ArrayList<>();

		//S <- Set of all nodes with no incoming edges
		LinkedHashSet<Node> noIncomingSet = new LinkedHashSet<>();
		for (Node n : nodes) {
			if (n.inEdges.size() == 0) {
				noIncomingSet.add(n);
			}
		}

		//while S is non-empty do
		while (!noIncomingSet.isEmpty()) {
			//remove a node n from S
			Node n = noIncomingSet.iterator().next();
			noIncomingSet.remove(n);

			//insert n into result
			result.add(n);

			//for each node m with an edge e from n to m do
			for (Iterator<Edge> it = n.outEdges.iterator(); it.hasNext(); ) {
				//remove edge e from the graph
				Edge e = it.next();
				Node m = e.to;
				it.remove();//Remove edge from n
				m.inEdges.remove(e);//Remove edge from m

				//if m has no other incoming edges then insert m into S
				if (m.inEdges.isEmpty()) {
					noIncomingSet.add(m);
				}
			}
		}
		//Check to see if all edges are removed
		boolean cycle = false;
		for (Node n : nodes) {
			if (!n.inEdges.isEmpty()) {
				cycle = true;
				break;
			}
		}
		if (cycle) {
			throw new IllegalStateException("Cycle present, topological sort not possible");
		}
		return result;
	}

	public static class Node {

		private final String name;

		private final Object value;

		private final HashSet<Edge> inEdges;

		private final HashSet<Edge> outEdges;

		public Node(String name, Object value) {
			this.name = name;
			this.value = value;
			inEdges = new HashSet<>();
			outEdges = new HashSet<>();
		}

		public Object getValue() {
			return value;
		}

		public Node addEdge(Node node) {
			Edge e = new Edge(this, node);
			outEdges.add(e);
			node.inEdges.add(e);
			return this;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static class Edge {

		private final Node from;

		private final Node to;

		public Edge(Node from, Node to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public boolean equals(Object obj) {
			Edge e = (Edge) obj;
			return e != null && e.from == from && e.to == to;
		}
	}
}