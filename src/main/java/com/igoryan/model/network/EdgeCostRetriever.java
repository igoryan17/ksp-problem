package com.igoryan.model.network;

@FunctionalInterface
public interface EdgeCostRetriever {
  long getCost(Edge edge);
}
