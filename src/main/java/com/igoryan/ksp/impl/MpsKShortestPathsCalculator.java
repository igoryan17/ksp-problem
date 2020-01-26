package com.igoryan.ksp.impl;

import static java.util.Collections.emptyList;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.inject.Inject;
import com.igoryan.ksp.KShortestPathsCalculator;
import com.igoryan.model.Edge;
import com.igoryan.model.KShortestPathsTree;
import com.igoryan.model.Node;
import com.igoryan.model.NodeEdgeTuple;
import com.igoryan.model.ParallelEdges;
import com.igoryan.model.ShortestPath;
import com.igoryan.model.ShortestPathsTree;
import com.igoryan.model.SortedParallelEdges;
import com.igoryan.sp.ShortestPathCalculator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

public class MpsKShortestPathsCalculator implements KShortestPathsCalculator {

  private static final Comparator<ShortestPath> COMPARE_SHORTEST_PATHS_BY_REDUCED_COST =
      Comparator.comparingLong(ShortestPath::getReducedCost);

  final ShortestPathCalculator shortestPathCalculator;

  @Inject
  public MpsKShortestPathsCalculator(
      final ShortestPathCalculator shortestPathCalculator) {
    this.shortestPathCalculator = shortestPathCalculator;
  }

  @Override
  public List<ShortestPath> calculate(final Node src, final Node dst,
      final MutableNetwork<Node, ParallelEdges> network, final int count) {
    final Network<Node, ParallelEdges> reversed = Graphs.transpose(network);
    final ShortestPathsTree shortestPathsTree =
        shortestPathCalculator.calculateOfReversed(dst, src, reversed, true);
    final Map<Integer, SortedParallelEdges> edgesStructure = new HashMap<>(network.nodes().size());
    network.edges().forEach(parallelEdges -> {
      for (Edge edge : parallelEdges) {
        final long costFromHead = shortestPathsTree.getCost(edge.getDstSwNum());
        final long costFromTail = shortestPathsTree.getCost(edge.getSrcSwNum());
        if (Long.MAX_VALUE == costFromHead || Long.MAX_VALUE == costFromTail) {
          edge.setReducedCost(Long.MAX_VALUE);
          continue;
        }
        edge.setReducedCost(costFromHead - costFromTail + edge.getCost());
      }
      edgesStructure.put(parallelEdges.getSrcSwNum(), new SortedParallelEdges(parallelEdges));
    });
    final Queue<ShortestPath> candidates =
        new PriorityQueue<>(COMPARE_SHORTEST_PATHS_BY_REDUCED_COST);
    final ShortestPath firstShortestPath =
        shortestPathsTree.getShortestPathWithReducedCost(src.getSwNum());
    if (firstShortestPath == null) {
      return emptyList();
    }
    candidates.add(firstShortestPath);
    int currentCount = 0;
    final List<ShortestPath> result = new ArrayList<>();
    final KShortestPathsTree kShortestPathsTree = new KShortestPathsTree();
    while (!candidates.isEmpty() && currentCount <= count) {
      final ShortestPath shortestPath = candidates.poll();
      final NodeEdgeTuple nodeEdgeTuple = kShortestPathsTree.getDeviationKey(shortestPath);
      if (!shortestPath.hasCycles()) {
        currentCount++;
        result.add(shortestPath);
        kShortestPathsTree.add(shortestPath, count - 1);
      }
      if (nodeEdgeTuple == null) {
        continue;
      }
      Node deviationNode = nodeEdgeTuple.getNode();
      ShortestPath toDeviationNode = null;
      int indexOfTarget = shortestPath.getIndex(shortestPath.getDst());
      int nodeIndex = shortestPath.getIndex(deviationNode);
      do {
        deviationNode = shortestPath.getNodes().get(nodeIndex);
        final SortedParallelEdges sortedParallelEdges =
            edgesStructure.get(deviationNode.getSwNum());
        final Edge[] parallelEdges = sortedParallelEdges.getSortedEdges();
        toDeviationNode = shortestPath.subPath(nodeIndex);
        Edge deviationEdge = null;
        for (int i = sortedParallelEdges.getIndex(shortestPath.getEdges().get(nodeIndex)) + 1;
            i < parallelEdges.length; i++) {
          final Edge edge = parallelEdges[i];
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
        final EndpointPair<Node> nodesOfDeviationEdge =
            network.incidentNodes(sortedParallelEdges.getParallelEdges());
        if (nodesOfDeviationEdge.target() == dst) {
          candidates.add(toDeviationNode.append(deviationEdge, dst));
          nodeIndex++;
          continue;
        }
        final ShortestPath fromHeadOfDeviationNodeToDst =
            shortestPathsTree.getShortestPathWithReducedCost(deviationEdge.getDstSwNum());
        if (fromHeadOfDeviationNodeToDst != null) {
          candidates.add(toDeviationNode.append(deviationEdge, nodesOfDeviationEdge.target())
              .append(fromHeadOfDeviationNodeToDst));
        }
        nodeIndex++;
      } while (!Objects.requireNonNull(toDeviationNode).hasCycles() && nodeIndex != indexOfTarget);
    }
    return result;
  }
}
