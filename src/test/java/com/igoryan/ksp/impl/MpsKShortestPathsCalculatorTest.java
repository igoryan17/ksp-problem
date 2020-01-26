package com.igoryan.ksp.impl;

import com.igoryan.sp.impl.DijkstraShortestPathCalculator;
import org.junit.Before;

public class MpsKShortestPathsCalculatorTest extends BaseKShortestPathsCalculatorTest {

  @Before
  public void setUp() throws Exception {
    shortestPathCalculator = new DijkstraShortestPathCalculator();
    kShortestPathsCalculator = new MpsKShortestPathsCalculator(shortestPathCalculator);
  }
}