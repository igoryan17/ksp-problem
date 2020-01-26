package com.igoryan.model;

import static java.util.Collections.emptyList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(of = "src")
public class ShortestPathsTree {

  private final int src;
  private final Map<Integer, Node> swNumToNode;
  private final Map<Integer, Node> swNumToOriginalNode;
  private final Map<Node, ShortestPath> cachedShortestPaths = new HashMap<>();
  private final Map<Node, List<Edge>> cachedPaths = new HashMap<>();
  private final boolean reversed;

  public long getCost(final int dstSwNum) {
    final Node dstNode = swNumToNode.get(dstSwNum);
    if (dstNode == null) {
      return Long.MAX_VALUE;
    }
    return dstNode.getDistance();
  }

  public List<Edge> getPath(final int dstSwNum) {
    final Node dstNode = swNumToNode.get(dstSwNum);
    if (dstNode == null) {
      return emptyList();
    }
    return cachedPaths.computeIfAbsent(dstNode, key -> dstNode.buildPath(reversed));
  }

  @Nullable
  public ShortestPath getShortestPath(final int dstSwNum) {
    final Node dstNode = swNumToNode.get(dstSwNum);
    return dstNode == null
        ? null
        : cachedShortestPaths
            .computeIfAbsent(dstNode, key -> key.buildShortestPath(swNumToOriginalNode, reversed));
  }

  @Nullable
  public ShortestPath getShortestPathWithReducedCost(final int dstSwNum) {
    final Node dstNode = swNumToNode.get(dstSwNum);
    return dstNode == null
        ? null
        : cachedShortestPaths
            .computeIfAbsent(dstNode, key -> key.buildShortestPathWithReducedCost(swNumToOriginalNode, reversed));
  }
}


