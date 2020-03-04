package com.igoryan.sp.impl;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Network;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.edge.ParallelEdges;
import com.igoryan.model.path.ShortestPath;
import com.igoryan.model.path.ShortestPathCreator;
import com.igoryan.sp.ShortestPathCalculator;
import java.util.Set;
import lombok.NonNull;

public abstract class BaseBellmanFordShortestPathCalculator implements ShortestPathCalculator {

  @Override
  public void calculate(final @NonNull Node src, final @NonNull Node dst,
      final @NonNull Network<Node, ParallelEdges> network) {
    performAlgorithm(src, network);
  }

  @Override
  public <T extends ShortestPath> T calculate(final Class<T> type, final @NonNull Node src,
      final @NonNull Node dst, final @NonNull Network<Node, ParallelEdges> network,
      final @NonNull ShortestPathCreator<T> shortestPathCreator) {
    performAlgorithm(src, network);
    return dst.buildShortestPath(type, shortestPathCreator);
  }

  protected void init(final Node src, Set<Node> nodes) {
    nodes.forEach(node -> {
      node.setDistance(Long.MAX_VALUE);
      node.setEdgePredecessor(null);
      node.setNodePredecessor(null);
    });
    src.setDistance(0L);
  }

  protected abstract void relaxation(final EndpointPair<Node> endpointPair,
      final ParallelEdges parallelEdges);

  protected void performAlgorithm(final Node src, final Network<Node, ParallelEdges> network) {
    init(src, network.nodes());
    for (int i = 0; i < network.nodes().size() - 1; i++) {
      network.edges().forEach(edge -> {
        final EndpointPair<Node> endpointPair = network.incidentNodes(edge);
      });
    }
  }
}
