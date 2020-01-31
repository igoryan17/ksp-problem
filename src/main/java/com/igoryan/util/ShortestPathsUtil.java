package com.igoryan.util;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.ParallelEdges;
import com.igoryan.model.path.ShortestPath;
import com.igoryan.model.path.ShortestPathCreator;
import com.igoryan.model.tree.ReversedShortestPathTree;
import com.igoryan.model.tree.ShortestPathsTree;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;

public final class ShortestPathsUtil {

  private ShortestPathsUtil() {
  }

  public static <T extends ShortestPath> ShortestPathsTree<T> buildRecursively(
      final @NonNull Class<T> type, final @NonNull Node src,
      final ShortestPathCreator<T> shortestPathCreator,
      final @NonNull Collection<Node> nodes) {
    System.out.println("build shortest paths tree recursively; src:" + src);
    final Map<Integer, Node> swNumToNodeOfTree = new HashMap<>();
    swNumToNodeOfTree.put(src.getSwNum(), new Node(src, null));
    final Map<Integer, Node> swNumToOriginalNode = new HashMap<>();
    swNumToOriginalNode.put(src.getSwNum(), src);
    for (final Node node : nodes) {
      if (node.getNodePredecessor() == null && node.getDistance() != 0) {
        // skip node that can not be reached from source
        continue;
      }
      swNumToOriginalNode.put(node.getSwNum(), node);
      if (swNumToNodeOfTree.containsKey(node.getSwNum())) {
        continue;
      }
      addNode(node, swNumToNodeOfTree);
    }
    return new ShortestPathsTree<>(swNumToNodeOfTree, swNumToOriginalNode, type,
        shortestPathCreator, src.getSwNum());
  }

  public static <T extends ShortestPath> ReversedShortestPathTree<T> buildRevertedRecursively(
      final @NonNull Class<T> type, final @NonNull Node dst,
      final ShortestPathCreator<T> shortestPathCreator,
      final @NonNull Collection<Node> nodes) {
    final Map<Integer, Node> swNumToNodeOfTree = new HashMap<>();
    swNumToNodeOfTree.put(dst.getSwNum(), new Node(dst, null));
    final Map<Integer, Node> swNumToOriginalNode = new HashMap<>();
    swNumToOriginalNode.put(dst.getSwNum(), dst);
    for (final Node node : nodes) {
      final Node predecessor = node.getNodePredecessor();
      if (predecessor == null) {
        continue;
      }
      swNumToOriginalNode.putIfAbsent(node.getSwNum(), node);
      addNode(node, swNumToNodeOfTree);
    }
    return new ReversedShortestPathTree<>(swNumToNodeOfTree, swNumToOriginalNode, type,
        shortestPathCreator, dst.getSwNum());
  }

  public static void addNode(final @NonNull Node node,
      final @NonNull Map<Integer, Node> swNumToNodeOfTree) {
    final Node predecessor = node.getNodePredecessor();
    if (predecessor == null) {
      // it's source
      swNumToNodeOfTree.put(node.getSwNum(), new Node(node, null));
      return;
    }
    // if predecessor isn't copied than add it recursively max depth of recursion is max hops count
    if (!swNumToNodeOfTree.containsKey(predecessor.getSwNum())) {
      addNode(predecessor, swNumToNodeOfTree);
    }
    // get recursively added predecessor
    final Node addedPredecessor = swNumToNodeOfTree.get(predecessor.getSwNum());
    // copy node
    final Node addedNode = new Node(node, addedPredecessor);
    swNumToNodeOfTree.put(addedNode.getSwNum(), addedNode);
  }

  public static Set<Node> getTransitNodes(final @NonNull Network<Node, ParallelEdges> network) {
    return network.nodes().stream()
        .filter(Node::isTransit)
        .collect(Collectors.toSet());
  }

  public static void addNodeToTransitSubGraph(final @NonNull Node node,
      final @NonNull MutableNetwork<Node, ParallelEdges> subNetworkWithTransit,
      final @NonNull MutableNetwork<Node, ParallelEdges> network) {

    if (node.isTransit()) {
      return;
    }
    subNetworkWithTransit.addNode(node);
    network.incidentEdges(node)
        .forEach(edge -> subNetworkWithTransit.addEdge(network.incidentNodes(edge), edge));
  }

  public static void removeNodeFromTransitSubGraph(final @NonNull Node node,
      final @NonNull MutableNetwork<Node, ParallelEdges> subNetworkWithTransit) {

    if (node.isTransit()) {
      return;
    }
    subNetworkWithTransit.removeNode(node);
  }
}
