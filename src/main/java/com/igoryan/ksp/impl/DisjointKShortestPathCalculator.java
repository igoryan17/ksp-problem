package com.igoryan.ksp.impl;

import static com.igoryan.model.path.YenShortestPath.YEN_SHORTEST_PATH_CREATOR;
import static java.util.Collections.emptyList;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.inject.Inject;
import com.igoryan.ksp.KShortestPathsCalculator;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.edge.Edge;
import com.igoryan.model.network.edge.ParallelEdges;
import com.igoryan.model.path.YenShortestPath;
import com.igoryan.model.tree.ShortestPathsTree;
import com.igoryan.sp.ShortestPathCalculator;
import com.igoryan.util.ShortestPathsUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

public class DisjointKShortestPathCalculator implements KShortestPathsCalculator<YenShortestPath> {

  private final ShortestPathCalculator shortestPathCalculator;

  private final Map<Integer, ShortestPathsTree<YenShortestPath>> srcSwNumToCachedShortestPathTree =
      new HashMap<>();

  @Inject
  protected DisjointKShortestPathCalculator(
      final ShortestPathCalculator shortestPathCalculator) {
    this.shortestPathCalculator = shortestPathCalculator;
  }

  @Override
  public List<YenShortestPath> calculate(final Node src, final Node dst,
      final MutableNetwork<Node, ParallelEdges> network, final int count) {
    final List<YenShortestPath> result = new ArrayList<>(count);
    final YenShortestPath firstShortestPath = getFirstShortestPath(src, dst, network);
    if (firstShortestPath == null) {
      return emptyList();
    }
    result.add(firstShortestPath);
    markPathAsUsed(network, firstShortestPath);
    while (result.size() < count) {
      final YenShortestPath shortestPath = shortestPathCalculator
          .calculate(YenShortestPath.class, src, dst, network, YEN_SHORTEST_PATH_CREATOR);
      if (shortestPath == null) {
        break;
      }
      if (result.stream().anyMatch(
          path -> path.hashCode() == shortestPath.hashCode() && path.equals(shortestPath))) {
        break;
      }
      markPathAsUsed(network, shortestPath);
      result.add(shortestPath);
    }
    clearMarkedEdges(network, result);
    return result;
  }

  protected void markPathAsUsed(final Network<Node, ParallelEdges> network,
      final YenShortestPath shortestPath) {
    for (int i = 0; i < shortestPath.getEdges().size(); i++) {
      final Edge edge = shortestPath.getEdges().get(i);
      final ParallelEdges parallelEdges =
          Objects.requireNonNull(network.edgeConnectingOrNull(shortestPath.getNodes().get(i),
              shortestPath.getNodes().get(i + 1)));
      parallelEdges.markAsUsed(edge);
    }
  }

  protected void clearMarkedEdges(final Network<Node, ParallelEdges> network,
      List<YenShortestPath> result) {
    for (final YenShortestPath shortestPath : result) {
      final List<Node> pathNodes = shortestPath.getNodes();
      for (int i = 0; i < pathNodes.size() - 1; i++) {
        final EndpointPair<Node> endpointPair =
            EndpointPair.ordered(pathNodes.get(i), pathNodes.get(i + 1));
        final ParallelEdges parallelEdges =
            Objects.requireNonNull(network.edgeConnectingOrNull(endpointPair));
        parallelEdges.clearMarkedEdges();
      }
    }
  }

  @Nullable
  protected YenShortestPath getFirstShortestPath(final Node src, final Node dst,
      final MutableNetwork<Node, ParallelEdges> network) {
    final ShortestPathsTree<YenShortestPath> pathsTree =
        srcSwNumToCachedShortestPathTree.computeIfAbsent(src.getSwNum(), key -> {
          shortestPathCalculator.calculate(src, dst, network);
          return ShortestPathsUtil
              .buildRecursively(YenShortestPath.class, src,
                  YenShortestPath.YEN_SHORTEST_PATH_CREATOR, network.nodes());
        });
    return pathsTree.getShortestPath(dst.getSwNum());
  }

  @Override
  public void clear() {
    srcSwNumToCachedShortestPathTree.clear();
  }
}
