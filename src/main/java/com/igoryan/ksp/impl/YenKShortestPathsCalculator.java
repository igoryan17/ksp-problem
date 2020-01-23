package com.igoryan.ksp.impl;

import com.google.common.graph.MutableNetwork;
import com.google.inject.Inject;
import com.igoryan.model.Node;
import com.igoryan.model.ParallelEdges;
import com.igoryan.model.ShortestPath;
import com.igoryan.sp.ShortestPathCalculator;
import java.util.ArrayList;
import java.util.List;

public class YenKShortestPathsCalculator extends BaseYenKShortestPathsCalculator {

  @Inject
  public YenKShortestPathsCalculator(
      final ShortestPathCalculator shortestPathCalculator) {
    super(shortestPathCalculator);
  }

  @Override
  public List<ShortestPath> calculate(final Node src, final Node dst,
      final MutableNetwork<Node, ParallelEdges> network, final int count) {
    final List<ShortestPath> result = new ArrayList<>();
    final ShortestPath firstShortestPath =
        shortestPathCalculator.calculateShortestPath(src, dst, network, true);

    result.add(firstShortestPath);
    performYenAlgorithm(dst, network, count, result);
    return result;
  }
}
