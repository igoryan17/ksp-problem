package com.igoryan.model.tree;

import com.igoryan.model.network.Edge;
import com.igoryan.model.network.Node;
import com.igoryan.model.path.ShortestPath;
import com.igoryan.model.path.ShortestPathCreator;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

public class ShortestPathsTree<T extends ShortestPath> extends BaseShortestPathsTree<T> {

  private final int src;

  public ShortestPathsTree(final @NonNull Map<Integer, Node> swNumToNode,
      final @NonNull Map<Integer, Node> swNumToOriginalNode,
      final @NonNull Class<T> clazz,
      final @NonNull ShortestPathCreator<T> shortestPathCreator, final int src) {
    super(swNumToNode, swNumToOriginalNode, clazz, shortestPathCreator);
    this.src = src;
  }

  public long getCost(final int dstSwNum) {
    final Node dstNode = swNumToNode.get(dstSwNum);
    if (dstNode == null) {
      return Long.MAX_VALUE;
    }
    return dstNode.getDistance();
  }

  @Override
  protected List<Edge> buildEdges(final Node node) {
    return node.buildPath();
  }

  @Override
  protected T buildShortestPath(final Node node) {
    return node.buildShortestPath(clazz, shortestPathCreator, swNumToOriginalNode);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ShortestPathsTree<?> that = (ShortestPathsTree<?>) o;

    return src == that.src;
  }

  @Override
  public int hashCode() {
    return src;
  }
}


