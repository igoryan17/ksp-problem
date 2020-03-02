package com.igoryan.sp.impl;

import com.google.common.graph.Network;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.edge.Edge;
import com.igoryan.model.network.edge.ParallelEdges;
import com.igoryan.model.path.ShortestPath;
import com.igoryan.model.path.ShortestPathCreator;
import com.igoryan.sp.ShortestPathCalculator;
import java.util.PriorityQueue;
import lombok.NonNull;

public abstract class BaseDijkstraShortestPathCalculator implements ShortestPathCalculator {

  @Override
  public void calculate(final @NonNull Node src, final @NonNull Node dst,
      final Network<Node, ParallelEdges> network) {
    init(src, network);
    performDijkstra(network, initQueue(network));
  }

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

  protected abstract PriorityQueue<Node> initQueue(Network<Node, ParallelEdges> network);

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

  protected void setPredecessor(final Node u, final Node v, final Edge edge, final long fromUToV) {
    v.setDistance(fromUToV);
    v.setNodePredecessor(u);
    v.setEdgePredecessor(edge);
  }
}
