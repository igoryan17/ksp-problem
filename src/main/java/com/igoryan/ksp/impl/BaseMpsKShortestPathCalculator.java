package com.igoryan.ksp.impl;

import static java.util.Collections.emptyList;

import com.google.common.graph.EndpointPair;
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
import java.util.stream.Collectors;
import org.apache.logging.log4j.Logger;

public abstract class BaseMpsKShortestPathCalculator
    implements KShortestPathsCalculator<MpsShortestPath> {

  protected static final Comparator<MpsShortestPath> COMPARE_SHORTEST_PATHS_BY_COST =
      Comparator.comparingLong(MpsShortestPath::getOriginalCost);
  protected static final ShortestPathCreator<MpsShortestPath>
      MPS_SHORTEST_PATH_SHORTEST_PATH_CREATOR = MpsShortestPath::new;
  protected final ShortestPathCalculator shortestPathCalculator;
  protected final Map<Integer, SortedParallelEdges> cachedEdgesStructure = new HashMap<>();
  protected final Map<Integer, Node> swNumToNode = new HashMap<>();
  protected ReversedShortestPathTree<MpsShortestPath> cachedShortestPathTree;
  protected Node lastDst;
  protected Network<Node, ParallelEdges> cachedTransposedNetwork;
  protected boolean needCheckCycles = true;

  protected BaseMpsKShortestPathCalculator(
      final ShortestPathCalculator shortestPathCalculator) {
    this.shortestPathCalculator = shortestPathCalculator;
  }

  protected ReversedShortestPathTree<MpsShortestPath> calculateShortestPathTree(final Node src,
      final Node dst, final Network<Node, ParallelEdges> network) {
    shortestPathCalculator.calculate(dst, src, cachedTransposedNetwork);
    calcReducedCost(network);
    return ShortestPathsUtil.buildRevertedRecursively(MpsShortestPath.class, dst,
        MPS_SHORTEST_PATH_SHORTEST_PATH_CREATOR, network.nodes());
  }

  protected List<MpsShortestPath> performMpsAlgorithm(final Node src, final Node dst,
      final int count) {
    log().debug("calc {} shortest paths; src: {}, dst: {}", count, src, dst);
    final KShortestPathsTree candidatesKShortestPathsTree = new KShortestPathsTree();
    final Queue<MpsShortestPath> candidates = new PriorityQueue<>(COMPARE_SHORTEST_PATHS_BY_COST);
    final MpsShortestPath firstShortestPath =
        cachedShortestPathTree.getShortestPath(src.getSwNum());
    if (firstShortestPath == null) {
      return emptyList();
    }
    candidates.add(firstShortestPath);
    candidatesKShortestPathsTree.add(firstShortestPath);
    int currentCount = 0;
    final List<MpsShortestPath> result = new ArrayList<>();
    while (!candidates.isEmpty() && currentCount <= count) {
      final MpsShortestPath shortestPath = candidates.poll();
      log().trace("candiadate for {} shortest path is found; path: {}", currentCount + 1,
          shortestPath);
      final NodeEdgeTuple nodeEdgeTuple = Objects.requireNonNull(shortestPath.getDeviationNode());
      log().trace("deviation key of candidate is calculated; key: {}", nodeEdgeTuple);
      if (!needCheckCycles || shortestPath.withoutLoops()) {
        currentCount++;
        result.add(shortestPath);
        log().trace("{} shortest path is found; path: {}", currentCount, shortestPath);
      }
      Node deviationNode = nodeEdgeTuple.getNode();
      MpsShortestPath toDeviationNode;
      int indexOfTarget = shortestPath.getIndex(shortestPath.getDst());
      int nodeIndex = shortestPath.getIndex(deviationNode);
      do {
        deviationNode = shortestPath.getNodes().get(nodeIndex);
        log().trace("loop of deviation nodes: node: {}", deviationNode);
        final SortedParallelEdges sortedParallelEdges =
            cachedEdgesStructure.get(deviationNode.getSwNum());
        toDeviationNode = shortestPath.subPath(nodeIndex);
        if (sortedParallelEdges == null) {
          log().warn("there is not edges structure of deviation node; deviationNode: {}",
              deviationNode);
          nodeIndex++;
          continue;
        }
        log().trace("out edges of deviation node; deviationNode: {}, edges: {}", deviationNode,
            sortedParallelEdges);
        Edge deviationEdge = null;
        final Edge deviationEdgeOfShortestPath = shortestPath.getEdges().get(nodeIndex);
        log().trace("find edges less than edge of shortest path, deviationEdgeOfShortestPath: {}",
            deviationEdgeOfShortestPath);
        final List<Edge> sortedEdges = sortedParallelEdges.getSortedEdges();
        for (int i = sortedParallelEdges.getIndex(shortestPath.getEdges().get(nodeIndex)) + 1;
            i < sortedEdges.size(); i++) {
          final Edge edge = sortedEdges.get(i);
          if (needCheckCycles && toDeviationNode.formsCycle(edge)) {
            continue;
          }
          deviationEdge = edge;
          break;
        }
        log().trace("deviation edge is calculated; deviationNode: {}, edge: {}", deviationNode,
            deviationEdge);
        if (deviationEdge == null) {
          nodeIndex++;
          continue;
        }
        final Node targetNodeOfDeviationEdge = swNumToNode.get(deviationEdge.getDstSwNum());
        final MpsShortestPath.Builder builder = MpsShortestPath.builder()
            .from(toDeviationNode);
        if (targetNodeOfDeviationEdge == dst) {
          log().trace("target of deviation edge is dst; edge: {}", deviationEdge);
          final MpsShortestPath candidate = builder.append(deviationEdge, dst).build();
          candidates.add(candidate);
          candidatesKShortestPathsTree.add(candidate);
          nodeIndex++;
          continue;
        }
        final List<Edge> fromHeadOfDeviationEdgeToDst =
            cachedShortestPathTree.getPath(deviationEdge.getDstSwNum());
        log().trace("path from head of deviation node to dst is calculated; path: {}",
            fromHeadOfDeviationEdgeToDst);
        if (fromHeadOfDeviationEdgeToDst.isEmpty()) {
          nodeIndex++;
          continue;
        }
        final MpsShortestPath candidate = builder
            .append(deviationEdge, targetNodeOfDeviationEdge)
            .append(fromHeadOfDeviationEdgeToDst, cachedShortestPathTree.getSwNumToOriginalNode())
            .build();
        candidates.add(candidate);
        candidatesKShortestPathsTree.add(candidate);
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

  protected void fillEdgesStructure(
      final Network<Node, ParallelEdges> network) {
    for (Node node : network.nodes()) {
      swNumToNode.put(node.getSwNum(), node);
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
      final SortedParallelEdges sortedParallelEdges = new SortedParallelEdges(notSortedEdges);
      cachedEdgesStructure.put(node.getSwNum(), sortedParallelEdges);
    }
  }

  abstract Logger log();

  @Override
  public void clear() {
    cachedEdgesStructure.clear();
    swNumToNode.clear();
    needCheckCycles = true;
    cachedShortestPathTree = null;
    cachedTransposedNetwork = null;
    lastDst = null;
  }
}
