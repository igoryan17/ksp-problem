package com.igoryan.model.tree;

import com.igoryan.model.path.MpsShortestPath;
import com.igoryan.model.network.NodeEdgeTuple;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.NonNull;

public class KShortestPathsTree {

  private final Map<NodeEdgeTuple, NodeEdgeTuple> connectionsOfRoot = new HashMap<>();

  public void add(@NonNull MpsShortestPath path, int pathNum) {
    final NodeEdgeTuple firstKey = path.getKey(0);
    NodeEdgeTuple temp = connectionsOfRoot.putIfAbsent(firstKey, firstKey);
    if (temp == null) {
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
      final NodeEdgeTuple next = old == null ? key : old;
      temp.getPathNumToNextKey().put(pathNum, next);
      temp = next;
    }
  }

  @Nullable
  public NodeEdgeTuple getDeviationKey(@NonNull MpsShortestPath path) {
    final NodeEdgeTuple firstKey = path.getKey(0);
    NodeEdgeTuple temp = connectionsOfRoot.get(firstKey);
    if (temp == null) {
      return firstKey;
    }
    for (int i = 1; i < path.getEdges().size(); i++) {
      final NodeEdgeTuple key = path.getKey(i);
      final Map<NodeEdgeTuple, NodeEdgeTuple> children = temp.getChildren();
      if (children == null) {
        return key;
      }
      temp = children.get(key);
      if (temp == null) {
        return key;
      }
    }
    return null;
  }
}
