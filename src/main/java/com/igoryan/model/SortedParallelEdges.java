package com.igoryan.model;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode(of = "parallelEdges")
public final class SortedParallelEdges {

  private static final Comparator<Edge> COMPARE_EDGES_BY_REDUCED_COST =
      Comparator.comparingLong(Edge::getReducedCost);

  @Getter
  private final ParallelEdges parallelEdges;
  private final Map<Edge, Integer> edgeToIndex;
  @Getter
  private final Edge[] sortedEdges;

  public SortedParallelEdges(final @NonNull ParallelEdges parallelEdges) {
    this.parallelEdges = parallelEdges;
    this.sortedEdges = parallelEdges.toArray(new Edge[]{});
    Arrays.sort(this.sortedEdges, COMPARE_EDGES_BY_REDUCED_COST);
    edgeToIndex = new HashMap<>(this.sortedEdges.length);
    for (int i = 0; i < this.sortedEdges.length; i++) {
      edgeToIndex.put(this.sortedEdges[i], i);
    }
  }

  public int getIndex(final @NonNull Edge edge) {
    return Objects.requireNonNull(edgeToIndex.get(edge));
  }
}
