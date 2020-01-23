package com.igoryan.model;

import static java.util.Collections.emptyList;

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

  public List<Edge> getPath(final int dstSwNum) {
    final Node dstNode = swNumToNode.get(dstSwNum);
    if (dstNode == null) {
      return emptyList();
    }
    final List<Edge> result = dstNode.buildPath();
    if (result == null) {
      return emptyList();
    }
    return result;
  }

  @Nullable
  public ShortestPath getShortestPath(final int dstSwNum) {
    final Node dstNode = swNumToNode.get(dstSwNum);
    return dstNode == null ? null : dstNode.buildShortestPath(swNumToOriginalNode);
  }
}


