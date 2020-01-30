package com.igoryan.ksp.impl;

import com.google.common.graph.Graphs;
import com.google.common.graph.MutableNetwork;
import com.google.inject.Inject;
import com.igoryan.model.path.MpsShortestPath;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.ParallelEdges;
import com.igoryan.model.tree.ReversedShortestPathTree;
import com.igoryan.sp.ShortestPathCalculator;
import java.util.List;
import lombok.NonNull;

public final class MpsKShortestPathsCalculator extends BaseMpsKShortestPathCalculator {

  @Inject
  public MpsKShortestPathsCalculator(
      final ShortestPathCalculator shortestPathCalculator) {
    super(shortestPathCalculator);
  }

  @Override
  public List<MpsShortestPath> calculate(final @NonNull Node src, final @NonNull Node dst,
      final @NonNull MutableNetwork<Node, ParallelEdges> network, final int count) {
    final ReversedShortestPathTree<MpsShortestPath> shortestPathTree =
        getOrCalculateShortestPathTree(src, dst, Graphs.transpose(network));
    return performMpsAlgorithm(src, dst, count, network, shortestPathTree);
  }
}
