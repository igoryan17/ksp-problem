package com.igoryan.model.network;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NonNull;

public final class ParallelEdges implements Queue<Edge> {

  public static final Comparator<Edge> COMPARE_EDGES_BY_COST =
      Comparator.comparingLong(Edge::getCost);
  public static final Comparator<Edge> COMPARE_EDGES_BY_USED_AND_COST =
      Comparator.comparing(Edge::isUsed, (o1, o2) -> o1 == o2 ? 0 : o1 ? 1 : -1)
          .thenComparingLong(Edge::getCost);

  @Getter
  private final int srcSwNum;
  @Getter
  private final int dstSwNum;
  private final Queue<Edge> edges;
  private transient int hash;

  public ParallelEdges(final int srcSwNum, final int dstSwNum,
      final @NonNull Comparator<Edge> comparator) {
    this.srcSwNum = srcSwNum;
    this.dstSwNum = dstSwNum;
    this.edges = new PriorityQueue<>(comparator);
  }

  public ParallelEdges(final int srcSwNum, final int dstSwNum, int capacity,
      final Comparator<Edge> comparator) {
    this.srcSwNum = srcSwNum;
    this.dstSwNum = dstSwNum;
    this.edges = new PriorityQueue<>(capacity, comparator);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ParallelEdges edges = (ParallelEdges) o;

    if (srcSwNum != edges.srcSwNum) {
      return false;
    }
    return dstSwNum == edges.dstSwNum;
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

  @Override
  public boolean add(final Edge edge) {
    return edges.add(edge);
  }

  @Override
  public boolean offer(final Edge edge) {
    return edges.offer(edge);
  }

  @Override
  public Edge remove() {
    return edges.remove();
  }

  @Override
  public Edge poll() {
    return edges.poll();
  }

  @Override
  public Edge element() {
    return edges.element();
  }

  @Override
  public Edge peek() {
    return edges.peek();
  }

  @Override
  public int size() {
    return edges.size();
  }

  @Override
  public boolean isEmpty() {
    return edges.isEmpty();
  }

  @Override
  public boolean contains(final Object o) {
    return edges.contains(o);
  }

  @Override
  public Iterator<Edge> iterator() {
    return edges.iterator();
  }

  @Override
  public Object[] toArray() {
    return edges.toArray();
  }

  @Override
  public <T> T[] toArray(final T[] a) {
    return edges.toArray(a);
  }

  @Override
  public boolean remove(final Object o) {
    return edges.remove(o);
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    return edges.containsAll(c);
  }

  @Override
  public boolean addAll(final Collection<? extends Edge> c) {
    return edges.addAll(c);
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    return edges.removeAll(c);
  }

  @Override
  public boolean removeIf(final Predicate<? super Edge> filter) {
    return edges.removeIf(filter);
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    return edges.retainAll(c);
  }

  @Override
  public void clear() {
    edges.clear();
  }

  @Override
  public Spliterator<Edge> spliterator() {
    return edges.spliterator();
  }

  @Override
  public Stream<Edge> stream() {
    return edges.stream();
  }

  @Override
  public Stream<Edge> parallelStream() {
    return edges.parallelStream();
  }

  @Override
  public void forEach(final Consumer<? super Edge> action) {
    edges.forEach(action);
  }
}
