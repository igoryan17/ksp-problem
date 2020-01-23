package com.igoryan;

import com.google.inject.AbstractModule;
import com.igoryan.ksp.KShortestPathsCalculator;
import com.igoryan.ksp.impl.YenKShortestPathsCalculatorWithNoTransit;
import com.igoryan.sp.ShortestPathCalculator;
import com.igoryan.sp.impl.DijkstraShortestPathCalculatorWithNoTransit;

public class YenWithDijkstraWithNoTransitModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ShortestPathCalculator.class).to(DijkstraShortestPathCalculatorWithNoTransit.class);
    bind(KShortestPathsCalculator.class).to(YenKShortestPathsCalculatorWithNoTransit.class);
  }
}
