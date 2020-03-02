package com.igoryan.model.network.edge;

import java.util.Comparator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public final class Edge {

  public static final Comparator<Edge> COMPARE_EDGES_BY_COST =
      Comparator.comparingLong(Edge::getCost);
  public static final Comparator<Edge> COMPARE_EDGES_BY_COST_AND_PORTS = Comparator
      .comparingLong(Edge::getCost)
      .thenComparing(Edge::getSrcPort, Short::compareTo)
      .thenComparing(Edge::getDstPort, Short::compareTo);

  @Getter
  private final int srcSwNum;
  @Getter
  private final short srcPort;
  @Getter
  private final int dstSwNum;
  @Getter
  private final short dstPort;
  @Getter
  private final long cost;
  @Getter
  @Setter
  private long reducedCost;
  @Getter
  @Setter
  private boolean isUsed;
  private transient int hash;

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Edge edge = (Edge) o;

    if (srcSwNum != edge.srcSwNum) {
      return false;
    }
    if (dstSwNum != edge.dstSwNum) {
      return false;
    }
    if (srcPort != edge.srcPort) {
      return false;
    }
    if (dstPort != edge.dstPort) {
      return false;
    }
    return cost == edge.cost;
  }

  @Override
  public int hashCode() {
    int h = hash;
    if (hash == 0) {
      h = srcSwNum;
      h = 31 * h + dstSwNum;
      h = 31 * h + (int) srcPort;
      h = 31 * h + (int) dstPort;
      h = 31 * h + (int) (cost ^ (cost >>> 32));
      hash = h;
    }
    return h;
  }

  @Override
  public String toString() {
    return "Edge{" +
        "srcSwNum=" + srcSwNum +
        ", dstSwNum=" + dstSwNum +
        ", srcPort=" + srcPort +
        ", dstPort=" + dstPort +
        ", cost=" + cost +
        '}';
  }
}
