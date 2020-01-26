package com.igoryan.sp;

import com.google.common.graph.Network;
import com.igoryan.model.Edge;
import com.igoryan.model.Node;
import com.igoryan.model.ParallelEdges;
import com.igoryan.model.ShortestPath;
import com.igoryan.model.ShortestPathsTree;
import java.util.List;

public interface ShortestPathCalculator {

  void calculate(Node src, Node dst, Network<Node, ParallelEdges> network);

  ShortestPathsTree calculateOfReversed(Node src, Node dst,
      Network<Node, ParallelEdges> network, final boolean reversed);

  ShortestPath calculateShortestPath(Node src, Node dst, Network<Node, ParallelEdges> network,
      boolean cache);

  List<Edge> calculatePath(Node src, Node dst, Network<Node, ParallelEdges> network,
      final boolean cache);

  void clear();
}
