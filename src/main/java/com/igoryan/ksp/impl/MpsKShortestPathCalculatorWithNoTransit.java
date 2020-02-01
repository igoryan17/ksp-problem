package com.igoryan.ksp.impl;

import static com.igoryan.util.ShortestPathsUtil.addNodeToTransitSubGraph;
import static com.igoryan.util.ShortestPathsUtil.removeNodeFromTransitSubGraph;
import static com.igoryan.util.ShortestPathsUtil.subNetworkExpectOutEdgesOfNoTransit;
import static java.util.Collections.singletonMap;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableNetwork;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.ParallelEdges;
import com.igoryan.model.network.SortedParallelEdges;
import com.igoryan.model.path.MpsShortestPath;
import com.igoryan.model.tree.ReversedShortestPathTree;
import com.igoryan.sp.ShortestPathCalculator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;

@Singleton
public final class MpsKShortestPathCalculatorWithNoTransit extends BaseMpsKShortestPathCalculator {

  private MutableNetwork<Node, ParallelEdges> subNetworkExpectOutEdgesOfNoTransit;

  @Inject
  public MpsKShortestPathCalculatorWithNoTransit(
      final ShortestPathCalculator shortestPathCalculator) {
    super(shortestPathCalculator);
  }

  @Override
  public List<MpsShortestPath> calculate(final @NonNull Node src, final @NonNull Node dst,
      final @NonNull MutableNetwork<Node, ParallelEdges> network, final int count) {
    if (subNetworkExpectOutEdgesOfNoTransit == null) {
      subNetworkExpectOutEdgesOfNoTransit = subNetworkExpectOutEdgesOfNoTransit(network);
    }
    final ReversedShortestPathTree<MpsShortestPath> shortestPathTree =
        getOrCalculateShortestPathTree(src, dst, Graphs.transpose(network));
    final boolean dstChanged = lastDst != dst;
    if (dstChanged) {
      if (lastDst != null) {
        removeInEdgesFromStructure(lastDst);
        removeNodeFromTransitSubGraph(lastDst, subNetworkExpectOutEdgesOfNoTransit);
      }
      lastDst = dst;
      cachedEdgesStructure = buildEdgesStructure(network, shortestPathTree);
      addNodeToTransitSubGraph(dst, subNetworkExpectOutEdgesOfNoTransit, network);
      addInEdgesToStructure(dst);
    }
    final List<MpsShortestPath> result =
        performMpsAlgorithm(src, dst, count, subNetworkExpectOutEdgesOfNoTransit, shortestPathTree);
    return result;
  }

  private void addInEdgesToStructure(final @NonNull Node dst) {
    for (final ParallelEdges parallelEdges : subNetworkExpectOutEdgesOfNoTransit.inEdges(dst)) {
      final EndpointPair<Node> endpointPair =
          subNetworkExpectOutEdgesOfNoTransit.incidentNodes(parallelEdges);
      final SortedParallelEdges sortedParallelEdges =
          cachedEdgesStructure.get(endpointPair.nodeU());
      if (sortedParallelEdges == null) {
        final Integer swNumOfSrc = endpointPair.source().getSwNum();
        final Map<Integer, Node> swNumToNode = singletonMap(dst.getSwNum(), dst);
        cachedEdgesStructure
            .put(swNumOfSrc,
                new SortedParallelEdges(swNumOfSrc, swNumToNode, new ArrayList<>(parallelEdges)));
        continue;
      }
      sortedParallelEdges.addAll(parallelEdges, dst);
    }
  }

  private void removeInEdgesFromStructure(final @NonNull Node dst) {
    for (final ParallelEdges parallelEdges : subNetworkExpectOutEdgesOfNoTransit
        .inEdges(dst)) {
      final EndpointPair<Node> endpointPair =
          subNetworkExpectOutEdgesOfNoTransit.incidentNodes(parallelEdges);
      final Integer swNumOfSrc = endpointPair.source().getSwNum();
      final SortedParallelEdges sortedParallelEdges =
          Objects.requireNonNull(cachedEdgesStructure.get(swNumOfSrc));
      sortedParallelEdges.removeAll(parallelEdges, dst);
      if (sortedParallelEdges.getSortedEdges().isEmpty()) {
        cachedEdgesStructure.remove(swNumOfSrc);
      }
    }
  }

  @Override
  public void clear() {
    super.clear();
    subNetworkExpectOutEdgesOfNoTransit = null;
  }
}
