package com.igoryan.apsp.impl;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Network;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.igoryan.ksp.KShortestPathsCalculator;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.ParallelEdges;
import com.igoryan.model.path.YenShortestPath;
import com.igoryan.sp.ShortestPathCalculator;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;

@Singleton
public final class YenAllPairsCalculator extends BaseAllPairsCalculator {

  @Inject
  public YenAllPairsCalculator(
      final @NonNull KShortestPathsCalculator<YenShortestPath> kShortestPathsCalculator,
      final @NonNull ShortestPathCalculator shortestPathCalculator) {
    super(kShortestPathsCalculator, shortestPathCalculator);
  }

  @Override
  protected List<List<EndpointPair<Node>>> buildAllPairs(
      final Network<Node, ParallelEdges> network) {
    final List<List<EndpointPair<Node>>> result = new ArrayList<>(network.nodes().size());
    for (final Node srcNode : network.nodes()) {
      final List<EndpointPair<Node>> withCommonSrc = new ArrayList<>(network.nodes().size() - 1);
      for (final Node dstNode : network.nodes()) {
        if (srcNode == dstNode) {
          continue;
        }
        withCommonSrc.add(EndpointPair.ordered(srcNode, dstNode));
      }
      result.add(withCommonSrc);
    }
    return result;
  }
}
