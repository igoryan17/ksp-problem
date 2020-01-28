package com.igoryan.ksp.impl;

import com.google.common.graph.MutableNetwork;
import com.google.inject.Inject;
import com.igoryan.model.Node;
import com.igoryan.model.ParallelEdges;
import com.igoryan.model.ShortestPath;
import com.igoryan.model.ShortestPathCreator;
import com.igoryan.model.ShortestPathsTree;
import com.igoryan.model.YenShortestPath;
import com.igoryan.sp.ShortestPathCalculator;
import com.igoryan.sp.util.ShortestPathsUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YenKShortestPathsCalculator extends BaseYenKShortestPathsCalculator {

  private static final ShortestPathCreator<YenShortestPath> SHORTEST_PATH_CREATOR =
      (src, dst, edges, nodes) -> new YenShortestPath(src, dst, edges, nodes, dst.getDistance());

  private final Map<Integer, ShortestPathsTree<YenShortestPath>> srcSwNumToCachedShortestPathTree =
      new HashMap<>();

  @Inject
  public YenKShortestPathsCalculator(
      final ShortestPathCalculator shortestPathCalculator) {
    super(shortestPathCalculator);
  }

  @Override
  public List<ShortestPath> calculate(final Node src, final Node dst,
      final MutableNetwork<Node, ParallelEdges> network, final int count) {
    final List<ShortestPath> result = new ArrayList<>();
    final ShortestPathsTree<YenShortestPath> pathsTree =
        srcSwNumToCachedShortestPathTree.computeIfAbsent(src.getSwNum(), key -> {
          shortestPathCalculator.calculate(src, dst, network);
          return ShortestPathsUtil
              .buildRecursively(YenShortestPath.class, src, SHORTEST_PATH_CREATOR, network.nodes());
        });
    result.add(pathsTree.getShortestPath(dst.getSwNum()));
    performYenAlgorithm(dst, network, count, result);
    return result;
  }
}
