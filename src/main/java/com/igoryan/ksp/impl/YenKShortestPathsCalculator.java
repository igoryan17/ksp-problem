package com.igoryan.ksp.impl;

import static java.util.Collections.emptyList;

import com.google.common.graph.MutableNetwork;
import com.google.inject.Inject;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.ParallelEdges;
import com.igoryan.model.path.YenShortestPath;
import com.igoryan.sp.ShortestPathCalculator;
import java.util.ArrayList;
import java.util.List;

public final class YenKShortestPathsCalculator extends BaseYenKShortestPathsCalculator {

  @Inject
  public YenKShortestPathsCalculator(
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
    performYenAlgorithm(dst, network, count, result);
    return result;
  }
}
