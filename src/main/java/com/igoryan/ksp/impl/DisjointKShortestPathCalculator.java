package com.igoryan.ksp.impl;

import static java.util.Collections.emptyList;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.inject.Inject;
import com.igoryan.model.network.Edge;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.ParallelEdges;
import com.igoryan.model.path.YenShortestPath;
import com.igoryan.sp.ShortestPathCalculator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DisjointKShortestPathCalculator extends BaseYenKShortestPathsCalculator {

  @Inject
  protected DisjointKShortestPathCalculator(
      final ShortestPathCalculator shortestPathCalculator) {
    super(shortestPathCalculator);
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
          .calculate(YenShortestPath.class, src, dst, network, SHORTEST_PATH_CREATOR);
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
    clearMarkedEdges(network);
    if (result.size() == count) {
      return result;
    }
    performYenAlgorithm(dst, network, count - result.size() + 1, result);
    return result;
  }

  protected void markPathAsUsed(final Network<Node, ParallelEdges> network,
      final YenShortestPath shortestPath) {
    if (shortestPath != null) {
      for (int i = 0; i < shortestPath.getEdges().size(); i++) {
        final Edge edge = shortestPath.getEdges().get(i);
        final ParallelEdges parallelEdges =
            Objects.requireNonNull(network.edgeConnectingOrNull(shortestPath.getNodes().get(i),
                shortestPath.getNodes().get(i + 1)));
        parallelEdges.remove(edge);
        edge.setUsed(true);
        parallelEdges.add(edge);
      }
    }
  }

  protected void clearMarkedEdges(final Network<Node, ParallelEdges> network) {
    for (ParallelEdges edges : network.edges()) {
      if (edges.stream().noneMatch(Edge::isUsed)) {
        continue;
      }
      final List<Edge> usedEdges = edges.stream()
          .filter(Edge::isUsed)
          .collect(Collectors.toList());
      usedEdges.forEach(edge -> {
        edges.remove(edge);
        edge.setUsed(false);
        edges.add(edge);
      });
    }
  }
}
