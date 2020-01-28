package com.igoryan.sp;

import com.google.common.graph.Network;
import com.igoryan.model.Node;
import com.igoryan.model.ParallelEdges;

public interface ShortestPathCalculator {

  void calculate(Node src, Node dst, Network<Node, ParallelEdges> network);
}
