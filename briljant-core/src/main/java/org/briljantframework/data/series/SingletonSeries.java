/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Isak Karlsson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.briljantframework.data.series;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.briljantframework.data.Is;
import org.briljantframework.data.index.Index;
import org.briljantframework.data.index.RangeIndex;

/**
 * Representing vectors of a single (unique) value repeated n (0 <= n) times.
 * 
 * @author Isak Karlsson
 */
final class SingletonSeries extends AbstractSeries {

  private static final Series EMPTY = new SingletonSeries(null, 0);
  private final Object element;
  private final Type type;
  private final int elementSize;
  private final Index index;

  SingletonSeries(Object element, int elementSize) {
    this(new RangeIndex(0, elementSize), elementSize, element);
  }

  private SingletonSeries(Index index, int elementSize, Object element) {
    this.index = Objects.requireNonNull(index);
    this.element = element;
    this.type = Types.from(element != null ? element.getClass() : Object.class);
    this.elementSize = elementSize;
  }

  private SingletonSeries(Index index, Object element) {
    this(index, 1, element);
  }

  public static Series empty() {
    return EMPTY;
  }

  @Override
  public <T> Series sortBy(Class<T> cls, Comparator<? super T> cmp) {
    return reindex(index());
  }

  @Override
  public Index index() {
    return index;
  }

  @Override
  public Object get(Object key) {
    if (!index().contains(key)) {
      throw new NoSuchElementException();
    }
    return element;
  }

  @Override
  public void set(Object key, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends Comparable<T>> Series sortBy(Class<T> cls) {
    return reindex(index());
  }

  @Override
  public boolean hasNA() {
    return Is.NA(element);
  }

  // @Override
  // protected int getIntElement(int i) {
  // Check.validIndex(i, size());
  // Number val = Convert.to(Number.class, element);
  // return Is.NA(val) ? Na.INT : val.intValue();
  // }
  //
  // @Override
  // protected double getDoubleElement(int i) {
  // Check.validIndex(i, size());
  // Number val = Convert.to(Number.class, element);
  // return Is.NA(val) ? Na.DOUBLE : val.doubleValue();
  // }
  //
  // @Override
  // protected <T> T getElement(Class<T> cls, int index) {
  // Check.validIndex(index, size());
  // return Convert.to(cls, element);
  // }

  @Override
  public Series reindex(Index index) {
    return new SingletonSeries(index, elementSize, element);
  }

  @Override
  public Builder newCopyBuilder() {
    return newBuilder().setAll(this);
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public int size() {
    return elementSize;
  }
}
