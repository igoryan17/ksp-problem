package com.igoryan.apsp.impl;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.igoryan.apsp.AllPairsCalculator;
import com.igoryan.ksp.KShortestPathsCalculator;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.edge.ParallelEdges;
import com.igoryan.model.path.ShortestPath;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class BaseAllPairsCalculator implements AllPairsCalculator {

  private final KShortestPathsCalculator<? extends ShortestPath> kShortestPathsCalculator;

  protected BaseAllPairsCalculator(
      final KShortestPathsCalculator<? extends ShortestPath> kShortestPathsCalculator) {
    this.kShortestPathsCalculator = kShortestPathsCalculator;
  }

  @Override
  public Map<EndpointPair<Node>, List<? extends ShortestPath>> calculate(
      final MutableNetwork<Node, ParallelEdges> network, final int PerPairCount) {
    final Map<EndpointPair<Node>, List<? extends ShortestPath>> result = new HashMap<>();

    buildAllPairs(network).forEach(endpointPairs -> endpointPairs.forEach(endPointPair -> {
      result.put(endPointPair, kShortestPathsCalculator
          .calculate(endPointPair.source(), endPointPair.target(), network, PerPairCount));
    }));
    kShortestPathsCalculator.clear();
    return result;
  }

  // it's useful for parallel implementation
  protected abstract List<List<EndpointPair<Node>>> buildAllPairs(
      Network<Node, ParallelEdges> network);
}
