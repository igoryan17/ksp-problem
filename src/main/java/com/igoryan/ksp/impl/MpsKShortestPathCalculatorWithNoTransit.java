package com.igoryan.ksp.impl;

import static com.igoryan.util.ShortestPathsUtil.addInEdges;
import static com.igoryan.util.ShortestPathsUtil.removeInEdges;
import static com.igoryan.util.ShortestPathsUtil.subNetworkExpectInEdgesOfNoTransit;

import com.google.common.graph.Graphs;
import com.google.common.graph.MutableNetwork;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.igoryan.model.network.Node;
import com.igoryan.model.network.edge.ParallelEdges;
import com.igoryan.model.path.MpsShortestPath;
import com.igoryan.sp.ShortestPathCalculator;
import java.util.List;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Logger;

@Singleton
@Log4j2(topic = "com.igoryan.ksp")
public final class MpsKShortestPathCalculatorWithNoTransit extends BaseMpsKShortestPathCalculator {

  private MutableNetwork<Node, ParallelEdges> subNetworkWithoutInEdgesToNoTransit;

  @Inject
  public MpsKShortestPathCalculatorWithNoTransit(
      final @Named("noTransit") ShortestPathCalculator shortestPathCalculator) {
    super(shortestPathCalculator);
  }

  @Override
  public List<MpsShortestPath> calculate(final @NonNull Node src, final @NonNull Node dst,
      final MutableNetwork<Node, ParallelEdges> network, final int count) {
    if (subNetworkWithoutInEdgesToNoTransit == null) {
      subNetworkWithoutInEdgesToNoTransit = subNetworkExpectInEdgesOfNoTransit(network);
      needCheckCycles = Graphs.hasCycle(subNetworkWithoutInEdgesToNoTransit);
    }
    if (cachedTransposedNetwork == null) {
      cachedTransposedNetwork = Graphs.transpose(network);
    }
    if (lastDst != dst) {
      if (lastDst != null) {
        cachedEdgesStructure.clear();
        removeInEdges(lastDst, subNetworkWithoutInEdgesToNoTransit);
      }
      lastDst = dst;
      addInEdges(dst, subNetworkWithoutInEdgesToNoTransit, network);
      cachedShortestPathTree =
          calculateShortestPathTree(src, dst, subNetworkWithoutInEdgesToNoTransit);
      fillEdgesStructure(subNetworkWithoutInEdgesToNoTransit);
    }
    return performMpsAlgorithm(src, dst, count);
  }

  @Override
  Logger log() {
    return log;
  }

  @Override
  public void clear() {
    super.clear();
    subNetworkWithoutInEdgesToNoTransit = null;
  }
}
