package com.igoryan.model.tree;

import com.igoryan.model.network.Edge;
import com.igoryan.model.network.Node;
import com.igoryan.model.path.ShortestPath;
import com.igoryan.model.path.ShortestPathCreator;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

public class ReversedShortestPathTree<T extends ShortestPath> extends BaseShortestPathsTree<T> {

  private final int dst;

  public ReversedShortestPathTree(
      final @NonNull Map<Integer, Node> swNumToNode,
      final @NonNull Map<Integer, Node> swNumToOriginalNode,
      final @NonNull Class<T> clazz,
      final @NonNull ShortestPathCreator shortestPathCreator,
      final int dst) {
    super(swNumToNode, swNumToOriginalNode, clazz, shortestPathCreator);
    this.dst = dst;
  }

  @Override
  protected List<Edge> buildEdges(final Node node) {
    return node.buildReversed();
  }

  @Override
  protected T buildShortestPath(final Node node) {
    return node.buildReversedShortestPath(clazz, shortestPathCreator, swNumToOriginalNode);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ReversedShortestPathTree<?> that = (ReversedShortestPathTree<?>) o;

    return dst == that.dst;
  }

  @Override
  public int hashCode() {
    return dst;
  }
}
