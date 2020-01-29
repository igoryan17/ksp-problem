package com.igoryan.sp;

import com.google.common.graph.Network;
import com.igoryan.model.Node;
import com.igoryan.model.ParallelEdges;
import com.igoryan.model.ShortestPath;
import com.igoryan.model.ShortestPathCreator;

public interface ShortestPathCalculator {

  void calculate(Node src, Node dst, Network<Node, ParallelEdges> network);

  <T extends ShortestPath> T calculate(Class<T> type, Node src, Node dst,
      Network<Node, ParallelEdges> network, ShortestPathCreator<T> shortestPathCreator);
}
