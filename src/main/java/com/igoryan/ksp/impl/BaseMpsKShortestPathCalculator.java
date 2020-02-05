package com.igoryan.ksp.impl;

import static java.util.Collections.emptyList;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graphs;
import com.google.common.graph.Network;
import com.igoryan.ksp.KShortestPathsCalculator;
import com.igoryan.model.network.Edge;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.NodeEdgeTuple;
import com.igoryan.model.network.ParallelEdges;
import com.igoryan.model.network.SortedParallelEdges;
import com.igoryan.model.path.MpsShortestPath;
import com.igoryan.model.path.ShortestPathCreator;
import com.igoryan.model.tree.KShortestPathsTree;
import com.igoryan.model.tree.ReversedShortestPathTree;
import com.igoryan.sp.ShortestPathCalculator;
import com.igoryan.util.ShortestPathsUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class BaseMpsKShortestPathCalculator implements KShortestPathsCalculator<MpsShortestPath> {

  protected static final Comparator<MpsShortestPath> COMPARE_SHORTEST_PATHS_BY_COST =
      Comparator.comparingLong(MpsShortestPath::getOriginalCost);
  protected static final ShortestPathCreator<MpsShortestPath>
      MPS_SHORTEST_PATH_SHORTEST_PATH_CREATOR = MpsShortestPath::new;
  protected final Map<Integer, ReversedShortestPathTree<MpsShortestPath>>
      dstSwNumToCachedShortestPathTree = new HashMap<>();
  protected final ShortestPathCalculator shortestPathCalculator;
  protected Node lastDst;
  protected Map<Integer, SortedParallelEdges> cachedEdgesStructure;
  protected Network<Node, ParallelEdges> cachedTransposedNetwork;

  protected BaseMpsKShortestPathCalculator(
      final ShortestPathCalculator shortestPathCalculator) {
    this.shortestPathCalculator = shortestPathCalculator;
  }

  protected ReversedShortestPathTree<MpsShortestPath> getOrCalculateShortestPathTree(final Node src,
      final Node dst, final Network<Node, ParallelEdges> network) {
    return dstSwNumToCachedShortestPathTree.computeIfAbsent(dst.getSwNum(), dstSwNum -> {
      if (cachedTransposedNetwork == null) {
        cachedTransposedNetwork = Graphs.transpose(network);
      }
      shortestPathCalculator.calculate(dst, src, cachedTransposedNetwork);
      calcReducedCost(network);
      return ShortestPathsUtil.buildRevertedRecursively(MpsShortestPath.class, dst,
          MPS_SHORTEST_PATH_SHORTEST_PATH_CREATOR, cachedTransposedNetwork.nodes());
    });
  }

  protected List<MpsShortestPath> performMpsAlgorithm(final Node src, final Node dst,
      final int count, final ReversedShortestPathTree<MpsShortestPath> shortestPathTree) {
    final Queue<MpsShortestPath> candidates = new PriorityQueue<>(COMPARE_SHORTEST_PATHS_BY_COST);
    final MpsShortestPath firstShortestPath = shortestPathTree.getShortestPath(src.getSwNum());
    if (firstShortestPath == null) {
      return emptyList();
    }
    candidates.add(firstShortestPath);
    int currentCount = 0;
    final List<MpsShortestPath> result = new ArrayList<>();
    final KShortestPathsTree kShortestPathsTree = new KShortestPathsTree();
    while (!candidates.isEmpty() && currentCount <= count) {
      final MpsShortestPath shortestPath = candidates.poll();
      final NodeEdgeTuple nodeEdgeTuple = kShortestPathsTree.getDeviationKey(shortestPath);
      if (shortestPath.withoutLoops()) {
        currentCount++;
        result.add(shortestPath);
        kShortestPathsTree.add(shortestPath, count - 1);
      }
      if (nodeEdgeTuple == null) {
        continue;
      }
      Node deviationNode = nodeEdgeTuple.getNode();
      MpsShortestPath toDeviationNode;
      int indexOfTarget = shortestPath.getIndex(shortestPath.getDst());
      int nodeIndex = shortestPath.getIndex(deviationNode);
      do {
        deviationNode = shortestPath.getNodes().get(nodeIndex);
        final SortedParallelEdges sortedParallelEdges =
            cachedEdgesStructure.get(deviationNode.getSwNum());
        toDeviationNode = shortestPath.subPath(nodeIndex);
        if (sortedParallelEdges == null) {
          nodeIndex++;
          continue;
        }
        final TreeSet<Edge> parallelEdges = sortedParallelEdges.getSortedEdges();
        Edge deviationEdge = null;
        for (Edge edge : parallelEdges.tailSet(shortestPath.getEdges().get(nodeIndex), false)) {
          if (toDeviationNode.formsCycle(edge)) {
            continue;
          }
          deviationEdge = edge;
          break;
        }
        if (deviationEdge == null) {
          nodeIndex++;
          continue;
        }
        final Node targetNodeOfDeviationEdge = sortedParallelEdges.getTargetNode(deviationEdge);
        final MpsShortestPath.Builder builder = MpsShortestPath.builder()
            .from(toDeviationNode);
        if (targetNodeOfDeviationEdge == dst) {
          candidates.add(builder.append(deviationEdge, dst).build());
          nodeIndex++;
          continue;
        }
        final List<Edge> fromHeadOfDeviationEdgeToDst =
            shortestPathTree.getPath(deviationEdge.getDstSwNum());
        if (fromHeadOfDeviationEdgeToDst.isEmpty()) {
          nodeIndex++;
          continue;
        }
        candidates.add(builder
            .append(deviationEdge, targetNodeOfDeviationEdge)
            .append(fromHeadOfDeviationEdgeToDst, shortestPathTree.getSwNumToOriginalNode())
            .build()
        );
        nodeIndex++;
      } while (Objects.requireNonNull(toDeviationNode).withoutLoops()
          && nodeIndex < indexOfTarget);
    }
    return result;
  }

  protected void calcReducedCost(final Network<Node, ParallelEdges> network) {
    for (final ParallelEdges parallelEdges : network.edges()) {
      final EndpointPair<Node> endpointPair = network.incidentNodes(parallelEdges);
      final long costFromHead = endpointPair.target().getDistance();
      final long costFromTail = endpointPair.source().getDistance();
      if (Long.MAX_VALUE == costFromHead || Long.MAX_VALUE == costFromTail) {
        parallelEdges.forEach(edge -> edge.setReducedCost(Long.MAX_VALUE));
        continue;
      }
      parallelEdges
          .forEach(edge -> edge.setReducedCost(costFromHead - costFromTail + edge.getCost()));
    }
  }

  protected Map<Integer, SortedParallelEdges> buildEdgesStructure(
      final Network<Node, ParallelEdges> network) {
    final Map<Integer, SortedParallelEdges> edgesStructure = new HashMap<>(network.nodes().size());
    for (Node node : network.nodes()) {
      final Set<ParallelEdges> outEdges = network.outEdges(node);
      if (outEdges.isEmpty()) {
        continue;
      }
      final List<Edge> notSortedEdges = outEdges.stream()
          .flatMap(Collection::stream)
          .filter(edge -> edge.getReducedCost() < Long.MAX_VALUE)
          .collect(Collectors.toList());
      if (notSortedEdges.isEmpty()) {
        continue;
      }
      final Map<Integer, Node> swNumToNode = outEdges.stream()
          .map(network::incidentNodes)
          .map(EndpointPair::target)
          .collect(Collectors.toMap(Node::getSwNum, Function.identity()));
      edgesStructure.put(node.getSwNum(),
          new SortedParallelEdges(node.getSwNum(), swNumToNode, notSortedEdges));
    }
    return edgesStructure;
  }

  @Override
  public void clear() {
    dstSwNumToCachedShortestPathTree.clear();
    cachedEdgesStructure = null;
    lastDst = null;
    cachedTransposedNetwork = null;
  }
}
