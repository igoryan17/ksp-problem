package com.igoryan.ksp.impl;

import static com.igoryan.util.ShortestPathsUtil.addInEdges;
import static com.igoryan.util.ShortestPathsUtil.removeInEdges;
import static com.igoryan.util.ShortestPathsUtil.subNetworkExpectInEdgesOfNoTransit;

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

  private MutableNetwork<Node, ParallelEdges> subNetworkWithoutInEdgesToNoTransit;

  @Inject
  public MpsKShortestPathCalculatorWithNoTransit(
      final ShortestPathCalculator shortestPathCalculator) {
    super(shortestPathCalculator);
  }

  @Override
  public List<MpsShortestPath> calculate(final @NonNull Node src, final @NonNull Node dst,
      final @NonNull MutableNetwork<Node, ParallelEdges> network, final int count) {
    if (subNetworkWithoutInEdgesToNoTransit == null) {
      subNetworkWithoutInEdgesToNoTransit = subNetworkExpectInEdgesOfNoTransit(network);
    }
    final ReversedShortestPathTree<MpsShortestPath> shortestPathTree =
        getOrCalculateShortestPathTree(src, dst, network);
    if (cachedEdgesStructure == null) {
      cachedEdgesStructure = buildEdgesStructure(subNetworkWithoutInEdgesToNoTransit);
    }
    if (lastDst != dst) {
      if (lastDst != null) {
        removeNodeInEdgesOfSubNetwork(lastDst);
        removeInEdges(lastDst, subNetworkWithoutInEdgesToNoTransit);
      }
      lastDst = dst;
      addInEdges(dst, subNetworkWithoutInEdgesToNoTransit, network);
      addInEdgesOfDstToTransitSubNetwork(dst);
    }
    return performMpsAlgorithm(src, dst, count, shortestPathTree);
  }

  private void addInEdgesOfDstToTransitSubNetwork(final @NonNull Node node) {
    if (node.isTransit()) {
      return;
    }
    for (final ParallelEdges parallelEdges : subNetworkWithoutInEdgesToNoTransit.inEdges(node)) {
      final EndpointPair<Node> endpointPair =
          subNetworkWithoutInEdgesToNoTransit.incidentNodes(parallelEdges);
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

  private void removeNodeInEdgesOfSubNetwork(final @NonNull Node node) {
    if (node.isTransit()) {
      return;
    }
    for (final ParallelEdges parallelEdges : subNetworkWithoutInEdgesToNoTransit.inEdges(node)) {
      final EndpointPair<Node> endpointPair =
          subNetworkWithoutInEdgesToNoTransit.incidentNodes(parallelEdges);
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
    subNetworkWithoutInEdgesToNoTransit = null;
  }
}
