package com.igoryan.model.network;

import com.igoryan.model.network.edge.Edge;

@FunctionalInterface
public interface EdgeCostRetriever {
  long getCost(Edge edge);
}
