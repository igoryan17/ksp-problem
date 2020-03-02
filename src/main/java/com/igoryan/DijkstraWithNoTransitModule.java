package com.igoryan;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.igoryan.sp.ShortestPathCalculator;
import com.igoryan.sp.impl.DijkstraShortestPathCalculatorWithNoTransit;

public class DijkstraWithNoTransitModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ShortestPathCalculator.class).annotatedWith(Names.named("noTransit"))
        .to(DijkstraShortestPathCalculatorWithNoTransit.class);
  }
}
