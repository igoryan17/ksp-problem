package com.igoryan.sp.impl;

import static com.igoryan.model.network.Node.COMPARE_NODES_BY_DISTANCE;

import com.google.common.graph.Network;
import com.google.inject.Singleton;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.edge.Edge;
import com.igoryan.model.network.edge.ParallelEdges;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

@Singleton
public class DijkstraShortestPathCalculatorWithNoTransit
    extends BaseDijkstraShortestPathCalculator {

  @Override
  protected void relaxation(final Node u, final Node v, final ParallelEdges parallelEdges,
      final PriorityQueue<Node> queue) {
    final Edge edge = parallelEdges.getFirstUnusedOrNull();
    if (edge == null) {
      return;
    }
    final long fromUToV =
        u.getDistance() < Long.MAX_VALUE ? u.getDistance() + edge.getCost() : Long.MAX_VALUE;
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
  protected PriorityQueue<Node> initQueue(final Network<Node, ParallelEdges> network) {
    final PriorityQueue<Node> queue =
        new PriorityQueue<>(network.nodes().size(), COMPARE_NODES_BY_DISTANCE);
    queue.addAll(network.nodes().stream()
        .filter(Node::isTransit)
        .collect(Collectors.toList()));
    return queue;
  }

  @Override
  public void calculate(final Node src, final Node dst,
      final Network<Node, ParallelEdges> network) {
    final boolean srcIsTransit = src.isTransit();
    if (!srcIsTransit) {
      src.setTransit(true);
    }
    super.calculate(src, dst, network);
    src.setTransit(srcIsTransit);
  }
}
