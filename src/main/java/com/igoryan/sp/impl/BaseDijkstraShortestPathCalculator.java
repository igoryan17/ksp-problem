package com.igoryan.sp.impl;

import com.google.common.graph.Network;
import com.igoryan.model.Node;
import com.igoryan.model.ParallelEdges;
import com.igoryan.model.ShortestPath;
import com.igoryan.model.ShortestPathCreator;
import com.igoryan.sp.ShortestPathCalculator;
import java.util.Comparator;
import java.util.PriorityQueue;

public abstract class BaseDijkstraShortestPathCalculator implements ShortestPathCalculator {

  protected static final Comparator<Node> COMPARE_NODES_BY_DISTANCE =
      Comparator.comparingLong(Node::getDistance);

  protected void init(Node src, final Network<Node, ParallelEdges> network) {
    network.nodes().forEach(node -> {
      node.setEdgePredecessor(null);
      node.setNodePredecessor(null);
      node.setDistance(Long.MAX_VALUE);
    });
    src.setDistance(0);
  }

  protected abstract void relaxation(Node u, Node v, ParallelEdges parallelEdges,
      PriorityQueue<Node> queue);

  @Override
  public <T extends ShortestPath> T calculate(final Class<T> type, final Node src, final Node dst,
      final Network<Node, ParallelEdges> network,
      final ShortestPathCreator<T> shortestPathCreator) {
    calculate(src, dst, network);
    return dst.buildShortestPath(type, shortestPathCreator);
  }
}
