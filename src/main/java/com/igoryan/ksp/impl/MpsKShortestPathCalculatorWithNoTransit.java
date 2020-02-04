package com.igoryan.ksp.impl;

import static com.igoryan.util.ShortestPathsUtil.addNode;
import static com.igoryan.util.ShortestPathsUtil.removeNode;
import static com.igoryan.util.ShortestPathsUtil.transitSubNetwork;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.ParallelEdges;
import com.igoryan.model.network.SortedParallelEdges;
import com.igoryan.model.path.MpsShortestPath;
import com.igoryan.model.tree.ReversedShortestPathTree;
import com.igoryan.sp.ShortestPathCalculator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;

@Singleton
public final class MpsKShortestPathCalculatorWithNoTransit extends BaseMpsKShortestPathCalculator {

  private MutableNetwork<Node, ParallelEdges> subNetworkWithTransits;

  @Inject
  public MpsKShortestPathCalculatorWithNoTransit(
      final ShortestPathCalculator shortestPathCalculator) {
    super(shortestPathCalculator);
  }

  @Override
  public List<MpsShortestPath> calculate(final @NonNull Node src, final @NonNull Node dst,
      final @NonNull MutableNetwork<Node, ParallelEdges> network, final int count) {
    if (subNetworkWithTransits == null) {
      subNetworkWithTransits = transitSubNetwork(network);
    }
    final ReversedShortestPathTree<MpsShortestPath> shortestPathTree =
        getOrCalculateShortestPathTree(src, dst, network);
    if (cachedEdgesStructure == null) {
      cachedEdgesStructure = buildEdgesStructure(subNetworkWithTransits);
    }
    if (lastDst != dst) {
      if (lastDst != null) {
        removeNodeEdgesOfTransitSubNetwork(lastDst);
        removeNode(lastDst, subNetworkWithTransits);
      }
      lastDst = dst;
      addNode(dst, subNetworkWithTransits, network);
      addNodeEdgesOfTransitSubNetwork(dst);
    }
    addNode(src, subNetworkWithTransits, network);
    addNodeEdgesOfTransitSubNetwork(src);
    final List<MpsShortestPath> result =
        performMpsAlgorithm(src, dst, count, subNetworkWithTransits, shortestPathTree);
    removeNodeEdgesOfTransitSubNetwork(src);
    removeNode(src, subNetworkWithTransits);
    return result;
  }

  private void addNodeEdgesOfTransitSubNetwork(final @NonNull Node node) {
    if (node.isTransit()) {
      return;
    }
    for (final ParallelEdges parallelEdges : subNetworkWithTransits.incidentEdges(node)) {
      final EndpointPair<Node> endpointPair = subNetworkWithTransits.incidentNodes(parallelEdges);
      final SortedParallelEdges sortedParallelEdges =
          cachedEdgesStructure.get(endpointPair.source().getSwNum());
      if (sortedParallelEdges == null) {
        final Integer swNumOfSrc = endpointPair.source().getSwNum();
        final Map<Integer, Node> swNumToNode = new HashMap<>();
        swNumToNode.put(endpointPair.target().getSwNum(), endpointPair.target());
        cachedEdgesStructure
            .put(swNumOfSrc, new SortedParallelEdges(swNumOfSrc, swNumToNode, parallelEdges));
        continue;
      }
      sortedParallelEdges.addAll(parallelEdges, endpointPair.target());
    }
  }

  private void removeNodeEdgesOfTransitSubNetwork(final @NonNull Node node) {
    if (node.isTransit()) {
      return;
    }
    for (final ParallelEdges parallelEdges : subNetworkWithTransits.incidentEdges(node)) {
      final EndpointPair<Node> endpointPair = subNetworkWithTransits.incidentNodes(parallelEdges);
      final Integer swNumOfSrc = endpointPair.source().getSwNum();
      final SortedParallelEdges sortedParallelEdges =
          Objects.requireNonNull(cachedEdgesStructure.get(swNumOfSrc));
      sortedParallelEdges.removeAll(parallelEdges, endpointPair.target());
      if (sortedParallelEdges.getSortedEdges().isEmpty()) {
        cachedEdgesStructure.remove(swNumOfSrc);
      }
    }
  }

  @Override
  public void clear() {
    super.clear();
    subNetworkWithTransits = null;
  }
}
