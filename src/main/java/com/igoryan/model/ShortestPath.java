package com.igoryan.model;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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
  private final List<Node> nodes;
  @Getter
  private final Map<Node, Integer> nodeToIndex;
  @Getter
  private final long cost;
  @Getter
  private final long reducedCost;
  private final transient int[] edgesHashes;
  private final transient long[] edgesCostCache;
  private final transient List<NodeEdgeTuple> keys;

  private transient Set<Integer> uniqueVertexes;
  private transient int hash;

  public ShortestPath(final @NonNull Node src, final @NonNull Node dst,
      final @NonNull List<Edge> edges, final @NonNull List<Node> nodes, final long cost) {
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
    this.reducedCost = edges.stream().mapToLong(Edge::getReducedCost).sum();
    this.edgesHashes = new int[edges.size()];
    this.edgesHashes[0] = edges.get(0).hashCode();
    this.edgesCostCache = new long[edges.size()];
    this.edgesCostCache[0] = edges.get(0).getCost();
    this.keys = new ArrayList<>(edges.size());
    this.nodeToIndex = new HashMap<>(nodes.size());
    for (int i = 0; i < nodes.size(); i++) {
      nodeToIndex.put(nodes.get(i), i);
    }
  }

  public ShortestPath(final @NonNull Node srcAndDst) {
    this.src = srcAndDst;
    this.dst = srcAndDst;
    this.edges = emptyList();
    this.nodes = singletonList(srcAndDst);
    this.nodeToIndex = singletonMap(srcAndDst, srcAndDst.getSwNum());
    this.cost = 0;
    this.reducedCost = 0;
    this.edgesHashes = new int[]{0};
    this.edgesCostCache = new long[]{0};
    this.keys = emptyList();
  }

  public ShortestPath(final @NonNull Node src, final @NonNull Node dst,
      final @NonNull List<Edge> edges, final @NonNull List<Node> nodes, final long cost,
      final long reducedCost) {
    assert !edges.isEmpty();
    this.src = src;
    this.dst = dst;
    this.edges = unmodifiableList(edges);
    this.nodes = unmodifiableList(nodes);
    this.cost = cost;
    this.reducedCost = reducedCost;
    this.edgesHashes = new int[edges.size()];
    this.edgesHashes[0] = edges.get(0).hashCode();
    this.edgesCostCache = new long[edges.size()];
    this.edgesCostCache[0] = edges.get(0).getCost();
    this.keys = new ArrayList<>(edges.size());
    this.nodeToIndex = new HashMap<>(nodes.size());
    for (int i = 0; i < nodes.size(); i++) {
      nodeToIndex.put(nodes.get(i), i);
    }
  }

  public ShortestPath(final @NonNull Node src, final @NonNull Node dst,
      final @NonNull List<Edge> edges, final @NonNull List<Node> nodes, final long cost,
      final int[] edgesHashes, final long[] edgesCostCache, final List<NodeEdgeTuple> keys) {
    this.src = src;
    this.dst = dst;
    this.edges = edges;
    this.nodes = nodes;
    this.cost = cost;
    this.reducedCost = edges.stream().mapToLong(Edge::getReducedCost).sum();
    this.edgesHashes = edgesHashes;
    this.edgesCostCache = edgesCostCache;
    this.keys = keys;
    this.nodeToIndex =
        nodes.stream().collect(Collectors.toMap(Function.identity(), Node::getSwNum));
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
        cost + shortestPath.cost);
  }

  public ShortestPath append(final @NonNull Edge edge, final @NonNull Node headOfEdge) {
    final List<Edge> links = new ArrayList<>(edges.size() + 1);
    links.addAll(this.edges);
    links.add(edge);
    final List<Node> vertexes = new ArrayList<>(this.nodes.size() + 1);
    vertexes.addAll(this.nodes);
    vertexes.add(headOfEdge);
    return new ShortestPath(this.src, headOfEdge, links, vertexes, this.cost + edge.getCost(),
        this.reducedCost + edge.getReducedCost());
  }

  public ShortestPath subPath(int i) {
    assert i >= 0;
    assert i < nodes.size();
    if (i == 0) {
      return new ShortestPath(src);
    }
    return new ShortestPath(this.src, this.nodes.get(i), this.edges.subList(0, i),
        this.nodes.subList(0, i + 1), getEdgesCost(i),
        Arrays.copyOfRange(edgesHashes, 0, i), Arrays.copyOfRange(edgesCostCache, 0, i),
        this.keys.subList(0, Math.min(i, keys.size())));
  }

  public boolean containsSubPath(final @NonNull ShortestPath shortestPath) {
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

  public int getIndex(final @NonNull Node node) {
    return Objects.requireNonNull(nodeToIndex.get(node));
  }

  public NodeEdgeTuple getKey(int nodeIndex) {
    assert nodeIndex >= 0;
    NodeEdgeTuple result = nodeIndex < keys.size() ? keys.get(nodeIndex) : null;
    if (result == null) {
      result = new NodeEdgeTuple(nodes.get(nodeIndex), edges.get(nodeIndex));
      keys.add(nodeIndex, result);
    }
    return result;
  }

  public boolean hasCycles() {
    if (uniqueVertexes == null) {
      uniqueVertexes = nodes.stream().map(Node::getSwNum).collect(Collectors.toSet());
    }
    return uniqueVertexes.size() != nodes.size();
  }

  public boolean formsCycle(final @NonNull Edge appendedEdge) {
    if (uniqueVertexes == null) {
      uniqueVertexes = nodes.stream().map(Node::getSwNum).collect(Collectors.toSet());
    }
    return uniqueVertexes.contains(appendedEdge.getDstSwNum());
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
        edgesCostCache[i] = edgesCostCache[i - 1] + edges.get(i).getCost();
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
