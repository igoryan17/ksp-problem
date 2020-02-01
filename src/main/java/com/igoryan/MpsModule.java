package com.igoryan;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.igoryan.apsp.AllPairsCalculator;
import com.igoryan.apsp.impl.MpsAllPairsCalculator;
import com.igoryan.ksp.KShortestPathsCalculator;
import com.igoryan.ksp.impl.MpsKShortestPathsCalculator;
import com.igoryan.model.path.MpsShortestPath;

public class MpsModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(new TypeLiteral<KShortestPathsCalculator<MpsShortestPath>>() { })
        .to(MpsKShortestPathsCalculator.class);
    bind(AllPairsCalculator.class).to(MpsAllPairsCalculator.class);
  }
}
