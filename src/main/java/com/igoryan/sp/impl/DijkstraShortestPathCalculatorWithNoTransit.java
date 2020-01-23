package com.igoryan.sp.impl;

import static com.igoryan.sp.util.ShortestPathsUtil.getTransitNodes;

import com.google.common.graph.Network;
import com.igoryan.model.Edge;
import com.igoryan.model.Node;
import com.igoryan.model.ParallelEdges;
import java.util.HashSet;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;

public class DijkstraShortestPathCalculatorWithNoTransit
    extends BaseDijkstraShortestPathCalculator {

  private Set<Node> transitNodes;

  @Override
  protected void init(final Node src, final Network<Node, ParallelEdges> network) {
    network.nodes().forEach(node -> {
      node.setEdgePredecessor(null);
      node.setNodePredecessor(null);
      node.setDistance(Long.MAX_VALUE);
    });
    src.setDistance(0);
  }

  @Override
  protected void relaxation(final Node u, final Node v, final ParallelEdges parallelEdges,
      final PriorityQueue<Node> queue) {
    final Edge edge = Objects.requireNonNull(parallelEdges.peek());
    final long fromUToV = u.getDistance() + edge.getCost();
    if (fromUToV < v.getDistance()) {
      if (v.isTransit()) {
        queue.remove(v);
      }
      v.setDistance(fromUToV);
      v.setNodePredecessor(u);
      v.setEdgePredecessor(edge);
      if (v.isTransit()) {
        queue.add(v);
      }
    }
  }

  @Override
  public void calculate(final Node src, final Node dst,
      final Network<Node, ParallelEdges> network) {
    init(src, network);
    final PriorityQueue<Node> queue =
        new PriorityQueue<>(network.nodes().size(), COMPARE_NODES_BY_DISTANCE);
    if (transitNodes == null) {
      transitNodes = getTransitNodes(network);
    }
    queue.addAll(transitNodes);
    final boolean srcIsTransit = src.isTransit();
    if (!srcIsTransit) {
      src.setTransit(true);
      queue.add(src);
    }
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
    src.setTransit(srcIsTransit);
  }

  @Override
  public void clear() {
    super.clear();
    transitNodes = null;
  }
}
