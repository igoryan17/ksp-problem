package com.igoryan.ksp.impl;

import static com.igoryan.util.ShortestPathsUtil.addNodeToTransitSubGraph;
import static com.igoryan.util.ShortestPathsUtil.getTransitNodes;
import static com.igoryan.util.ShortestPathsUtil.removeNodeFromTransitSubGraph;

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

public final class MpsShortestPathCalculatorWithNoTransit extends BaseMpsKShortestPathCalculator {

  private MutableNetwork<Node, ParallelEdges> subNetworkWithTransits;

  @Inject
  public MpsShortestPathCalculatorWithNoTransit(
      final ShortestPathCalculator shortestPathCalculator) {
    super(shortestPathCalculator);
  }

  @Override
  public List<MpsShortestPath> calculate(final @NonNull Node src, final @NonNull Node dst,
      final @NonNull MutableNetwork<Node, ParallelEdges> network, final int count) {
    final ReversedShortestPathTree<MpsShortestPath> shortestPathTree =
        getOrCalculateShortestPathTree(src, dst, Graphs.transpose(network));
    if (subNetworkWithTransits == null) {
      subNetworkWithTransits = Graphs.inducedSubgraph(network, getTransitNodes(network));
    }
    addNodeToTransitSubGraph(src, subNetworkWithTransits, network);
    addNodeToTransitSubGraph(dst, subNetworkWithTransits, network);
    final List<MpsShortestPath> result =
        performMpsAlgorithm(src, dst, count, subNetworkWithTransits, shortestPathTree);
    removeNodeFromTransitSubGraph(src, subNetworkWithTransits);
    removeNodeFromTransitSubGraph(dst, subNetworkWithTransits);
    return result;
  }

  @Override
  public void clear() {
    super.clear();
    subNetworkWithTransits = null;
  }
}
