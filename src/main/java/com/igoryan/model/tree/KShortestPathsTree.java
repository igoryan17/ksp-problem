package com.igoryan.model.tree;

import com.igoryan.model.network.NodeEdgeTuple;
import com.igoryan.model.path.MpsShortestPath;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;

public class KShortestPathsTree {

  private final Map<NodeEdgeTuple, NodeEdgeTuple> connectionsOfRoot = new HashMap<>();

  public void add(@NonNull MpsShortestPath path) {
    final NodeEdgeTuple firstKey = path.getKey(0);
    NodeEdgeTuple temp = connectionsOfRoot.putIfAbsent(firstKey, firstKey);
    if (temp == null) {
      path.setDeviationNodeIndex(0);
      temp = firstKey;
    }
    for (int i = 1; i < path.getEdges().size(); i++) {
      final NodeEdgeTuple key = path.getKey(i);
      Map<NodeEdgeTuple, NodeEdgeTuple> children = temp.getChildren();
      if (children == null) {
        children = new HashMap<>();
        temp.setChildren(children);
      }
      final NodeEdgeTuple old = children.putIfAbsent(key, key);
      final NodeEdgeTuple next;
      if (old == null) {
        path.setDeviationNodeIndex(i);
        next = key;
      } else {
        next = old;
      }
      temp = next;
    }
  }
}
