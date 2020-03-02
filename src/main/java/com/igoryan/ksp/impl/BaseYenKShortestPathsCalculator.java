package com.igoryan.ksp.impl;

import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import com.igoryan.ksp.KShortestPathsCalculator;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.edge.Edge;
import com.igoryan.model.network.edge.ParallelEdges;
import com.igoryan.model.path.ShortestPath;
import com.igoryan.model.path.YenShortestPath;
import com.igoryan.model.tree.ShortestPathsTree;
import com.igoryan.sp.ShortestPathCalculator;
import com.igoryan.util.ShortestPathsUtil;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "com.igoryan.ksp")
abstract class BaseYenKShortestPathsCalculator
    implements KShortestPathsCalculator<YenShortestPath> {

  protected final Map<Integer, ShortestPathsTree<YenShortestPath>>
      srcSwNumToCachedShortestPathTree = new HashMap<>();

  protected final ShortestPathCalculator shortestPathCalculator;
  protected final ShortestPathCalculator shortestPathCalculatorWithNoTransit;

  protected BaseYenKShortestPathsCalculator(
      final ShortestPathCalculator shortestPathCalculator,
      final ShortestPathCalculator shortestPathCalculatorWithNoTransit) {
    this.shortestPathCalculator = shortestPathCalculator;
    this.shortestPathCalculatorWithNoTransit = shortestPathCalculatorWithNoTransit;
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

  protected void performYenAlgorithm(final Node dst,
      final MutableNetwork<Node, ParallelEdges> network, final int count,
      final List<YenShortestPath> result) {
    if (result.isEmpty()) {
      // should has first shortest path
      return;
    }
    final MinMaxPriorityQueue<YenShortestPath> storage =
        MinMaxPriorityQueue.<YenShortestPath>orderedBy(
            Comparator.comparingLong(ShortestPath::getCost))
            .maximumSize(count)
            .create();
    for (int k = 1; k < count; k++) {
      final YenShortestPath previousShortest = result.get(k - 1);
      for (int i = 0; i < previousShortest.getNodes().size() - 1; i++) {
        // The sequence of nodes from the source to the spur node of the previous k-shortest path.
        final YenShortestPath rootPath = previousShortest.subPath(i);
        // Remove the links that are part of the previous shortest paths which share the same
        // root path.
        final Set<EndpointPair<Node>> pairsWithMarkedEdges = new HashSet<>();
        final Map<EndpointPair<Node>, ParallelEdges> endPointPairToRemovedEdges = new HashMap<>();
        for (YenShortestPath shortestPath : result) {
          if (!shortestPath.containsSubPath(rootPath)) {
            continue;
          }
          final EndpointPair<Node> nodePair = shortestPath.getIncidentNodes(i);
          log.trace("remove edge of shortest path after spur node: nodePair: {}", nodePair);
          final Edge removedEdge = shortestPath.getEdges().get(i);
          log.trace("remove edge from network; removedEdge: {}, nodePair: {}", removedEdge,
              nodePair);
          final ParallelEdges parallelEdges = network.edgeConnectingOrNull(nodePair);
          if (parallelEdges == null) {
            // already removed
            continue;
          }
          parallelEdges.markAsUsed(removedEdge);
          pairsWithMarkedEdges.add(nodePair);
          if (parallelEdges.allUsed()) {
            network.removeEdge(parallelEdges);
            endPointPairToRemovedEdges.put(nodePair, parallelEdges);
          }
        }
        // set nodes of root path as no transit, all nodes of root path are transit under definition
        for (int j = 0; j < i; j++) {
          final Node node = rootPath.getNodes().get(j);
          node.setTransit(false);
        }
        // Spur node is retrieved from the previous k-shortest path, k − 1.
        final Node spurNode = previousShortest.getNodes().get(i);
        final YenShortestPath spurPath = shortestPathCalculatorWithNoTransit
            .calculate(YenShortestPath.class, spurNode, dst, network,
                YenShortestPath.YEN_SHORTEST_PATH_CREATOR);
        if (spurPath != null) {
          YenShortestPath totalPath = rootPath.append(spurPath);
          if (storage.size() == (count - k)) {
            storage.removeLast();
          }
          storage.add(totalPath);
        }
        // set transit nodes of root path to transit back
        for (int j = 0; j < i; j++) {
          final Node node = rootPath.getNodes().get(j);
          node.setTransit(true);
        }
        endPointPairToRemovedEdges.forEach(network::addEdge);
        pairsWithMarkedEdges.forEach(nodePair -> {
          final ParallelEdges parallelEdges =
              Objects.requireNonNull(network.edgeConnectingOrNull(nodePair));
          parallelEdges.clearMarkedEdges();
        });
      }
      final YenShortestPath kthPath = storage.poll();
      if (kthPath == null) {
        break;
      }
      result.add(k, kthPath);
    }
  }

  @Override
  public void clear() {
    srcSwNumToCachedShortestPathTree.clear();
    shortestPathCalculator.clear();
    shortestPathCalculatorWithNoTransit.clear();
  }
}
