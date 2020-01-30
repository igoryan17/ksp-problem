package com.igoryan.model;

import static java.util.Collections.emptyList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;

public abstract class BaseShortestPathsTree<T extends ShortestPath> {

  protected final Map<Integer, Node> swNumToNode;
  @Getter
  protected final Map<Integer, Node> swNumToOriginalNode;
  protected final Map<Node, T> cachedShortestPaths = new HashMap<>();
  protected final Map<Node, List<Edge>> cachedPaths = new HashMap<>();

  protected final Class<T> clazz;
  protected final ShortestPathCreator<T> shortestPathCreator;

  public BaseShortestPathsTree(
      final @NonNull Map<Integer, Node> swNumToNode,
      final @NonNull Map<Integer, Node> swNumToOriginalNode, final @NonNull Class<T> clazz,
      final @NonNull ShortestPathCreator<T> shortestPathCreator) {
    this.swNumToNode = swNumToNode;
    this.swNumToOriginalNode = swNumToOriginalNode;
    this.clazz = clazz;
    this.shortestPathCreator = shortestPathCreator;
  }

  public long getCost(final int swNum) {
    return Objects.requireNonNull(swNumToNode.get(swNum)).getDistance();
  }

  protected abstract List<Edge> buildEdges(final Node node);

  public List<Edge> getPath(final int dstSwNum) {
    final Node dstNode = swNumToNode.get(dstSwNum);
    if (dstNode == null) {
      return emptyList();
    }
    return cachedPaths.computeIfAbsent(dstNode, this::buildEdges);
  }

  protected abstract T buildShortestPath(final Node node);

  @Nullable
  public T getShortestPath(final int dstSwNum) {
    final Node dstNode = swNumToNode.get(dstSwNum);
    return dstNode == null ? null : cachedShortestPaths
        .computeIfAbsent(dstNode, this::buildShortestPath);
  }
}
