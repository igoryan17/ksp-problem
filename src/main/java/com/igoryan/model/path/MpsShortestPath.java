package com.igoryan.model.path;

import static java.util.Collections.singletonMap;

import com.igoryan.model.network.Edge;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.NodeEdgeTuple;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;

public class MpsShortestPath extends ShortestPath {

  @Getter
  private final Map<Node, Integer> nodeToIndex;
  private final transient NodeEdgeTuple[] keys;

  private transient Set<Integer> uniqueVertexes;
  private transient long originalCost;
  private transient int hash;

  public MpsShortestPath(final @NonNull Node src,
      final @NonNull Node dst,
      final @NonNull List<Edge> edges,
      final @NonNull List<Node> nodes) {
    super(src, dst, edges, nodes, edges.stream().mapToLong(Edge::getReducedCost).sum(),
        Edge::getReducedCost);
    this.keys = new NodeEdgeTuple[edges.size()];
    this.nodeToIndex = new HashMap<>(nodes.size());
    for (int i = 0; i < nodes.size(); i++) {
      nodeToIndex.put(nodes.get(i), i);
    }
  }

  public MpsShortestPath(final @NonNull Node src,
      final @NonNull Node dst,
      final @NonNull List<Edge> edges,
      final @NonNull List<Node> nodes,
      final long cost) {
    super(src, dst, edges, nodes, cost, Edge::getReducedCost);
    this.keys = new NodeEdgeTuple[edges.size()];
    this.nodeToIndex = new HashMap<>(nodes.size());
    for (int i = 0; i < nodes.size(); i++) {
      nodeToIndex.put(nodes.get(i), i);
    }
  }

  public MpsShortestPath(final @NonNull Node src,
      final @NonNull Node dst,
      final @NonNull List<Edge> edges,
      final @NonNull List<Node> nodes,
      final long reducedCost,
      final @NonNull NodeEdgeTuple[] keys) {
    super(src, dst, edges, nodes, reducedCost, Edge::getReducedCost);
    this.keys = keys;
    this.nodeToIndex = new HashMap<>(nodes.size());
    for (int i = 0; i < nodes.size(); i++) {
      nodeToIndex.put(nodes.get(i), i);
    }
  }

  public MpsShortestPath(final @NonNull Node src,
      final @NonNull Node dst,
      final @NonNull List<Edge> edges,
      final @NonNull List<Node> nodes,
      final long reducedCost,
      final @NonNull long[] edgesCostCache,
      final @NonNull NodeEdgeTuple[] keys) {
    super(src, dst, edges, nodes, reducedCost, Edge::getReducedCost, edgesCostCache);
    this.keys = keys;
    this.nodeToIndex = new HashMap<>(nodes.size());
    for (int i = 0; i < nodes.size(); i++) {
      nodeToIndex.put(nodes.get(i), i);
    }
  }

  public MpsShortestPath(final @NonNull Node srcAndDst) {
    super(srcAndDst, Edge::getReducedCost);
    this.keys = null;
    this.nodeToIndex = singletonMap(srcAndDst, 0);
  }

  public boolean withoutLoops() {
    if (uniqueVertexes == null) {
      uniqueVertexes = nodes.stream().map(Node::getSwNum).collect(Collectors.toSet());
    }
    return uniqueVertexes.size() == nodes.size();
  }

  public boolean formsCycle(final @NonNull Edge appendedEdge) {
    if (uniqueVertexes == null) {
      uniqueVertexes = nodes.stream().map(Node::getSwNum).collect(Collectors.toSet());
    }
    return uniqueVertexes.contains(appendedEdge.getDstSwNum());
  }

  @Override
  public long getOriginalCost() {
    long result = originalCost;
    if (result == 0 && !edges.isEmpty()) {
      result = edges.stream().mapToLong(Edge::getCost).sum();
      originalCost = result;
    }
    return result;
  }

  public int getIndex(final @NonNull Node node) {
    return Objects.requireNonNull(nodeToIndex.get(node));
  }

  public NodeEdgeTuple getKey(int nodeIndex) {
    assert nodeIndex >= 0;
    assert nodeIndex <= edges.size();
    NodeEdgeTuple result = keys[nodeIndex];
    if (result == null) {
      result = new NodeEdgeTuple(nodes.get(nodeIndex), edges.get(nodeIndex));
      keys[nodeIndex] = result;
    }
    return result;
  }

