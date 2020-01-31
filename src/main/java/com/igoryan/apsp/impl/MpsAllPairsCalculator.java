package com.igoryan.apsp.impl;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Network;
import com.google.inject.Inject;
import com.igoryan.ksp.KShortestPathsCalculator;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.ParallelEdges;
import com.igoryan.model.path.MpsShortestPath;
import com.igoryan.sp.ShortestPathCalculator;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;

public final class MpsAllPairsCalculator extends BaseAllPairsCalculator {

  @Inject
  public MpsAllPairsCalculator(
      final @NonNull KShortestPathsCalculator<MpsShortestPath> kShortestPathsCalculator,
      final @NonNull ShortestPathCalculator shortestPathCalculator) {
    super(kShortestPathsCalculator, shortestPathCalculator);
  }

  @Override
  protected List<List<EndpointPair<Node>>> buildAllPairs(
      final Network<Node, ParallelEdges> network) {
    final List<List<EndpointPair<Node>>> result = new ArrayList<>(network.nodes().size());
    for (final Node dstNode : network.nodes()) {
      final List<EndpointPair<Node>> withCommonDst = new ArrayList<>(network.nodes().size() - 1);
      for (final Node srcNode : network.nodes()) {
        if (dstNode == srcNode) {
          continue;
        }
        withCommonDst.add(EndpointPair.ordered(srcNode, dstNode));
      }
      result.add(withCommonDst);
    }
    return result;
  }
}
