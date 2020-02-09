package com.igoryan.model.network;

import static java.util.Collections.unmodifiableList;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;

public final class SortedParallelEdges {

  protected static final Comparator<Edge> COMPARE_EDGES_BY_REDUCED_COST_AND_PORTS =
      Comparator.comparingLong(Edge::getReducedCost);
  @Getter
  private final List<Edge> sortedEdges;
  private final Map<Edge, Integer> edgeToIndex;

  public SortedParallelEdges(final @NonNull List<Edge> parallelEdges) {
    parallelEdges.sort(COMPARE_EDGES_BY_REDUCED_COST_AND_PORTS);
    this.sortedEdges = unmodifiableList(parallelEdges);
    this.edgeToIndex = new HashMap<>(sortedEdges.size());
    for (int i = 0; i < sortedEdges.size(); i++) {
      this.edgeToIndex.put(sortedEdges.get(i), i);
    }
  }

  public int getIndex(final @NonNull Edge edge) {
    return Objects.requireNonNull(edgeToIndex.get(edge));
  }
}
