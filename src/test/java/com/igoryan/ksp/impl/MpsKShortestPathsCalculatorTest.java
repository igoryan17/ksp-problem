package com.igoryan.ksp.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import com.igoryan.ksp.KShortestPathsCalculator;
import com.igoryan.model.Edge;
import com.igoryan.model.Node;
import com.igoryan.model.ParallelEdges;
import com.igoryan.model.ShortestPath;
import com.igoryan.sp.ShortestPathCalculator;
import com.igoryan.sp.impl.DijkstraShortestPathCalculator;
import java.util.List;
import org.junit.Test;

public class MpsKShortestPathsCalculatorTest {

  private final ShortestPathCalculator shortestPathCalculator = new DijkstraShortestPathCalculator();
  private final KShortestPathsCalculator kShortestPathsCalculator =
      new MpsKShortestPathsCalculator(shortestPathCalculator);

  @Test
  public void twoNodesWithTwoEdges() {
    final Node src = new Node(1, true);
    final Node dst = new Node(2, true);
    final Edge firstEdge = new Edge(1, 2, (short) 1, (short) 1, 1L);
    final Edge secondEdge = new Edge(1, 2, (short) 2, (short) 2, 2L);
    final ParallelEdges parallelEdges = new ParallelEdges(1, 2, 2);
    parallelEdges.add(firstEdge);
    parallelEdges.add(secondEdge);
    final MutableNetwork<Node, ParallelEdges> network = NetworkBuilder.directed()
        .allowsParallelEdges(false)
        .allowsSelfLoops(false)
        .expectedEdgeCount(1)
        .expectedNodeCount(2)
        .build();
    network.addNode(src);
    network.addNode(dst);
    network.addEdge(src, dst, parallelEdges);
    final List<ShortestPath> result = kShortestPathsCalculator.calculate(src, dst, network, 2);
    assertThat(result, hasSize(2));
    assertThat(result.get(0).getCost(), is(1L));
    assertThat(result.get(1).getCost(), is(2L));
  }
}