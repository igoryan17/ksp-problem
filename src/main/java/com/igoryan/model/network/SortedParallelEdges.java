package com.igoryan.model.network;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode(of = "srcSwNum")
public final class SortedParallelEdges {

  private static final Comparator<Edge> COMPARE_EDGES_BY_REDUCED_COST =
      Comparator.comparingLong(Edge::getReducedCost)
          .thenComparingInt(Edge::getSrcPort).thenComparingInt(Edge::getDstPort);

  private final int srcSwNum;

  // only target nodes of edges
  private final Map<Integer, Node> swNumToNode;

  @Getter
  private final TreeSet<Edge> sortedEdges;

  public SortedParallelEdges(final int srcSwNum,
      final Map<Integer, Node> swNumToNode,
      final @NonNull List<Edge> notSortedEdges) {
    this.srcSwNum = srcSwNum;
    this.swNumToNode = swNumToNode;
    this.sortedEdges = new TreeSet<>(COMPARE_EDGES_BY_REDUCED_COST);
    sortedEdges.addAll(notSortedEdges);
    this.sortedEdges.addAll(notSortedEdges);
  }

  public void addAll(final @NonNull Collection<Edge> edgesWithSameDst,
      final @NonNull Node dstNode) {
    swNumToNode.put(dstNode.getSwNum(), dstNode);
    sortedEdges.addAll(edgesWithSameDst);
  }

  public void removeAll(final @NonNull Collection<Edge> edges, final @NonNull Node dst) {
    sortedEdges.removeAll(edges);
  }

  public Node getTargetNode(final @NonNull Edge edge) {
    return Objects.requireNonNull(swNumToNode.get(edge.getDstSwNum()));
  }
}
