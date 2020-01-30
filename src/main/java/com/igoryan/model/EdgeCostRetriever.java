package com.igoryan.model;

@FunctionalInterface
public interface EdgeCostRetriever {
  long getCost(Edge edge);
}
