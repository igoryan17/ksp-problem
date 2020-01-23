package com.igoryan.ksp;

import com.google.common.graph.MutableNetwork;
import com.igoryan.model.Node;
import com.igoryan.model.ParallelEdges;
import com.igoryan.model.ShortestPath;
import java.util.List;

public interface KShortestPathsCalculator {

  List<ShortestPath> calculate(final Node src, final Node dst, final MutableNetwork<Node, ParallelEdges> network,
      int count);

  default void clear() {

  }
}
