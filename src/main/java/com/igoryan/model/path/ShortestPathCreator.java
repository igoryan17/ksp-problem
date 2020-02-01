package com.igoryan.model.path;

import com.igoryan.model.network.Edge;
import com.igoryan.model.network.Node;
import java.util.List;
import lombok.NonNull;

@FunctionalInterface
public interface ShortestPathCreator<T extends ShortestPath> {
  T create(final @NonNull Node src, final @NonNull Node dst, final @NonNull List<Edge> edges,
      final @NonNull List<Node> nodes);
}
