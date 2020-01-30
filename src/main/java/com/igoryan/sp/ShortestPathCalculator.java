package com.igoryan.sp;

import com.google.common.graph.Network;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.ParallelEdges;
import com.igoryan.model.path.ShortestPath;
import com.igoryan.model.path.ShortestPathCreator;

public interface ShortestPathCalculator {

  void calculate(Node src, Node dst, Network<Node, ParallelEdges> network);

  <T extends ShortestPath> T calculate(Class<T> type, Node src, Node dst,
      Network<Node, ParallelEdges> network, ShortestPathCreator<T> shortestPathCreator);
}
