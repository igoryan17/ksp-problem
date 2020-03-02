package com.igoryan.model.network.edge;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

abstract class ArrayParallelEdges extends BaseParallelEdges {

  protected final List<Edge> edges;
  protected int firstUnusedIndex;

  protected ArrayParallelEdges(final int srcSwNum, final int dstSwNum,
      final List<Edge> edges) {
    super(srcSwNum, dstSwNum);
    this.edges = edges;
  }

  @Override
  public boolean allUsed() {
    return firstUnusedIndex >= edges.size();
  }

  @Nullable
  @Override
  public Edge getFirstUnusedOrNull() {
    return firstUnusedIndex < edges.size() ? edges.get(firstUnusedIndex) : null;
  }

  @Override
  public Edge getFirstUnusedIfPossible() {
    return edges.get(firstUnusedIndex < edges.size() ? firstUnusedIndex : 0);
  }

  @Override
  public void clearMarkedEdges() {
    firstUnusedIndex = 0;
  }

  @Override
  public Collection<Edge> getEdges() {
    return edges;
  }
}
