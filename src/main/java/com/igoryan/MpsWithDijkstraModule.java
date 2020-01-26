package com.igoryan;

import com.google.inject.AbstractModule;
import com.igoryan.ksp.KShortestPathsCalculator;
import com.igoryan.ksp.impl.MpsKShortestPathsCalculator;
import com.igoryan.sp.ShortestPathCalculator;
import com.igoryan.sp.impl.DijkstraShortestPathCalculator;

public class MpsWithDijkstraModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ShortestPathCalculator.class).to(DijkstraShortestPathCalculator.class);
    bind(KShortestPathsCalculator.class).to(MpsKShortestPathsCalculator.class);
  }
}
