package com.igoryan.sp.impl;

import com.google.common.graph.Network;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.ParallelEdges;
import com.igoryan.model.path.ShortestPath;
import com.igoryan.model.path.ShortestPathCreator;
import com.igoryan.sp.ShortestPathCalculator;
import java.util.Comparator;
import java.util.PriorityQueue;

public abstract class BaseDijkstraShortestPathCalculator implements ShortestPathCalculator {

  protected static final Comparator<Node> COMPARE_NODES_BY_DISTANCE =
      Comparator.comparingLong(Node::getDistance);

  @Override
  public <T extends ShortestPath> T calculate(final Class<T> type, final Node src, final Node dst,
      final Network<Node, ParallelEdges> network,
      final ShortestPathCreator<T> shortestPathCreator) {
    calculate(src, dst, network);
    return dst.buildShortestPath(type, shortestPathCreator);
  }

  protected void init(Node src, final Network<Node, ParallelEdges> network) {
    network.nodes().forEach(node -> {
      node.setVisited(false);
      node.setEdgePredecessor(null);
      node.setNodePredecessor(null);
      node.setDistance(Long.MAX_VALUE);
    });
    src.setDistance(0);
  }

  protected abstract void relaxation(Node u, Node v, ParallelEdges parallelEdges,
      PriorityQueue<Node> queue);

  protected void performDijkstra(final Network<Node, ParallelEdges> network,
      final PriorityQueue<Node> queue) {
    while (!queue.isEmpty()) {
      final Node u = queue.poll();
      u.setVisited(true);
      for (final ParallelEdges outEdge : network.outEdges(u)) {
        final Node v = network.incidentNodes(outEdge).target();
        if (v.isVisited()) {
          continue;
        }
        relaxation(u, v, outEdge, queue);
      }
    }
  }
}
