package com.igoryan.sp.impl;

import com.google.common.graph.Network;
import com.igoryan.model.Edge;
import com.igoryan.model.Node;
import com.igoryan.model.ParallelEdges;
import com.igoryan.model.ShortestPath;
import com.igoryan.model.ShortestPathsTree;
import com.igoryan.sp.ShortestPathCalculator;
import com.igoryan.sp.util.ShortestPathsUtil;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import javax.annotation.Nullable;

public abstract class BaseDijkstraShortestPathCalculator implements ShortestPathCalculator {

  protected static final Comparator<Node> COMPARE_NODES_BY_DISTANCE =
      Comparator.comparingLong(Node::getDistance);

  protected final Map<Integer, ShortestPathsTree> cachedShortestPathTrees = new HashMap<>();

  @Override
  public ShortestPathsTree calculateOfReversed(final Node src, final Node dst,
      final Network<Node, ParallelEdges> network, final boolean reversed) {
    calculate(src, dst, network);
    return ShortestPathsUtil.buildRecursively(src, network.nodes(), reversed);
  }

  @Override
  public List<Edge> calculatePath(final Node src, final Node dst,
      final Network<Node, ParallelEdges> network, final boolean cache) {
    if (cache) {
      return cachedShortestPathTrees.computeIfAbsent(src.getSwNum(),
          key -> calculateOfReversed(src, dst, network, false)).getPath(dst.getSwNum());
    }
    calculate(src, dst, network);
    return dst.buildPath(false);
  }

  @Override
  @Nullable
  public ShortestPath calculateShortestPath(final Node src, final Node dst,
      final Network<Node, ParallelEdges> network, boolean cache) {
    if (cache) {
      return cachedShortestPathTrees.computeIfAbsent(src.getSwNum(),
          key -> calculateOfReversed(src, dst, network, false))
          .getShortestPath(dst.getSwNum());
    }
    calculate(src, dst, network);
    return dst.buildShortestPath(false);
  }

  protected void init(Node src, final Network<Node, ParallelEdges> network) {
    network.nodes().forEach(node -> {
      node.setEdgePredecessor(null);
      node.setNodePredecessor(null);
      node.setDistance(Long.MAX_VALUE);
    });
    src.setDistance(0);
  }

  protected abstract void relaxation(Node u, Node v, ParallelEdges parallelEdges,
      PriorityQueue<Node> queue);

  @Override
  public void clear() {
    cachedShortestPathTrees.clear();
  }
}
