package com.igoryan.model.network.edge;

import static com.igoryan.model.network.edge.Edge.COMPARE_EDGES_BY_COST_AND_PORTS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import javax.annotation.Nullable;
import lombok.NonNull;

public final class QueueYenParallelEdges extends BaseParallelEdges {

  protected final PriorityQueue<Edge> edges;
  private final List<Edge> used = new ArrayList<>();

  public QueueYenParallelEdges(final int srcSwNum, final int dstSwNum,
      final @NonNull List<Edge> edges) {
    super(srcSwNum, dstSwNum);
    this.edges = new PriorityQueue<>(edges.size(), COMPARE_EDGES_BY_COST_AND_PORTS);
    this.edges.addAll(edges);
  }

  @Override
  public void markAsUsed(final @NonNull Edge edge) {
    if (edges.remove(edge)) {
      used.add(edge);
    }
  }

  @Override
  public boolean allUsed() {
    return edges.isEmpty();
  }

  @Nullable
  @Override
  public Edge getFirstUnusedOrNull() {
    return edges.isEmpty() ? null : edges.peek();
  }

  @Override
  public Edge getFirstUnusedIfPossible() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clearMarkedEdges() {
    edges.addAll(used);
    used.clear();
  }

  @Override
  public Collection<Edge> getEdges() {
    return edges;
  }
}
