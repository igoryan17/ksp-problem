package com.igoryan.model;

import java.util.List;
import lombok.NonNull;

@FunctionalInterface
public interface ShortestPathCreator<T extends ShortestPath> {
  T create(final @NonNull Node src, final @NonNull Node dst, final @NonNull List<Edge> edges,
      final @NonNull List<Node> nodes);
}
