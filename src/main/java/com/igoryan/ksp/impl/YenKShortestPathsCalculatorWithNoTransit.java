package com.igoryan.ksp.impl;

import static com.igoryan.sp.util.ShortestPathsUtil.addNodeToTransitSubGraph;
import static com.igoryan.sp.util.ShortestPathsUtil.getTransitNodes;
import static com.igoryan.sp.util.ShortestPathsUtil.removeNodeFromTransitSubGraph;
import static java.util.Collections.emptyList;

import com.google.common.graph.Graphs;
import com.google.common.graph.MutableNetwork;
import com.google.inject.Inject;
import com.igoryan.model.Node;
import com.igoryan.model.ParallelEdges;
import com.igoryan.model.YenShortestPath;
import com.igoryan.sp.ShortestPathCalculator;
import java.util.ArrayList;
import java.util.List;

public class YenKShortestPathsCalculatorWithNoTransit extends BaseYenKShortestPathsCalculator {

  private MutableNetwork<Node, ParallelEdges> subNetworkWithTransits;

  @Inject
  protected YenKShortestPathsCalculatorWithNoTransit(
      final ShortestPathCalculator shortestPathCalculator) {
    super(shortestPathCalculator);
  }

  @Override
  public List<YenShortestPath> calculate(final Node src, final Node dst,
      final MutableNetwork<Node, ParallelEdges> network, final int count) {
    final YenShortestPath firstShortestPath = getFirstShortestPath(src, dst, network);
    if (firstShortestPath == null) {
      return emptyList();
    }
    final List<YenShortestPath> result = new ArrayList<>();
    result.add(firstShortestPath);
    if (subNetworkWithTransits == null) {
      subNetworkWithTransits = Graphs.inducedSubgraph(network, getTransitNodes(network));
    }
    addNodeToTransitSubGraph(src, subNetworkWithTransits, network);
    addNodeToTransitSubGraph(dst, subNetworkWithTransits, network);
    performYenAlgorithm(dst, network, count, result);
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
