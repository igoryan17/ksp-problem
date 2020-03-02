package com.igoryan.sp.impl;

import static com.igoryan.model.network.Node.COMPARE_NODES_BY_DISTANCE;

import com.google.common.graph.Network;
import com.google.inject.Singleton;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.edge.Edge;
import com.igoryan.model.network.edge.ParallelEdges;
import java.util.PriorityQueue;

@Singleton
public class DijkstraShortestPathCalculator extends BaseDijkstraShortestPathCalculator {

  @Override
  protected void relaxation(Node u, Node v, ParallelEdges parallelEdges,
      PriorityQueue<Node> queue) {
    final Edge edge = parallelEdges.getFirstUnusedOrNull();
    if (edge == null) {
      return;
    }
    final long fromUToV =
        u.getDistance() < Long.MAX_VALUE ? u.getDistance() + edge.getCost() : Long.MAX_VALUE;
    if (fromUToV < v.getDistance()) {
      queue.remove(v);
      setPredecessor(u, v, edge, fromUToV);
      queue.add(v);
    }
  }

  @Override
  protected PriorityQueue<Node> initQueue(final Network<Node, ParallelEdges> network) {
    final PriorityQueue<Node> queue =
        new PriorityQueue<>(network.nodes().size(), COMPARE_NODES_BY_DISTANCE);
    queue.addAll(network.nodes());
    return queue;
  }
}
