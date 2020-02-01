package com.igoryan;

import com.google.inject.AbstractModule;
import com.igoryan.sp.ShortestPathCalculator;
import com.igoryan.sp.impl.DijkstraShortestPathCalculator;

public class DijkstraModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ShortestPathCalculator.class).to(DijkstraShortestPathCalculator.class);
  }
}
