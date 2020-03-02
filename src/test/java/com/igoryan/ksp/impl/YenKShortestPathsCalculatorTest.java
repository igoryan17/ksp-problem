package com.igoryan.ksp.impl;

import com.igoryan.model.path.YenShortestPath;
import com.igoryan.sp.impl.DijkstraShortestPathCalculator;
import com.igoryan.sp.impl.DijkstraShortestPathCalculatorWithNoTransit;
import org.junit.Before;

public class YenKShortestPathsCalculatorTest
    extends BaseKShortestPathsCalculatorTest<YenShortestPath> {

  @Before
  public void setUp() throws Exception {
    shortestPathCalculator = new DijkstraShortestPathCalculator();
    shortestPathCalculatorWithNoTransit = new DijkstraShortestPathCalculatorWithNoTransit();
    kShortestPathsCalculator = new YenKShortestPathsCalculator(shortestPathCalculator,
        shortestPathCalculatorWithNoTransit);
  }
}