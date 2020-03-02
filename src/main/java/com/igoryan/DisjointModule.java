package com.igoryan;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.igoryan.apsp.AllPairsCalculator;
import com.igoryan.apsp.impl.YenAllPairsCalculator;
import com.igoryan.ksp.KShortestPathsCalculator;
import com.igoryan.ksp.impl.DisjointKShortestPathCalculator;
import com.igoryan.model.path.YenShortestPath;
import com.igoryan.sp.ShortestPathCalculator;
import com.igoryan.sp.impl.DisjointShortestPathCalculator;

public class DisjointModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ShortestPathCalculator.class).to(DisjointShortestPathCalculator.class);
    bind(new TypeLiteral<KShortestPathsCalculator<YenShortestPath>>() {
    })
        .to(DisjointKShortestPathCalculator.class);
    bind(AllPairsCalculator.class).to(YenAllPairsCalculator.class);
  }
}
