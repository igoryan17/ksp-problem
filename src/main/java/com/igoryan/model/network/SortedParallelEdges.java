package com.igoryan.model.network;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode(of = "srcSwNum")
public final class SortedParallelEdges {

  private static final Comparator<Edge> COMPARE_EDGES_BY_REDUCED_COST =
      Comparator.comparingLong(Edge::getReducedCost);

  private final int srcSwNum;

  private final Map<Edge, Integer> edgeToIndex;

  // only target nodes of edges
  private final Map<Integer, Node> swNumToNode;

  @Getter
  private final List<Edge> sortedEdges;

  public SortedParallelEdges(final int srcSwNum,
      final Map<Integer, Node> swNumToNode,
      final @NonNull List<Edge> notSortedEdges) {
    this.srcSwNum = srcSwNum;
    this.swNumToNode = swNumToNode;
    notSortedEdges.sort(COMPARE_EDGES_BY_REDUCED_COST);
    this.sortedEdges = notSortedEdges;
    this.edgeToIndex = new HashMap<>(this.sortedEdges.size());
    for (int i = 0; i < this.sortedEdges.size(); i++) {
      edgeToIndex.put(this.sortedEdges.get(i), i);
    }
  }

  public int getIndex(final @NonNull Edge edge) {
    return Objects.requireNonNull(edgeToIndex.get(edge),
        "no mapping for edge; srcSwNum: " + srcSwNum + " ,edge: " + edge);
  }

  public Node getTargetNode(final @NonNull Edge edge) {
    return Objects.requireNonNull(swNumToNode.get(edge.getDstSwNum()));
  }
}
