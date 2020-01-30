package com.igoryan.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public final class NodeEdgeTuple {
  @Getter
  private final Node node;
  @Getter
  private final Edge edge;
  @Getter
  private final Map<Integer, NodeEdgeTuple> pathNumToNextKey = new HashMap<>();
  @Getter
  @Setter
  private Map<NodeEdgeTuple, NodeEdgeTuple> children;
  private transient int hash;

  public NodeEdgeTuple(@NonNull final Node node, @NonNull final Edge edge) {
    this.node = node;
    this.edge = edge;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final NodeEdgeTuple that = (NodeEdgeTuple) o;

    if (!node.equals(that.node)) {
      return false;
    }
    return edge.equals(that.edge);
  }

  @Override
  public int hashCode() {
    int h = hash;
    if (h == 0) {
      h = node.hashCode();
      h = 31 * h + edge.hashCode();
      hash = h;
    }
    return h;
  }
}
