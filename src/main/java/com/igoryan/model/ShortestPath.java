package com.igoryan.model;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;

public final class ShortestPath {
  @Getter
  private final Node src;
  @Getter
  private final Node dst;
  @Getter
  private final List<Edge> edges;
  @Getter
  private final List<Node> vertexes;
  @Getter
  private final long cost;
  private final int[] edgesHashes;
  private final long[] edgesCostCache;

  private int hash;

  public ShortestPath(final Node src, final Node dst,
      final Edge edge) {
    this.src = src;
    this.dst = dst;
    this.edges = singletonList(edge);
    this.cost = edge.getCost();
    this.vertexes = emptyList();
    edgesHashes = new int[]{};
    edgesCostCache = new long[]{};
  }

  public ShortestPath(final @NonNull Node src, final @NonNull Node dst,
      final @NonNull List<Edge> edges, @NonNull final List<Node> vertexes,
      @NonNull final long cost) {
    this.src = src;
    this.dst = dst;
    this.edges = unmodifiableList(edges);
    this.vertexes = unmodifiableList(vertexes);
    this.cost = cost;
    if (edges.isEmpty()) {
      this.edgesHashes = new int[]{0};
      this.edgesCostCache = new long[]{0};
      return;
    }
    this.edgesHashes = new int[edges.size()];
    this.edgesHashes[0] = edges.get(0).hashCode();
    this.edgesCostCache = new long[edges.size()];
    this.edgesCostCache[0] = edges.get(0).getCost();
  }

  public ShortestPath(final Node src, final Node dst,
      final List<Edge> edges, final List<Node> vertexes, final long cost,
      final int[] edgesHashes,
      final long[] edgesCostCache) {
    this.src = src;
    this.dst = dst;
    this.edges = edges;
    this.vertexes = vertexes;
    this.cost = cost;
    this.edgesHashes = edgesHashes;
    this.edgesCostCache = edgesCostCache;
  }

  public int getEdgesHash(int edgesCount) {
    if (edgesCount == 0) {
      return 0;
    }
    int h = edgesHashes[edgesCount - 1];
    if (h == 0) {
      int lastCalculatedHash = 0;
      for (int i = edgesCount - 1; i >= 0; i--) {
        if (edgesHashes[i] == 0) {
          continue;
        }
        lastCalculatedHash = i;
        break;
      }
      for (int i = lastCalculatedHash + 1; i < edgesCount; i++) {
        edgesHashes[i] = 31 * edgesHashes[i - 1] + edges.get(i).hashCode();
      }
      h = edgesHashes[edgesCount - 1];
    }
    return h;
  }

  public int getAllEdgesHash() {
    if (edges.isEmpty()) {
      return 0;
    }
    return getEdgesHash(edges.size());
  }

  public long getEdgesCost(int edgesCount) {
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
        edgesCostCache[i] = edgesCostCache[i - 1] + edges.get(i).getCost();
      }
      cost = edgesCostCache[edgesCount - 1];
    }
    return cost;
  }

  public ShortestPath subPath(int i) {
    final List<Edge> edges = this.edges.subList(0, i);
    final List<Node> vertexes = this.vertexes.subList(0, i + 1);
    if (i > 0) {
      return new ShortestPath(this.src, vertexes.get(i), edges, vertexes, getEdgesCost(i),
          Arrays.copyOfRange(edgesHashes, 0, i), Arrays.copyOfRange(edgesCostCache, 0, i));
    }
    return new ShortestPath(this.src, vertexes.get(i), edges, vertexes, getEdgesCost(i));
  }

  public ShortestPath append(@NonNull ShortestPath shortestPath) {
    final List<Edge> links = new ArrayList<>(edges.size() + shortestPath.edges.size());
    links.addAll(this.edges);
    links.addAll(shortestPath.edges);
    final List<Node> vertexes =
        new ArrayList<>(this.vertexes.size() + shortestPath.vertexes.size() - 1);
    vertexes.addAll(this.vertexes);
    vertexes.addAll(shortestPath.vertexes.subList(1, shortestPath.vertexes.size()));
    return new ShortestPath(this.src, shortestPath.dst, links, vertexes,
        cost + shortestPath.cost);
  }

  public boolean containsSubPath(@NonNull ShortestPath shortestPath) {
    if (edges.size() < shortestPath.edges.size()) {
      return false;
    }
    if (cost < shortestPath.cost) {
      return false;
    }
    if (shortestPath.getAllEdgesHash() != getEdgesHash(shortestPath.edges.size())) {
      return false;
    }
    for (int i = 0; i < shortestPath.edges.size(); i++) {
      if (!edges.get(i).equals(shortestPath.edges.get(i))) {
        return false;
      }
    }
    return true;
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

  @Override
  public int hashCode() {
    int h = hash;
    if (h == 0) {
      h = src.hashCode();
      h = 31 * h + dst.hashCode();
      h = 31 * h + (int) (cost ^ (cost >>> 32));
      h = 31 * h + getAllEdgesHash();
      hash = h;
    }
    return h;
  }

  @Override
  public String toString() {
    return "ShortestPath{"
        + "src=" + src
        + ", dst=" + dst
        + ", edges=" + edges
        + ", cost=" + cost
        + '}';
  }
}
