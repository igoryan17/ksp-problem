package com.igoryan.sp.impl;

import com.google.common.graph.EndpointPair;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.edge.Edge;
import com.igoryan.model.network.edge.ParallelEdges;

public class DisjointShortestPathCalculatorWithNoTransit
    extends BaseBellmanFordShortestPathCalculator {

  @Override
  protected void relaxation(final EndpointPair<Node> endpointPair,
      final ParallelEdges parallelEdges) {
    final Edge edge = parallelEdges.getFirstUnusedIfPossible();
    final Node source = endpointPair.source();
    final Node target = endpointPair.target();
    if (!source.isTransit()) {
      return;
    }
    if (source.getDistance() == Long.MAX_VALUE) {
      return;
    }
    final long fromUToV = source.getDistance() + edge.getCost();
    final boolean sourceWithEdgeHasUnused = source.hasUnusedPredecessors() || !edge.isUsed();
    if (target.hasUnusedPredecessors() && !(sourceWithEdgeHasUnused && fromUToV < target
        .getDistance())) {
      return;
    }
    if (sourceWithEdgeHasUnused || fromUToV < target.getDistance()) {
      target.setEdgePredecessor(edge);
      target.setNodePredecessor(source);
      target.setDistance(fromUToV);
    }
  }
}
