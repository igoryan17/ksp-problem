package com.igoryan.apsp;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.edge.ParallelEdges;
import com.igoryan.model.path.ShortestPath;
import java.util.List;
import java.util.Map;

public interface AllPairsCalculator {

  Map<EndpointPair<Node>, List<? extends ShortestPath>> calculate(
      final MutableNetwork<Node, ParallelEdges> network, final int PerPairCount);
}
