package com.igoryan.ksp.impl;

import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import com.igoryan.ksp.KShortestPathsCalculator;
import com.igoryan.model.Edge;
import com.igoryan.model.Node;
import com.igoryan.model.ParallelEdges;
import com.igoryan.model.ShortestPath;
import com.igoryan.model.ShortestPathCreator;
import com.igoryan.model.ShortestPathsTree;
import com.igoryan.model.YenShortestPath;
import com.igoryan.sp.ShortestPathCalculator;
import com.igoryan.sp.util.ShortestPathsUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class BaseYenKShortestPathsCalculator
    implements KShortestPathsCalculator<YenShortestPath> {

  protected static final ShortestPathCreator<YenShortestPath> SHORTEST_PATH_CREATOR =
      (src, dst, edges, nodes) -> new YenShortestPath(src, dst, edges, nodes, dst.getDistance());

  protected final Map<Integer, ShortestPathsTree<YenShortestPath>>
      srcSwNumToCachedShortestPathTree =
      new HashMap<>();

  protected final ShortestPathCalculator shortestPathCalculator;

  protected BaseYenKShortestPathsCalculator(
      final ShortestPathCalculator shortestPathCalculator) {
    this.shortestPathCalculator = shortestPathCalculator;
  }

  protected YenShortestPath getFirstShortestPath(final Node src, final Node dst,
      final MutableNetwork<Node, ParallelEdges> network) {
    final ShortestPathsTree<YenShortestPath> pathsTree =
        srcSwNumToCachedShortestPathTree.computeIfAbsent(src.getSwNum(), key -> {
          shortestPathCalculator.calculate(src, dst, network);
          return ShortestPathsUtil
              .buildRecursively(YenShortestPath.class, src, SHORTEST_PATH_CREATOR, network.nodes());
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
        final Map<EndpointPair<Node>, List<Edge>> removedEdges = new HashMap<>();
        final List<Node> removedNodes = new ArrayList<>();
        final Map<EndpointPair<Node>, ParallelEdges> endPointPairToRemovedEdges = new HashMap<>();
        for (YenShortestPath shortestPath : result) {
          if (!shortestPath.containsSubPath(rootPath)) {
            continue;
          }
          final EndpointPair<Node> nodePair = shortestPath.getIncidentNodes(i);
          final Edge removedEdge = shortestPath.getEdges().get(i);
          final ParallelEdges parallelEdges = network.edgeConnectingOrNull(nodePair);
          if (parallelEdges == null) {
            // already removed, because all edges covered
            continue;
          }
          parallelEdges.remove(removedEdge);
          if (parallelEdges.isEmpty()) {
            network.removeEdge(parallelEdges);
            parallelEdges.add(removedEdge);
            endPointPairToRemovedEdges.put(nodePair, parallelEdges);
          } else {
            removedEdges
                .computeIfAbsent(nodePair, key -> new ArrayList<>())
                .add(removedEdge);
          }
        }
        // remove each node in root path except spurNode
        for (int j = 0; j < i; j++) {
          final Node node = rootPath.getNodes().get(j);
          removedNodes.add(node);
          network.incidentEdges(node).forEach(edge -> {
            final EndpointPair<Node> endpointPair = network.incidentNodes(edge);
            endPointPairToRemovedEdges.put(endpointPair, edge);
          });
          network.removeNode(node);
        }
        // Spur node is retrieved from the previous k-shortest path, k − 1.
        final Node spurNode = previousShortest.getNodes().get(i);
        final YenShortestPath spurPath =
            shortestPathCalculator
                .calculate(YenShortestPath.class, spurNode, dst, network, SHORTEST_PATH_CREATOR);
        if (spurPath != null) {
          YenShortestPath totalPath = rootPath.append(spurPath);
          if (storage.size() == (count - k)) {
            storage.removeLast();
          }
          storage.add(totalPath);
        }
        removedNodes.forEach(network::addNode);
        endPointPairToRemovedEdges.forEach(network::addEdge);
        removedEdges.forEach((nodePair, edges) -> {
          final ParallelEdges parallelEdges =
              Objects.requireNonNull(network.edgeConnectingOrNull(nodePair));
          parallelEdges.addAll(edges);
        });
      }
      boolean isNewPath;
      YenShortestPath kthPath;
      do {
        kthPath = storage.poll();
        isNewPath = true;
        if (kthPath != null) {
          for (ShortestPath p : result) {
            // Check to see if this candidate path duplicates a previously found path
            // compare by hash code firstly to fast
            if (p.hashCode() == kthPath.hashCode() && p.equals(kthPath)) {
              isNewPath = false;
              break;
            }
          }
        }
      } while (!isNewPath);
      if (kthPath == null) {
        break;
      }
      result.add(kthPath);
    }
  }

  @Override
  public void clear() {
    srcSwNumToCachedShortestPathTree.clear();
  }
}
