package com.igoryan.ksp;

import com.google.common.graph.MutableNetwork;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.edge.ParallelEdges;
import com.igoryan.model.path.ShortestPath;
import java.util.List;

public interface KShortestPathsCalculator<T extends ShortestPath> {

  List<T> calculate(final Node src, final Node dst,
      final MutableNetwork<Node, ParallelEdges> network, int count);

  void clear();
}
