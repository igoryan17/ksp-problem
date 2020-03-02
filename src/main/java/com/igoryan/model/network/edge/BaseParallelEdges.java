package com.igoryan.model.network.edge;

import lombok.Getter;

public abstract class BaseParallelEdges implements ParallelEdges {

  @Getter
  protected final int srcSwNum;
  @Getter
  protected final int dstSwNum;
  protected transient int hash;

  protected BaseParallelEdges(final int srcSwNum, final int dstSwNum) {
    this.srcSwNum = srcSwNum;
    this.dstSwNum = dstSwNum;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final BaseParallelEdges that = (BaseParallelEdges) o;

    if (srcSwNum != that.srcSwNum) {
      return false;
    }
    return dstSwNum == that.dstSwNum;
  }

  @Override
  public int hashCode() {
    int h = hash;
    if (h == 0) {
      h = srcSwNum;
      h = 31 * h + dstSwNum;
      hash = h;
    }
    return h;
  }
}
