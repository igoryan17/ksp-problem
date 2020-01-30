package com.igoryan.util;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.ParallelEdges;
import com.igoryan.model.tree.ReversedShortestPathTree;
import com.igoryan.model.path.ShortestPath;
import com.igoryan.model.path.ShortestPathCreator;
import com.igoryan.model.tree.ShortestPathsTree;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
    final Map<Integer, Node> swNumToNodeOfTree = new HashMap<>();
    final Map<Integer, Node> swNumToOriginalNode = new HashMap<>();
    swNumToNodeOfTree.put(src.getSwNum(), new Node(src, null));
    swNumToOriginalNode.put(src.getSwNum(), src);
    for (final Node node : nodes) {
      final Node predecessor = node.getNodePredecessor();
      if (predecessor == null) {
        continue;
      }
      swNumToOriginalNode.putIfAbsent(node.getSwNum(), node);
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
    final Map<Integer, Node> swNumToOriginalNode = new HashMap<>();
    swNumToNodeOfTree.put(dst.getSwNum(), new Node(dst, null));
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

  public static Node addNode(final @NonNull Node node,
      final @NonNull Map<Integer, Node> swNumToNodeOfTree) {
    Node predecessor = swNumToNodeOfTree.get(node.getNodePredecessor().getSwNum());
    if (predecessor == null) {
      predecessor = Objects.requireNonNull(addNode(node.getNodePredecessor(), swNumToNodeOfTree));
    }
    final Node result = new Node(node, predecessor);
    swNumToNodeOfTree.put(node.getSwNum(), result);
    return result;
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
