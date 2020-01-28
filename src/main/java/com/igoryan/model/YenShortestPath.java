package com.igoryan.model;

import java.util.Arrays;
import java.util.List;
import lombok.NonNull;

public class YenShortestPath extends ShortestPath {

  private final transient int[] edgesHashes;
  private transient int hash;

  public YenShortestPath(final @NonNull Node src,
      final @NonNull Node dst,
      final @NonNull List<Edge> edges,
      final @NonNull List<Node> nodes,
      final long cost
  ) {
    super(src, dst, edges, nodes, cost, Edge::getCost);
    this.edgesHashes = new int[edges.size()];
    this.edgesHashes[0] = edges.get(0).hashCode();
  }

  public YenShortestPath(final @NonNull Node src,
      final @NonNull Node dst,
      final @NonNull List<Edge> edges,
      final @NonNull List<Node> nodes,
      final long cost,
      final @NonNull int[] edgesHashes,
      final @NonNull long[] edgesCostCache
  ) {
    super(src, dst, edges, nodes, cost, Edge::getCost, edgesCostCache);
    this.edgesHashes = edgesHashes;
  }

  public YenShortestPath(final @NonNull Node srcAndDst) {
    super(srcAndDst, Edge::getCost);
    this.edgesHashes = new int[]{0};
  }

  public boolean containsSubPath(final @NonNull YenShortestPath shortestPath) {
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

  public YenShortestPath subPath(int i) {
    assert i >= 0;
    assert i < nodes.size();
    if (i == 0) {
      return new YenShortestPath(src);
    }
    return new YenShortestPath(this.src, this.nodes.get(i), this.edges.subList(0, i),
        this.nodes.subList(0, i + 1), getEdgesCost(i),
        Arrays.copyOfRange(edgesHashes, 0, i), Arrays.copyOfRange(edgesCostCache, 0, i));
  }

  public int getEdgesHash(int edgesCount) {
    assert edgesCount >= 0;
    assert edgesCount <= edges.size();
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
}
