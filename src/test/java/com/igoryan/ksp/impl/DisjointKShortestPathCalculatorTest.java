package com.igoryan.ksp.impl;

import static com.igoryan.model.network.ParallelEdges.COMPARE_EDGES_BY_USED_AND_COST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import com.igoryan.model.network.Edge;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.ParallelEdges;
import com.igoryan.model.path.YenShortestPath;
import com.igoryan.sp.impl.DijkstraShortestPathCalculator;
import java.util.List;
import org.junit.Test;

public class DisjointKShortestPathCalculatorTest {

  private DisjointKShortestPathCalculator kShortestPathCalculator =
      new DisjointKShortestPathCalculator(new DijkstraShortestPathCalculator());

  @Test
  public void calculate() {
    final Node src = new Node(0, true);
    final Node transit = new Node(1, true);
    final Node dst = new Node(2, true);

    final Edge firstSrcToTransit = new Edge(0, (short) 1, 1, (short) 1, 1L);
    final Edge secondSrcToTransit = new Edge(0, (short) 2, 1, (short) 2, 2L);
    final Edge thirdSrcToTransit = new Edge(0, (short) 3, 1, (short) 3, 3L);

    final Edge firstTransitToDst = new Edge(1, (short) 4, 2, (short) 4, 4L);
    final Edge secondTransitToDst = new Edge(1, (short) 5, 2, (short) 5, 5L);
    final Edge thirdTransitToDst = new Edge(1, (short) 6, 2, (short) 6, 6L);

    final ParallelEdges srcToTransit = new ParallelEdges(0, 1, 3, COMPARE_EDGES_BY_USED_AND_COST);
    srcToTransit.add(firstSrcToTransit);
    srcToTransit.add(secondSrcToTransit);
    srcToTransit.add(thirdSrcToTransit);

    final ParallelEdges transitToDst = new ParallelEdges(1, 2, 3, COMPARE_EDGES_BY_USED_AND_COST);
    transitToDst.add(firstTransitToDst);
    transitToDst.add(secondTransitToDst);
    transitToDst.add(thirdTransitToDst);

    final MutableNetwork<Node, ParallelEdges> network = NetworkBuilder.directed()
        .expectedNodeCount(3)
        .expectedEdgeCount(2)
        .build();
    network.addEdge(src, transit, srcToTransit);
    network.addEdge(transit, dst, transitToDst);

    final List<YenShortestPath> calculate = kShortestPathCalculator.calculate(src, dst, network, 3);
    assertThat(calculate, hasSize(3));

    assertThat(calculate.get(0).getCost(), is(5L));
    assertThat(calculate.get(0).getEdges(), contains(firstSrcToTransit, firstTransitToDst));

    assertThat(calculate.get(1).getCost(), is(7L));
    assertThat(calculate.get(1).getEdges(), contains(secondSrcToTransit, secondTransitToDst));

    assertThat(calculate.get(2).getCost(), is(9L));
    assertThat(calculate.get(2).getEdges(), contains(thirdSrcToTransit, thirdTransitToDst));
  }
}