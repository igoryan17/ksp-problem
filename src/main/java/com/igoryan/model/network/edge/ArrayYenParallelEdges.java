package com.igoryan.model.network.edge;

import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "com.igoryan.model")
@EqualsAndHashCode(callSuper = true)
public final class ArrayYenParallelEdges extends ArrayParallelEdges {

  public ArrayYenParallelEdges(final int srcSwNum, final int dstSwNum,
      final @NonNull List<Edge> edges) {
    super(srcSwNum, dstSwNum, edges);
  }

  @Override
  public void markAsUsed(final @NonNull Edge edge) {
    if (firstUnusedIndex >= edges.size()) {
      // all edges are used
      return;
    }
    final Edge freeEdge = edges.get(firstUnusedIndex);
    if (edge.getCost() < freeEdge.getCost()) {
      // already used
      return;
    }
    if (edge.getCost() > freeEdge.getCost()) {
      // already marked
      throw new IllegalArgumentException(
          "next free edge is not given edge;" + " freeEdge: " + freeEdge + ", edge: " + edge);
    }
    if (freeEdge.equals(edge)) {
      firstUnusedIndex++;
      return;
    }
    // check edges with equal costs
    int indexOfRelatedEdge = -1;
    log.trace("equal costs case; firstFreeEdge: {}, edge: {}, edges: {}", freeEdge, edge, edges);
    // check if previously used
    for (int i = firstUnusedIndex - 1; i >= 0; i--) {
      final Edge previousFreeEdge = edges.get(i);
      if (previousFreeEdge.getCost() < edge.getCost()) {
        break;
      }
      if (previousFreeEdge.equals(edge)) {
        // already used
        return;
      }
    }
    // check if exist in forward
    for (int i = firstUnusedIndex + 1; i < edges.size(); i++) {
      final Edge nextFreeEdge = edges.get(i);
      if (nextFreeEdge.getCost() > edge.getCost()) {
        break;
      }
      if (nextFreeEdge.equals(edge)) {
        indexOfRelatedEdge = i;
        break;
      }
    }
    if (indexOfRelatedEdge < 0) {
      throw new IllegalArgumentException(
          "next free edges is not given edge;" + " freeEdge: " + freeEdge + ", edge: " + edge);
    } else {
      Collections.swap(edges, firstUnusedIndex, indexOfRelatedEdge);
      firstUnusedIndex++;
    }
  }
}
