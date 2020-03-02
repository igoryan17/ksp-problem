package com.igoryan.model.network.edge;

import java.util.Collection;
import javax.annotation.Nullable;
import lombok.NonNull;

public interface ParallelEdges {

  void markAsUsed(final @NonNull Edge edge);

  boolean allUsed();

  @Nullable
  Edge getFirstUnusedOrNull();

  Edge getFirstUnusedIfPossible();

  void clearMarkedEdges();

  Collection<Edge> getEdges();
}
