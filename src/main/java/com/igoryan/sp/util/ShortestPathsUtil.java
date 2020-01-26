package com.igoryan.sp.util;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.igoryan.model.Node;
import com.igoryan.model.ParallelEdges;
import com.igoryan.model.ShortestPathsTree;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;

public final class ShortestPathsUtil {

  private ShortestPathsUtil() {
  }

  public static ShortestPathsTree buildRecursively(final @NonNull Node src,
      final @NonNull Collection<Node> nodes, final boolean reversed) {
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
    return new ShortestPathsTree(src.getSwNum(), swNumToNodeOfTree, swNumToOriginalNode, reversed);
  }

  public static Node addNode(final @NonNull Node node,
      final @NonNull Map<Integer, Node> swNumToNodeOfTree) {
    final Node predecessor = swNumToNodeOfTree.get(node.getNodePredecessor().getSwNum());
    if (predecessor == null) {
      addNode(node.getNodePredecessor(), swNumToNodeOfTree);
    }
    return swNumToNodeOfTree.put(node.getSwNum(), new Node(node, predecessor));
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
