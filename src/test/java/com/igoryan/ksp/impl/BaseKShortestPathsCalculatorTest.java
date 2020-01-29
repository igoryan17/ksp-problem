package com.igoryan.ksp.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
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
import java.util.List;
import org.junit.After;
import org.junit.Test;

public class BaseKShortestPathsCalculatorTest<T extends ShortestPath> {

  protected ShortestPathCalculator shortestPathCalculator;
  protected KShortestPathsCalculator<T> kShortestPathsCalculator;

  @After
  public void tearDown() throws Exception {
    kShortestPathsCalculator.clear();
  }

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
    final List<T> result = kShortestPathsCalculator.calculate(src, dst, network, 2);
    assertThat(result, hasSize(2));
    assertThat(result.get(0).getOriginalCost(), is(1L));
    assertThat(result.get(0).getEdges(), contains(firstEdge));
    assertThat(result.get(1).getOriginalCost(), is(2L));
    assertThat(result.get(1).getEdges(), contains(secondEdge));
  }

  @Test
  public void threeNodesWithParallelEdges() {
    final Node src = new Node(1, true);
    final Node transit = new Node(2, true);
    final Node dst = new Node(3, true);

    final Edge fromSrcToTransit = new Edge(1, 2, (short) 1, (short) 1, 1L);
    final ParallelEdges fromSrcToTransitEdges = new ParallelEdges(1, 2, 1);
    fromSrcToTransitEdges.add(fromSrcToTransit);

    final Edge firstFromTransitToSource = new Edge(2, 3, (short) 2, (short) 1, 1L);
    final Edge secondFromTransitToSource = new Edge(2, 3, (short) 3, (short) 2, 2L);
    final ParallelEdges fromTransitToDstEdges = new ParallelEdges(2, 3, 2);
    fromTransitToDstEdges.add(firstFromTransitToSource);
    fromTransitToDstEdges.add(secondFromTransitToSource);

    final MutableNetwork<Node, ParallelEdges> network = NetworkBuilder.directed()
        .allowsParallelEdges(false)
        .allowsSelfLoops(false)
        .expectedEdgeCount(3)
        .expectedNodeCount(3)
        .build();

    network.addNode(src);
    network.addNode(transit);
    network.addNode(dst);

    network.addEdge(src, transit, fromSrcToTransitEdges);
    network.addEdge(transit, dst, fromTransitToDstEdges);

    final List<T> result = kShortestPathsCalculator.calculate(src, dst, network, 2);
    assertThat(result, hasSize(2));

    assertThat(result.get(0).getOriginalCost(), is(2L));
    assertThat(result.get(0).getEdges(), contains(fromSrcToTransit, firstFromTransitToSource));

    assertThat(result.get(1).getOriginalCost(), is(3L));
    assertThat(result.get(1).getEdges(), contains(fromSrcToTransit, secondFromTransitToSource));
  }
}
