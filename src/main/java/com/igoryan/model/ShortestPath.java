package com.igoryan.model;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;

public class ShortestPath {
  @Getter
  protected final Node src;
  @Getter
  protected final Node dst;
  @Getter
  protected final List<Edge> edges;
  @Getter
  protected final List<Node> nodes;
  @Getter
  protected final long cost;
  protected final EdgeCostRetriever edgeCostRetriever;

  protected final transient long[] edgesCostCache;

  public ShortestPath(final @NonNull Node src, final @NonNull Node dst,
      final @NonNull List<Edge> edges, final @NonNull List<Node> nodes, final long cost,
      final @NonNull EdgeCostRetriever edgeCostRetriever) {
    this.edgeCostRetriever = edgeCostRetriever;
    assert !edges.isEmpty();
    assert !nodes.isEmpty();
    assert nodes.get(0) == src;
    assert nodes.get(nodes.size() - 1) == dst;
    assert edges.get(0).getSrcSwNum() == src.getSwNum();
    assert edges.get(edges.size() - 1).getDstSwNum() == dst.getSwNum();
    assert nodes.size() == edges.size() + 1;
    this.src = src;
    this.dst = dst;
    this.edges = unmodifiableList(edges);
    this.nodes = unmodifiableList(nodes);
    this.cost = cost;
    this.edgesCostCache = new long[edges.size()];
    this.edgesCostCache[0] = edges.get(0).getCost();
  }

  public ShortestPath(final @NonNull Node src, final @NonNull Node dst,
      final @NonNull List<Edge> edges, final @NonNull List<Node> nodes, final long cost,
      final @NonNull EdgeCostRetriever edgeCostRetriever,
      final @NonNull long[] edgesCostCache) {
    this.edgeCostRetriever = edgeCostRetriever;
    assert !edges.isEmpty();
    assert !nodes.isEmpty();
    assert nodes.get(0) == src;
    assert nodes.get(nodes.size() - 1) == dst;
    assert edges.get(0).getSrcSwNum() == src.getSwNum();
    assert edges.get(edges.size() - 1).getDstSwNum() == dst.getSwNum();
    assert nodes.size() == edges.size() + 1;
    this.src = src;
    this.dst = dst;
    this.edges = unmodifiableList(edges);
    this.nodes = unmodifiableList(nodes);
    this.cost = cost;
    this.edgesCostCache = edgesCostCache;
  }

  public ShortestPath(final @NonNull Node srcAndDst,
      final EdgeCostRetriever edgeCostRetriever) {
    this.src = srcAndDst;
    this.dst = srcAndDst;
    this.edgeCostRetriever = edgeCostRetriever;
    this.edges = emptyList();
    this.nodes = singletonList(srcAndDst);
    this.edgesCostCache = new long[0];
    this.cost = 0;
  }

  public ShortestPath append(final @NonNull ShortestPath shortestPath) {
    final List<Edge> links = new ArrayList<>(edges.size() + shortestPath.edges.size());
    links.addAll(this.edges);
    links.addAll(shortestPath.edges);
    final List<Node> vertexes =
        new ArrayList<>(this.nodes.size() + shortestPath.nodes.size() - 1);
    vertexes.addAll(this.nodes);
    vertexes.addAll(shortestPath.nodes.subList(1, shortestPath.nodes.size()));
    return new ShortestPath(this.src, shortestPath.dst, links, vertexes,
        cost, edgeCostRetriever);
  }

  public long getEdgesCost(int edgesCount) {
    assert edgesCount >= 0;
    assert edgesCount <= edges.size();
    if (edgesCount == 0) {
      return 0;
    }
    long cost = edgesCostCache[edgesCount - 1];
    if (cost == 0) {
      int lastCalculatedCost = 0;
      for (int i = edgesCount - 1; i >= 0; i--) {
        if (edgesCostCache[i] == 0) {
          continue;
        }
        lastCalculatedCost = i;
        break;
      }
      for (int i = lastCalculatedCost + 1; i < edgesCount; i++) {
        edgesCostCache[i] = edgesCostCache[i - 1] + edgeCostRetriever.getCost(edges.get(i));
      }
      cost = edgesCostCache[edgesCount - 1];
    }
    return cost;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ShortestPath that = (ShortestPath) o;

    if (cost != that.cost) {
      return false;
    }
    if (!src.equals(that.src)) {
      return false;
    }
    if (!dst.equals(that.dst)) {
      return false;
    }
    return edges.equals(that.edges);
  }
}
