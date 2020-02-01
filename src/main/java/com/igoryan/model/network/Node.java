package com.igoryan.model.network;

import static java.util.Collections.emptyList;

import com.igoryan.model.path.ShortestPath;
import com.igoryan.model.path.ShortestPathCreator;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public final class Node {

  @Getter
  private final Integer swNum;
  @Getter
  @Setter
  private boolean isTransit;
  @Getter
  @Setter
  private boolean isVisited;
  @Getter
  @Setter
  private long distance;
  @Getter
  @Setter
  private Edge edgePredecessor;
  @Getter
  @Setter
  private Node nodePredecessor;

  public Node(final int swNum, final boolean isTransit) {
    this.swNum = swNum;
    this.isTransit = isTransit;
  }

  public Node(final @NonNull Node node, final @Nullable Node predecessor) {
    this.swNum = node.getSwNum();
    this.isTransit = node.isTransit;
    this.distance = node.getDistance();
    this.edgePredecessor = node.getEdgePredecessor();
    this.nodePredecessor = predecessor;
  }

  public List<Edge> buildPath() {
    if (nodePredecessor == null) {
      return emptyList();
    }
    final Deque<Edge> edges = new ArrayDeque<>();
    Node temp = this;
    while (temp.getNodePredecessor() != null) {
      edges.addFirst(temp.getEdgePredecessor());
      temp = temp.getNodePredecessor();
    }
    return new ArrayList<>(edges);
  }

  public List<Edge> buildReversed() {
    if (nodePredecessor == null) {
      return emptyList();
    }
    final List<Edge> edges = new ArrayList<>();
    Node temp = this;
    while (temp.getNodePredecessor() != null) {
      edges.add(temp.getEdgePredecessor());
      temp = temp.getNodePredecessor();
    }
    return edges;
  }

  @Nullable
  public <T extends ShortestPath> T buildShortestPath(Class<T> type,
      ShortestPathCreator<T> shortestPathCreator) {
    if (nodePredecessor == null) {
      return null;
    }
    final Deque<Edge> edges = new ArrayDeque<>();
    final Deque<Node> nodes = new ArrayDeque<>();
    Node temp = this;
    while (temp.getNodePredecessor() != null) {
      edges.addFirst(temp.getEdgePredecessor());
      nodes.addFirst(temp);
      temp = temp.getNodePredecessor();
    }
    nodes.addFirst(temp);
    return shortestPathCreator.create(temp, this, new ArrayList<>(edges), new ArrayList<>(nodes));
  }

  @Nullable
  public <T extends ShortestPath> T buildShortestPath(Class<T> type,
      ShortestPathCreator<T> shortestPathCreator, Map<Integer, Node> swNumToOriginalNode) {
    if (nodePredecessor == null) {
      return null;
    }
    final Deque<Edge> edges = new ArrayDeque<>();
    final Deque<Node> nodes = new ArrayDeque<>();
    Node temp = this;
    while (temp.getNodePredecessor() != null) {
      edges.addFirst(temp.getEdgePredecessor());
      nodes.addFirst(Objects.requireNonNull(swNumToOriginalNode.get(temp.getSwNum()),
          "there is not mapping to original node; swNum:" + temp.getSwNum()));
      temp = temp.getNodePredecessor();
    }
    final Node src = Objects.requireNonNull(swNumToOriginalNode.get(temp.getSwNum()));
    nodes.addFirst(src);
    return shortestPathCreator.create(src, Objects.requireNonNull(swNumToOriginalNode.get(swNum)),
        new ArrayList<>(edges), new ArrayList<>(nodes));
  }

  @Nullable
  public <T extends ShortestPath> T buildReversedShortestPath(Class<T> type,
      ShortestPathCreator<T> shortestPathCreator, Map<Integer, Node> swNumToOriginalNode) {
    if (nodePredecessor == null) {
      return null;
    }
    final List<Edge> edges = new ArrayList<>();
    final List<Node> nodes = new ArrayList<>();
    Node temp = this;
    while (temp.getNodePredecessor() != null) {
      edges.add(temp.getEdgePredecessor());
      nodes.add(Objects.requireNonNull(swNumToOriginalNode.get(temp.getSwNum())));
      temp = temp.getNodePredecessor();
    }
    final Node dst = Objects.requireNonNull(swNumToOriginalNode.get(temp.getSwNum()));
    nodes.add(dst);
    return shortestPathCreator
        .create(Objects.requireNonNull(swNumToOriginalNode.get(swNum)), dst, edges, nodes);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Node node = (Node) o;

    return swNum.equals(node.swNum);
  }

  @Override
  public int hashCode() {
    return swNum;
  }

  @Override
  public String toString() {
    return "Node{" +
        "swNum=" + swNum +
        ", isTransit=" + isTransit +
        ", isVisited=" + isVisited +
        ", distance=" + distance +
        ", edgePredecessor=" + edgePredecessor +
        '}';
  }
}
