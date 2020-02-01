package com.igoryan.ksp.impl;

import com.igoryan.model.path.MpsShortestPath;
import com.igoryan.sp.impl.DijkstraShortestPathCalculator;
import org.junit.Before;

public class MpsKShortestPathsCalculatorTest
    extends BaseKShortestPathsCalculatorTest<MpsShortestPath> {

  @Before
  public void setUp() throws Exception {
    shortestPathCalculator = new DijkstraShortestPathCalculator();
    kShortestPathsCalculator = new MpsKShortestPathsCalculator(shortestPathCalculator);
  }
}