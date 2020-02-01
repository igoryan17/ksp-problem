package com.igoryan;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.igoryan.apsp.AllPairsCalculator;
import com.igoryan.apsp.impl.MpsAllPairsCalculator;
import com.igoryan.ksp.KShortestPathsCalculator;
import com.igoryan.ksp.impl.MpsKShortestPathCalculatorWithNoTransit;
import com.igoryan.model.path.MpsShortestPath;

public class MpsWithNoTransitModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(new TypeLiteral<KShortestPathsCalculator<MpsShortestPath>>() { })
        .to(MpsKShortestPathCalculatorWithNoTransit.class);
    bind(AllPairsCalculator.class).to(MpsAllPairsCalculator.class);
  }
}
