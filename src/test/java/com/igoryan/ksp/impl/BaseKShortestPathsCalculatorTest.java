package com.igoryan.ksp.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.Lists;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import com.igoryan.ksp.KShortestPathsCalculator;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.edge.ArrayYenParallelEdges;
import com.igoryan.model.network.edge.Edge;
import com.igoryan.model.network.edge.ParallelEdges;
import com.igoryan.model.path.ShortestPath;
import com.igoryan.sp.ShortestPathCalculator;
import java.util.List;
import org.junit.After;
import org.junit.Test;

public class BaseKShortestPathsCalculatorTest<T extends ShortestPath> {

  protected ShortestPathCalculator shortestPathCalculator;
  protected ShortestPathCalculator shortestPathCalculatorWithNoTransit;
  protected KShortestPathsCalculator<T> kShortestPathsCalculator;

  @After
  public void tearDown() throws Exception {
    kShortestPathsCalculator.clear();
  }

  @Test
  public void twoNodesWithTwoEdges() {
    final Node src = new Node(1, true);
    final Node dst = new Node(2, true);

    final Edge firstEdge = new Edge(1, (short) 1, 2, (short) 1, 1L);
    final Edge secondEdge = new Edge(1, (short) 2, 2, (short) 2, 2L);
    final ArrayYenParallelEdges parallelEdges =
        new ArrayYenParallelEdges(1, 2, Lists.newArrayList(firstEdge, secondEdge));

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

    final Edge fromSrcToTransit = new Edge(1, (short) 1, 2, (short) 1, 1L);
    final ArrayYenParallelEdges fromSrcToTransitEdges =
        new ArrayYenParallelEdges(1, 2, Lists.newArrayList(fromSrcToTransit));

    final Edge firstFromTransitToSource = new Edge(2, (short) 2, 3, (short) 1, 1L);
    final Edge secondFromTransitToSource = new Edge(2, (short) 3, 3, (short) 2, 2L);
    final ArrayYenParallelEdges fromTransitToDstEdges = new ArrayYenParallelEdges(2, 3,
        Lists.newArrayList(firstFromTransitToSource, secondFromTransitToSource));

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

  @Test
  public void testWithCycle() {
    final Node src = new Node(1, true);
    final Node transit1 = new Node(2, true);
    final Node transit2 = new Node(3, true);
    final Node dst = new Node(4, true);

    final Edge fromSrcToTransit1Cost1 = new Edge(1, (short) 1, 2, (short) 1, 1L);
    final Edge fromSrcToTransit1Cost2 = new Edge(1, (short) 2, 2, (short) 2, 2L);
    final ArrayYenParallelEdges fromSrcToTransit1 =
        new ArrayYenParallelEdges(1, 2,
            Lists.newArrayList(fromSrcToTransit1Cost1, fromSrcToTransit1Cost2));

    final Edge fromSrcToTransit2Cost1 = new Edge(1, (short) 3, 3, (short) 1, 1L);
    final Edge fromSrcToTransit2Cost2 = new Edge(1, (short) 4, 3, (short) 2, 2L);
    final ArrayYenParallelEdges fromSrcToTransit2 =
        new ArrayYenParallelEdges(1, 3,
            Lists.newArrayList(fromSrcToTransit2Cost1, fromSrcToTransit2Cost2));

    final ArrayYenParallelEdges fromTransit1ToTransit2 =
        new ArrayYenParallelEdges(2, 3,
            Lists.newArrayList(new Edge(2, (short) 3, 3, (short) 3, 5L)));

    final ArrayYenParallelEdges fromTransit2ToTransit1 =
        new ArrayYenParallelEdges(3, 2,
            Lists.newArrayList(new Edge(3, (short) 4, 2, (short) 4, 6L)));

    final ArrayYenParallelEdges fromTransit1ToDst =
        new ArrayYenParallelEdges(2, 4,
            Lists.newArrayList(new Edge(2, (short) 4, 4, (short) 1, 3L)));

    final ArrayYenParallelEdges fromTransit2ToDst =
        new ArrayYenParallelEdges(3, 4,
            Lists.newArrayList(new Edge(3, (short) 5, 4, (short) 2, 4L)));

    final MutableNetwork<Node, ParallelEdges> network = NetworkBuilder.directed()
        .expectedNodeCount(4)
        .expectedEdgeCount(6)
        .build();

    network.addNode(src);
    network.addNode(transit1);
    network.addNode(transit2);
    network.addNode(dst);

    network.addEdge(src, transit1, fromSrcToTransit1);
    network.addEdge(src, transit2, fromSrcToTransit2);
    network.addEdge(transit1, transit2, fromTransit1ToTransit2);
    network.addEdge(transit2, transit1, fromTransit2ToTransit1);
    network.addEdge(transit1, dst, fromTransit1ToDst);
    network.addEdge(transit2, dst, fromTransit2ToDst);

    final List<T> calculated = kShortestPathsCalculator.calculate(src, dst, network, 8);

    assertThat(calculated, hasSize(8));

    assertThat(calculated.get(0).getOriginalCost(), is(4L));
    assertThat(calculated.get(0).getEdges(),
        contains(fromSrcToTransit1Cost1, fromTransit1ToDst.getFirstUnusedOrNull()));

    assertThat(calculated.get(1).getOriginalCost(), is(5L));
    assertThat(calculated.get(1).getEdges(), anyOf(
        contains(fromSrcToTransit1Cost2, fromTransit1ToDst.getFirstUnusedOrNull()),
        contains(fromSrcToTransit2Cost1, fromTransit2ToDst.getFirstUnusedOrNull())));

    assertThat(calculated.get(2).getOriginalCost(), is(5L));
    assertThat(calculated.get(2).getEdges(), anyOf(
        contains(fromSrcToTransit1Cost2, fromTransit1ToDst.getFirstUnusedOrNull()),
        contains(fromSrcToTransit2Cost1, fromTransit2ToDst.getFirstUnusedOrNull())));

    assertThat(calculated.get(3).getOriginalCost(), is(6L));
    assertThat(calculated.get(3).getEdges(),
        contains(fromSrcToTransit2Cost2, fromTransit2ToDst.getFirstUnusedOrNull()));

    assertThat(calculated.get(4).getOriginalCost(), is(10L));
    assertThat(calculated.get(4).getEdges(), anyOf(
        contains(fromSrcToTransit1Cost1, fromTransit1ToTransit2.getFirstUnusedOrNull(),
            fromTransit2ToDst.getFirstUnusedOrNull()),
        contains(fromSrcToTransit2Cost1, fromTransit2ToTransit1.getFirstUnusedOrNull(),
            fromTransit1ToDst.getFirstUnusedOrNull())
    ));

    assertThat(calculated.get(5).getOriginalCost(), is(10L));
    assertThat(calculated.get(5).getEdges(), anyOf(
        contains(fromSrcToTransit1Cost1, fromTransit1ToTransit2.getFirstUnusedOrNull(),
            fromTransit2ToDst.getFirstUnusedOrNull()),
        contains(fromSrcToTransit2Cost1, fromTransit2ToTransit1.getFirstUnusedOrNull(),
            fromTransit1ToDst.getFirstUnusedOrNull())
    ));

    assertThat(calculated.get(6).getOriginalCost(), is(11L));
    assertThat(calculated.get(6).getEdges(), anyOf(
        contains(fromSrcToTransit1Cost2, fromTransit1ToTransit2.getFirstUnusedOrNull(),
            fromTransit2ToDst.getFirstUnusedOrNull()),
        contains(fromSrcToTransit2Cost2, fromTransit2ToTransit1.getFirstUnusedOrNull(),
            fromTransit1ToDst.getFirstUnusedOrNull())
    ));

    assertThat(calculated.get(7).getOriginalCost(), is(11L));
    assertThat(calculated.get(7).getEdges(), anyOf(
        contains(fromSrcToTransit1Cost2, fromTransit1ToTransit2.getFirstUnusedOrNull(),
            fromTransit2ToDst.getFirstUnusedOrNull()),
        contains(fromSrcToTransit2Cost2, fromTransit2ToTransit1.getFirstUnusedOrNull(),
            fromTransit1ToDst.getFirstUnusedOrNull())
    ));

    final List<T> calculatedWithMoreThanAvailable =
        kShortestPathsCalculator.calculate(src, dst, network, 10);
    assertThat(calculatedWithMoreThanAvailable, hasSize(8));
  }
}
