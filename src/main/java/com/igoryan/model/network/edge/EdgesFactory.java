package com.igoryan.model.network.edge;

import static com.igoryan.model.network.edge.Edge.COMPARE_EDGES_BY_COST;

import com.igoryan.model.Algorithms;
import java.util.List;
import lombok.NonNull;

public class EdgesFactory {

  public static ParallelEdges create(final int srcSwNum, final int dstSwNum,
      final @NonNull List<Edge> edges, final @NonNull Algorithms algorithm) {
    edges.sort(COMPARE_EDGES_BY_COST);
    switch (algorithm) {
      case DISJOINT:
        return new ArrayDisjointParallelEdges(srcSwNum, dstSwNum, edges);
      case MPS:
        return new ArrayYenParallelEdges(srcSwNum, dstSwNum, edges);
      case NAIVE:
      case YEN:
        int maxEqualCostEdgesCount = 0;
        int currentMaxCount = 0;
        for (int i = 0; i < edges.size() - 1; i++) {
          final Edge edge = edges.get(0);
          final Edge nextEdge = edges.get(i + 1);
          if (edge.equals(nextEdge)) {
            currentMaxCount++;
          } else {
            if (currentMaxCount > maxEqualCostEdgesCount) {
              maxEqualCostEdgesCount = currentMaxCount;
            }
            currentMaxCount = 0;
          }
        }
        if (currentMaxCount > maxEqualCostEdgesCount) {
          maxEqualCostEdgesCount = currentMaxCount;
        }
        if (maxEqualCostEdgesCount > 0) {
          maxEqualCostEdgesCount++;
        }
        if (maxEqualCostEdgesCount < edges.size() / 2) {
          return new ArrayYenParallelEdges(srcSwNum, dstSwNum, edges);
        } else {
          return new QueueYenParallelEdges(srcSwNum, dstSwNum, edges);
        }
      default:
        throw new IllegalArgumentException("unknown algorithm; algorithm: " + algorithm);
    }
  }
}
