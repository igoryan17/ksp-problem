package com.igoryan.util;

import static com.igoryan.model.CostGenerations.EQUAL;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import com.igoryan.model.Algorithms;
import com.igoryan.model.CostGenerations;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.edge.Edge;
import com.igoryan.model.network.edge.EdgesFactory;
import com.igoryan.model.network.edge.ParallelEdges;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
import lombok.NonNull;

public final class TopologyUtil {

  private static final int DEFAULT_WEIGHT = 100;
  private static final int MAX_WEIGHT = 10000;
  private static final int MAX_WEIGHT_BETWEEN_GATEWAYS = 100;
  private static final int MIN_LINKS = 4;
  private static final int MAX_LINKS = 20;

  private TopologyUtil() {
  }

  public static MutableNetwork<Node, ParallelEdges> simulateSdWanTopology(
      final int gwCount, final int cpeCount, final boolean linksBetweenGwEnabled,
      final @NonNull CostGenerations costGenerations, final @NonNull Algorithms algorithm) {
    final Random randomForWeights = new Random();
    final Random randomForParallelLinksCount = new Random();
    final Map<Integer, Node> swNumToNode = new HashMap<>();
    final MutableNetwork<Node, ParallelEdges> result = NetworkBuilder.directed()
        .expectedNodeCount(gwCount + cpeCount)
        .build();

    IntStream.range(0, gwCount).forEach(gwNum -> {
      final Node node = new Node(gwNum, true);
      swNumToNode.put(gwNum, node);
      result.addNode(node);
    });

    IntStream.range(gwCount, cpeCount + gwCount).forEach(cpeNum -> {
      final Node cpe = new Node(cpeNum, false);
      swNumToNode.put(cpeNum, cpe);
      result.addNode(cpe);

      IntStream.range(0, gwCount).forEach(gwNum -> {
        final Node gw = swNumToNode.get(gwNum);
        final int parallelLinksCount =
            randomForParallelLinksCount.nextInt(MAX_LINKS - MIN_LINKS + 1) + MIN_LINKS;
        final List<Edge> edgesToGw = new ArrayList<>(parallelLinksCount);
        final List<Edge> fromGwEdges = new ArrayList<>(parallelLinksCount);
        for (int i = 0; i < parallelLinksCount; i++) {
          final Edge cpeGw = new Edge(cpeNum, (short) (cpeNum + i), gwNum,
              (short) (gwNum + cpeNum + i), costGenerations == EQUAL
              ? DEFAULT_WEIGHT
              : randomForWeights.nextInt(MAX_WEIGHT) + 1);
          edgesToGw.add(cpeGw);
          final Edge gwCpe = new Edge(gwNum, (short) (gwNum + cpeNum + i), cpeNum,
              (short) (cpeNum + i), costGenerations == EQUAL
              ? DEFAULT_WEIGHT
              : randomForWeights.nextInt(MAX_WEIGHT) + 1);
          fromGwEdges.add(gwCpe);
        }
        result.addEdge(cpe, gw, EdgesFactory.create(cpeNum, gwNum, edgesToGw, algorithm));
        result.addEdge(gw, cpe, EdgesFactory.create(gwNum, cpeNum, fromGwEdges, algorithm));
      });
    });

    if (!linksBetweenGwEnabled) {
      return result;
    }

    for (int srcGwNum = 0; srcGwNum < gwCount; srcGwNum++) {
      final Node srcGw = swNumToNode.get(srcGwNum);
      for (int dstGwNum = 0; dstGwNum < gwCount; dstGwNum++) {
        if (srcGwNum == dstGwNum) {
          continue;
        }
        final Node dstGw = swNumToNode.get(dstGwNum);
        final int parallelLinksCount =
            randomForParallelLinksCount.nextInt(MAX_LINKS - MIN_LINKS + 1) + MIN_LINKS;
        final List<Edge> gwEdges = new ArrayList<>(parallelLinksCount);
        for (int i = 0; i < parallelLinksCount; i++) {
          final Edge betweenGateways =
              new Edge(srcGwNum, (short) (srcGwNum + i), dstGwNum, ((short) (dstGwNum + i)),
                  costGenerations == EQUAL
                      ? DEFAULT_WEIGHT
                      : randomForWeights.nextInt(MAX_WEIGHT_BETWEEN_GATEWAYS) + 1);
          gwEdges.add(betweenGateways);
        }
        result.addEdge(srcGw, dstGw, EdgesFactory.create(srcGwNum, dstGwNum, gwEdges, algorithm));
      }
    }
    return result;
  }
}
