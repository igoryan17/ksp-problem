package com.igoryan.sp.impl;

import com.google.common.graph.Network;
import com.igoryan.model.Edge;
import com.igoryan.model.Node;
import com.igoryan.model.ParallelEdges;
import java.util.HashSet;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;

public class DijkstraShortestPathCalculator extends BaseDijkstraShortestPathCalculator {

  @Override
  public void calculate(final Node src, final Node dst,
      final Network<Node, ParallelEdges> network) {
    init(src, network);
    final PriorityQueue<Node> queue =
        new PriorityQueue<>(network.nodes().size(), COMPARE_NODES_BY_DISTANCE);
    queue.addAll(network.nodes());
    final Set<Node> visitedNodes = new HashSet<>();
    while (!queue.isEmpty()) {
      final Node u = queue.poll();
      visitedNodes.add(u);
      for (final ParallelEdges outEdge : network.outEdges(u)) {
        final Node v = network.incidentNodes(outEdge).target();
        if (visitedNodes.contains(v)) {
          continue;
        }
        relaxation(u, v, outEdge, queue);
      }
    }
  }

  @Override
  protected void relaxation(Node u, Node v, ParallelEdges parallelEdges,
      PriorityQueue<Node> queue) {
    final Edge edge = Objects.requireNonNull(parallelEdges.peek());
    final long fromUToV = u.getDistance() + edge.getCost();
    if (fromUToV < v.getDistance()) {
      queue.remove(v);
      v.setDistance(fromUToV);
      v.setNodePredecessor(u);
      v.setEdgePredecessor(edge);
      queue.add(v);
    }
  }
}
