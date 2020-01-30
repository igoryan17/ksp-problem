package com.igoryan.sp.impl;

import com.google.common.graph.Network;
import com.igoryan.model.network.Edge;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.ParallelEdges;
import java.util.Objects;
import java.util.PriorityQueue;

public class DijkstraShortestPathCalculator extends BaseDijkstraShortestPathCalculator {

  @Override
  public void calculate(final Node src, final Node dst,
      final Network<Node, ParallelEdges> network) {
    init(src, network);
    final PriorityQueue<Node> queue =
        new PriorityQueue<>(network.nodes().size(), COMPARE_NODES_BY_DISTANCE);
    queue.addAll(network.nodes());
    performDijkstra(network, queue);
  }

  @Override
  protected void relaxation(Node u, Node v, ParallelEdges parallelEdges,
      PriorityQueue<Node> queue) {
    final Edge edge = Objects.requireNonNull(parallelEdges.peek());
    final long fromUToV =
        u.getDistance() < Long.MAX_VALUE ? u.getDistance() + edge.getCost() : Long.MAX_VALUE;
    if (fromUToV < v.getDistance()) {
      queue.remove(v);
      v.setDistance(fromUToV);
      v.setNodePredecessor(u);
      v.setEdgePredecessor(edge);
      queue.add(v);
    }
  }
}
