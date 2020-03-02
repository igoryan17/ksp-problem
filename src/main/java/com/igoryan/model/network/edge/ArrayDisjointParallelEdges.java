package com.igoryan.model.network.edge;

import java.util.List;
import lombok.NonNull;

public final class ArrayDisjointParallelEdges extends ArrayParallelEdges {

  public ArrayDisjointParallelEdges(final int srcSwNum, final int dstSwNum,
      final @NonNull List<Edge> edges) {
    super(srcSwNum, dstSwNum, edges);
  }

  @Override
  public void markAsUsed(final @NonNull Edge edge) {
    if (edge.isUsed()) {
      return;
    }
    if (firstUnusedIndex >= edges.size()) {
      return;
    }
    final Edge nextFree = edges.get(firstUnusedIndex);
    if (nextFree.equals(edge)) {
      edge.setUsed(true);
      firstUnusedIndex++;
    } else {
      throw new IllegalArgumentException(
          "edge should be next free edge; edge: " + edge + ", nextFreeEdge: " + nextFree);
    }
  }

  @Override
  public void clearMarkedEdges() {
    super.clearMarkedEdges();
    edges.forEach(edge -> edge.setUsed(false));
  }
}
