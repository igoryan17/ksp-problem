package com.igoryan.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
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

  public Node(final @NonNull Node node, final @NonNull Node predecessor) {
    this.swNum = node.getSwNum();
    this.isTransit = node.isTransit;
    this.distance = node.getDistance();
    this.edgePredecessor = node.getEdgePredecessor();
    this.nodePredecessor = predecessor;
  }

  @Nullable
  public List<Edge> buildPath() {
    if (getNodePredecessor() == null) {
      return null;
    }
    final Deque<Edge> links = new ArrayDeque<>();
    Node temp = this;
    while (temp.getNodePredecessor() != null) {
      links.addFirst(temp.getEdgePredecessor());
      temp = temp.getNodePredecessor();
    }
    return new ArrayList<>(links);
  }

  @Nullable
  public ShortestPath buildShortestPath() {
    if (getNodePredecessor() == null) {
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
    return new ShortestPath(temp, this, new ArrayList<>(edges),
        new ArrayList<>(nodes), getDistance());
  }

  @Nullable
  public ShortestPath buildShortestPath(Map<Integer, Node> swNumToOriginalNode) {
    if (getNodePredecessor() == null) {
      return null;
    }
    final Deque<Edge> edges = new ArrayDeque<>();
    final Deque<Node> nodes = new ArrayDeque<>();
    Node temp = this;
    while (temp.getNodePredecessor() != null) {
      edges.addFirst(temp.getEdgePredecessor());
      nodes.addFirst(swNumToOriginalNode.get(temp.getSwNum()));
      temp = temp.getNodePredecessor();
    }
    nodes.addFirst(temp);
    return new ShortestPath(temp, this, new ArrayList<>(edges),
        new ArrayList<>(nodes), getDistance());
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
}
