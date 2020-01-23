package com.igoryan.ksp.impl;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import com.igoryan.ksp.KShortestPathsCalculator;
import com.igoryan.model.Edge;
import com.igoryan.model.Node;
import com.igoryan.model.ParallelEdges;
import com.igoryan.model.ShortestPath;
import com.igoryan.sp.ShortestPathCalculator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

public abstract class BaseYenKShortestPathsCalculator implements KShortestPathsCalculator {

  protected final ShortestPathCalculator shortestPathCalculator;

  protected BaseYenKShortestPathsCalculator(
      final ShortestPathCalculator shortestPathCalculator) {
    this.shortestPathCalculator = shortestPathCalculator;
  }

  protected void performYenAlgorithm(final Node dst,
      final MutableNetwork<Node, ParallelEdges> network, final int count,
      final List<ShortestPath> result) {
    if (result.isEmpty()) {
      // should has first shortest path
      return;
    }
    final PriorityQueue<ShortestPath> storage =
        new PriorityQueue<>(Comparator.comparingLong(ShortestPath::getCost));
    for (int k = 1; k < count; k++) {
      ShortestPath previousShortest = result.get(k - 1);
      for (int i = 0; i < previousShortest.getVertexes().size() - 1; i++) {
        // The sequence of nodes from the source to the spur node of the previous k-shortest path.
        final ShortestPath rootPath = previousShortest.subPath(i);
        // Remove the links that are part of the previous shortest paths which share the same
        // root path.
        final Map<EndpointPair<Node>, List<Edge>> removedEdges = new HashMap<>();
        final List<Node> removedNodes = new ArrayList<>();
        final Map<EndpointPair<Node>, ParallelEdges> endPointPairToRemovedEdges = new HashMap<>();
        for (ShortestPath shortestPath : result) {
          if (shortestPath.containsSubPath(rootPath)) {
            final EndpointPair<Node> nodePair = EndpointPair
                .ordered(shortestPath.getVertexes().get(i), shortestPath.getVertexes().get(i + 1));
            final Edge removedEdge = shortestPath.getEdges().get(i);
            final ParallelEdges parallelEdges = network.edgeConnectingOrNull(nodePair);

            parallelEdges.remove(removedEdge);
            if (parallelEdges.isEmpty()) {
              network.removeEdge(parallelEdges);
              endPointPairToRemovedEdges.put(nodePair, parallelEdges);
            }
            removedEdges
                .computeIfAbsent(nodePair, key -> new ArrayList<>())
                .add(removedEdge);
          }
        }
        // remove each node in root path except spurNode
        for (int j = 0; j < i; j++) {
          final Node node = rootPath.getVertexes().get(j);
          removedNodes.add(node);
          network.incidentEdges(node).forEach(edge -> {
            final EndpointPair<Node> endpointPair = network.incidentNodes(edge);
            endPointPairToRemovedEdges.put(endpointPair, edge);
          });
          network.removeNode(node);
        }
        // Spur node is retrieved from the previous k-shortest path, k − 1.
        final Node spurNode = previousShortest.getVertexes().get(i);
        final ShortestPath spurPath =
            shortestPathCalculator.calculateShortestPath(spurNode, dst, network, false);
        if (spurPath != null) {
          ShortestPath totalPath = rootPath.append(spurPath);
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
      ShortestPath kthPath;
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
}
