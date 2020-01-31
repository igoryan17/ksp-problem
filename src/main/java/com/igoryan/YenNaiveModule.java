package com.igoryan;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.igoryan.apsp.AllPairsCalculator;
import com.igoryan.apsp.impl.YenAllPairsCalculator;
import com.igoryan.ksp.KShortestPathsCalculator;
import com.igoryan.ksp.impl.NaiveYenKShortestPathCalculatorWithNoTransit;
import com.igoryan.model.path.YenShortestPath;

public class YenNaiveModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(new TypeLiteral<KShortestPathsCalculator<YenShortestPath>>() { })
        .to(NaiveYenKShortestPathCalculatorWithNoTransit.class);
    bind(AllPairsCalculator.class).to(YenAllPairsCalculator.class);
  }
}
