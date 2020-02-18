package com.igoryan.ksp.impl;

import static com.igoryan.util.ShortestPathsUtil.addNode;
import static com.igoryan.util.ShortestPathsUtil.removeNode;
import static com.igoryan.util.ShortestPathsUtil.transitSubNetwork;

import com.google.common.graph.MutableNetwork;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.ParallelEdges;
import com.igoryan.model.path.YenShortestPath;
import com.igoryan.sp.ShortestPathCalculator;
import java.util.List;

@Singleton
public class DisjointKShortestPathCalculatorWithNoTransit
    extends DisjointKShortestPathCalculator {

  private Node lastSrc;
  private MutableNetwork<Node, ParallelEdges> subNetworkWithTransits;

  @Inject
  public DisjointKShortestPathCalculatorWithNoTransit(
      final ShortestPathCalculator shortestPathCalculator) {
    super(shortestPathCalculator);
  }

  @Override
  public List<YenShortestPath> calculate(final Node src, final Node dst,
      final MutableNetwork<Node, ParallelEdges> network, final int count) {
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
    final List<YenShortestPath> result =
        super.calculate(src, dst, subNetworkWithTransits, count);
    removeNode(dst, subNetworkWithTransits);
    clearMarkedEdges(network);
    return result;
  }

  @Override
  public void clear() {
    super.clear();
    subNetworkWithTransits = null;
    this.lastSrc = null;
  }
}
