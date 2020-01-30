package com.igoryan.sp.impl;

import static com.igoryan.util.ShortestPathsUtil.getTransitNodes;

import com.google.common.graph.Network;
import com.igoryan.model.network.Edge;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.ParallelEdges;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;

public class DijkstraShortestPathCalculatorWithNoTransit
    extends BaseDijkstraShortestPathCalculator {

  private Set<Node> transitNodes;

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
    performDijkstra(network, queue);
    src.setTransit(srcIsTransit);
  }
}
