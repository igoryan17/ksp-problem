package com.igoryan.util;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import com.igoryan.model.network.Edge;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.ParallelEdges;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

public final class TopologyUtil {

  private static final int MAX_WEIGHT = 10000;
  private static final int MAX_WEIGHT_BETWEEN_GATEWAYS = 100;
  private static final int MIN_LINKS = 4;
  private static final int MAX_LINKS = 20;

  private TopologyUtil() {
  }

  public static MutableNetwork<Node, ParallelEdges> simulateSdWanTopology(
      int gwCount, int cpeCount, final boolean linksBetweenGwEnabled) {
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
        final ParallelEdges parallelEdgesToGw =
            new ParallelEdges(cpeNum, gwNum, parallelLinksCount);
        final ParallelEdges fromGwParallelEdges =
            new ParallelEdges(gwNum, cpeNum, parallelLinksCount);
        for (int i = 0; i < parallelLinksCount; i++) {
          final Edge cpeGw = new Edge(cpeNum, (short) i, gwNum, (short) i,
              randomForWeights.nextInt(MAX_WEIGHT) + 1);
          parallelEdgesToGw.add(cpeGw);
          final Edge gwCpe = new Edge(gwNum, (short) i, cpeNum, (short) i,
              randomForWeights.nextInt(MAX_WEIGHT) + 1);
          fromGwParallelEdges.add(gwCpe);
        }
        result.addEdge(cpe, gw, parallelEdgesToGw);
        result.addEdge(gw, cpe, fromGwParallelEdges);
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
        final Edge betweenGateways =
            new Edge(srcGwNum, (short) srcGwNum, dstGwNum, (short) dstGwNum,
                randomForWeights.nextInt(MAX_WEIGHT_BETWEEN_GATEWAYS) + 1);
        final ParallelEdges gwEdges = new ParallelEdges(srcGwNum, dstGwNum, 1);
        gwEdges.add(betweenGateways);
        result.addEdge(srcGw, dstGw, gwEdges);
      }
    }
    return result;
  }
}
