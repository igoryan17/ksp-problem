package com.igoryan.ksp.impl;

import static java.util.Collections.emptyList;

import com.google.common.graph.EndpointPair;
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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

@Singleton
public final class NaiveYenKShortestPathCalculatorWithNoTransit
    extends BaseYenKShortestPathsCalculator {

  private Map<Node, List<EndPointPairParallelEdgesTuple>> noTransitNodeToOutEdges;

  @Inject
  public NaiveYenKShortestPathCalculatorWithNoTransit(
      final @Named("noTransit") ShortestPathCalculator shortestPathCalculator) {
    super(shortestPathCalculator);
  }

  @Override
  public List<YenShortestPath> calculate(final @NonNull Node src, final @NonNull Node dst,
      final MutableNetwork<Node, ParallelEdges> network, final int count) {
    prepareNetwork(network);
    addOutEdgesToNetwork(src, network);
    final YenShortestPath firstShortestPath = getFirstShortestPath(src, dst, network);
    if (firstShortestPath == null) {
      return emptyList();
    }
    final List<YenShortestPath> result = new ArrayList<>();
    result.add(firstShortestPath);
    performYenAlgorithm(dst, network, count, result);
    removeOutEdgesFromNetwork(src, network);
    return result;
  }

  private void addOutEdgesToNetwork(final Node node,
      final MutableNetwork<Node, ParallelEdges> network) {
    if (node.isTransit()) {
      return;
    }
    noTransitNodeToOutEdges.getOrDefault(node, emptyList())
        .forEach(endPointPairParallelEdgesTuple -> network
            .addEdge(endPointPairParallelEdgesTuple.endpointPair,
                endPointPairParallelEdgesTuple.parallelEdges));
  }

  private void removeOutEdgesFromNetwork(final Node node,
      final MutableNetwork<Node, ParallelEdges> network) {
    if (node.isTransit()) {
      return;
    }
    network.outEdges(node).forEach(network::removeEdge);
  }

  private void prepareNetwork(final MutableNetwork<Node, ParallelEdges> network) {
    if (noTransitNodeToOutEdges != null) {
      return;
    }
    noTransitNodeToOutEdges = network.nodes().stream()
        .filter(node -> !node.isTransit())
        .collect(Collectors
            .toMap(Function.identity(), noTransitNode -> network.outEdges(noTransitNode).stream()
                .map(parallelEdges -> new EndPointPairParallelEdgesTuple(
                    network.incidentNodes(parallelEdges), parallelEdges))
                .collect(Collectors.toList())));
    noTransitNodeToOutEdges.values().stream()
        .flatMap(List::stream)
        .map(EndPointPairParallelEdgesTuple::getParallelEdges)
        .forEach(network::removeEdge);
  }

  @Override
  public void clear() {
    super.clear();
    noTransitNodeToOutEdges = null;
  }

  @Value
  @EqualsAndHashCode(of = "parallelEdges")
  private static class EndPointPairParallelEdgesTuple {

    final EndpointPair<Node> endpointPair;
    final ParallelEdges parallelEdges;
  }
}
