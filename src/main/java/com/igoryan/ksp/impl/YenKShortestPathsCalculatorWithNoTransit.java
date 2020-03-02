package com.igoryan.ksp.impl;

import static com.igoryan.util.ShortestPathsUtil.addNode;
import static com.igoryan.util.ShortestPathsUtil.removeNode;
import static com.igoryan.util.ShortestPathsUtil.transitSubNetwork;
import static java.util.Collections.emptyList;

import com.google.common.graph.MutableNetwork;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.edge.ParallelEdges;
import com.igoryan.model.path.YenShortestPath;
import com.igoryan.sp.ShortestPathCalculator;
import java.util.ArrayList;
import java.util.List;

@Singleton
public final class YenKShortestPathsCalculatorWithNoTransit
    extends BaseYenKShortestPathsCalculator {

  private Node lastSrc;
  private MutableNetwork<Node, ParallelEdges> subNetworkWithTransits;

  @Inject
  protected YenKShortestPathsCalculatorWithNoTransit(
      final @Named("noTransit") ShortestPathCalculator shortestPathCalculator) {
    super(shortestPathCalculator, shortestPathCalculator);
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
    if (src != lastSrc) {
      if (lastSrc != null) {
        removeNode(lastSrc, subNetworkWithTransits);
      }
      addNode(src, subNetworkWithTransits, network);
      lastSrc = src;
    }
    addNode(dst, subNetworkWithTransits, network);
    performYenAlgorithm(dst, subNetworkWithTransits, count, result);
    removeNode(dst, subNetworkWithTransits);
    return result;
  }

  @Override
  public void clear() {
    super.clear();
    subNetworkWithTransits = null;
    this.lastSrc = null;
  }
}
