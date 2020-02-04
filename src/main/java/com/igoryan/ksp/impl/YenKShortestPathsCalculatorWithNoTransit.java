package com.igoryan.ksp.impl;

import static com.igoryan.util.ShortestPathsUtil.addNode;
import static com.igoryan.util.ShortestPathsUtil.removeNode;
import static com.igoryan.util.ShortestPathsUtil.transitSubNetwork;
import static java.util.Collections.emptyList;

import com.google.common.graph.MutableNetwork;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.ParallelEdges;
import com.igoryan.model.path.YenShortestPath;
import com.igoryan.sp.ShortestPathCalculator;
import java.util.ArrayList;
import java.util.List;

@Singleton
public final class YenKShortestPathsCalculatorWithNoTransit
    extends BaseYenKShortestPathsCalculator {

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
      subNetworkWithTransits = transitSubNetwork(network);
    }
    addNode(src, subNetworkWithTransits, network);
    addNode(dst, subNetworkWithTransits, network);
    performYenAlgorithm(dst, subNetworkWithTransits, count, result);
    removeNode(src, subNetworkWithTransits);
    removeNode(dst, subNetworkWithTransits);
    return result;
  }

  @Override
  public void clear() {
    super.clear();
    subNetworkWithTransits = null;
  }
}