  public MpsShortestPath subPath(int i) {
    assert i >= 0;
    assert i < nodes.size();
    if (i == 0) {
      return new MpsShortestPath(src);
    }
    return new MpsShortestPath(this.src, this.nodes.get(i), this.edges.subList(0, i),
        this.nodes.subList(0, i + 1), getEdgesCost(i),
        Arrays.copyOfRange(edgesCostCache, 0, i), Arrays.copyOfRange(keys, 0, i));
  }

  public MpsShortestPath append(final @NonNull MpsShortestPath shortestPath) {
    assert this.dst == shortestPath.getSrc();
    final int edgesCount = edges.size() + shortestPath.edges.size();
    final List<Edge> links = new ArrayList<>(edgesCount);
    links.addAll(this.edges);
    links.addAll(shortestPath.edges);
    final List<Node> vertexes = new ArrayList<>(edgesCount + 1);
    vertexes.addAll(this.nodes);
    vertexes.addAll(shortestPath.nodes.subList(1, shortestPath.nodes.size()));
    final NodeEdgeTuple keys[] = new NodeEdgeTuple[edgesCount];
    if (this.keys == null) {
      System.arraycopy(shortestPath.keys, 0, keys, 0, shortestPath.keys.length);
    } else {
      System.arraycopy(this.keys, 0, keys, 0, this.keys.length);
      System.arraycopy(shortestPath.keys, 0, keys, this.keys.length, shortestPath.keys.length);
    }
    return new MpsShortestPath(src, dst, edges, nodes, this.cost + shortestPath.cost, keys);
  }

  public ShortestPath append(final @NonNull Edge edge, final @NonNull Node headOfEdge) {
    assert this.dst.getSwNum() == edge.getSrcSwNum();
    final List<Edge> links = new ArrayList<>(edges.size() + 1);
    links.addAll(this.edges);
    links.add(edge);
    final List<Node> vertexes = new ArrayList<>(this.nodes.size() + 1);
    vertexes.addAll(this.nodes);
    vertexes.add(headOfEdge);
    return new ShortestPath(this.src, headOfEdge, links, vertexes, this.cost + edge.getCost(),
        Edge::getReducedCost);
  }

  @Override
  public int hashCode() {
    int h = hash;
    if (h == 0 && !edges.isEmpty()) {
      h = edges.hashCode();
      hash = h;
    }
    return h;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Node src;
    private Node dst;
    private List<Edge> edges;
    private List<Node> nodes;
    private long cost;

    public Builder from(final @NonNull MpsShortestPath shortestPath) {
      this.src = shortestPath.getSrc();
      this.dst = shortestPath.getDst();
      this.edges = new ArrayList<>(shortestPath.getEdges());
      this.nodes = new ArrayList<>(shortestPath.getNodes());
      this.cost = shortestPath.getCost();
      return this;
    }

    public Builder append(final @NonNull Edge edge, final @NonNull Node headOfEdge) {
      assert edge.getSrcSwNum() == dst.getSwNum();
      this.dst = headOfEdge;
      this.edges.add(edge);
      this.nodes.add(headOfEdge);
      this.cost += edge.getReducedCost();
      return this;
    }

    public Builder append(final @NonNull List<Edge> edges,
        final @NonNull Map<Integer, Node> swNumToOriginalNode) {
      assert !edges.isEmpty();
      assert edges.get(0).getSrcSwNum() == dst.getSwNum();
      this.dst = Objects
          .requireNonNull(swNumToOriginalNode.get(edges.get(edges.size() - 1).getDstSwNum()));
      this.edges.addAll(edges);
      edges.forEach(edge -> this.nodes
          .add(Objects.requireNonNull(swNumToOriginalNode.get(edge.getDstSwNum()))));
      this.cost += edges.stream().mapToLong(Edge::getReducedCost).sum();
      return this;
    }

    public Builder append(final @NonNull MpsShortestPath shortestPath) {
      assert this.dst == shortestPath.getSrc();
      if (shortestPath.getEdges().isEmpty()) {
        return this;
      }
      this.dst = shortestPath.getDst();
      edges.addAll(shortestPath.getEdges());
      nodes.addAll(shortestPath.getNodes().subList(1, shortestPath.getNodes().size()));
      this.cost += shortestPath.getCost();
      return this;
    }

    public MpsShortestPath build() {
      return new MpsShortestPath(src, dst, edges, nodes, cost);
    }
  }

  @Override
  public String toString() {
    return "MpsShortestPath{" +
        "src=" + src +
        ", dst=" + dst +
        ", edges=" + edges +
        ", nodes=" + nodes +
        '}';
  }
}
