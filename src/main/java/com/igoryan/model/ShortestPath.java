package com.igoryan.model;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

import com.google.common.graph.EndpointPair;
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

  protected final transient List<EndpointPair<Node>> incidentNodesOfEdgeCache;
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
    this.incidentNodesOfEdgeCache = new ArrayList<>();
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
    this.incidentNodesOfEdgeCache = new ArrayList<>();
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
    this.incidentNodesOfEdgeCache = emptyList();
  }

  public EndpointPair<Node> getIncidentNodes(int i) {
    EndpointPair<Node> result =
        i < incidentNodesOfEdgeCache.size() ? incidentNodesOfEdgeCache.get(i) : null;
    if (result == null) {
      result = EndpointPair.ordered(nodes.get(i), nodes.get(i + 1));
      incidentNodesOfEdgeCache.add(i, result);
    }
    return result;
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

  public long getOriginalCost() {
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
