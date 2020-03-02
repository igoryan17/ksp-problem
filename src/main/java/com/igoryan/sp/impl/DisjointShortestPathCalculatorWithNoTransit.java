package com.igoryan.sp.impl;

import static com.igoryan.model.network.Node.COMPARE_BY_PREDECESSOR_AND_DISTANCE;

import com.google.common.graph.Network;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.edge.Edge;
import com.igoryan.model.network.edge.ParallelEdges;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import lombok.NonNull;

public class DisjointShortestPathCalculatorWithNoTransit
    extends BaseDijkstraShortestPathCalculator {

  @Override
  protected void relaxation(final Node u, final Node v, final ParallelEdges parallelEdges,
      final PriorityQueue<Node> queue) {
    final Edge firstUnused = parallelEdges.getFirstUnusedIfPossible();
    if (u.getDistance() == Long.MAX_VALUE) {
      return;
    }
    if (v.getEdgePredecessor() != null && !v.getEdgePredecessor().isUsed() && firstUnused
        .isUsed()) {
      // strong restriction not used preferable
      return;
    }
    final long fromUtoV = u.getDistance() + firstUnused.getCost();
    // not used firstly
    if (v.getEdgePredecessor() != null && v.getEdgePredecessor().isUsed() && !firstUnused.isUsed()
        || v.getDistance() > fromUtoV) {
      if (v.isTransit()) {
        queue.remove(v);
      }
      setPredecessor(u, v, firstUnused, fromUtoV);
      if (v.isTransit()) {
        queue.add(v);
      }
    }
  }

  @Override
  protected PriorityQueue<Node> initQueue(final Network<Node, ParallelEdges> network) {
    final PriorityQueue<Node> queue =
        new PriorityQueue<>(network.nodes().size(), COMPARE_BY_PREDECESSOR_AND_DISTANCE);
    queue.addAll(network.nodes().stream()
        .filter(Node::isTransit)
        .collect(Collectors.toList()));
    return queue;
  }

  @Override
  public void calculate(final @NonNull Node src, final @NonNull Node dst,
      final Network<Node, ParallelEdges> network) {
    final boolean srcIsTransit = src.isTransit();
    if (!srcIsTransit) {
      src.setTransit(true);
    }
    super.calculate(src, dst, network);
    src.setTransit(srcIsTransit);
  }
}
