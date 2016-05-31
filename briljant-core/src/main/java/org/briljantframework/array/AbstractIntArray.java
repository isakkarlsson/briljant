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
package org.briljantframework.array;

import java.util.*;
import java.util.Arrays;
import java.util.function.*;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.complex.Complex;
import org.briljantframework.Check;
import org.briljantframework.array.api.ArrayBackend;
import org.briljantframework.function.IntBiPredicate;
import org.briljantframework.util.primitive.IntList;
import org.briljantframework.util.sort.QuickSort;

import net.mintern.primitive.comparators.IntComparator;

/**
 * This class provides a skeletal implementation of an int array.
 * 
 * @author Isak Karlsson
 */
public abstract class AbstractIntArray extends AbstractBaseArray<IntArray> implements IntArray {

  protected AbstractIntArray(ArrayBackend bj, int[] shape) {
    super(bj, shape);
  }

  protected AbstractIntArray(ArrayBackend bj, int offset, int[] shape, int[] stride) {
    super(bj, offset, shape, stride);
  }

  @Override
  public void swap(int a, int b) {
    int tmp = get(a);
    set(a, get(b));
    set(b, tmp);
  }

  @Override
  public void set(int toIndex, IntArray from, int fromIndex) {
    set(toIndex, from.get(fromIndex));
  }

  @Override
  public void set(int toRow, int toColumn, IntArray from, int fromRow, int fromColumn) {
    set(toRow, toColumn, from.get(fromRow, fromColumn));
  }

  @Override
  public void set(int[] toIndex, IntArray from, int[] fromIndex) {
    set(toIndex, from.get(fromIndex));
  }

  @Override
  public void set(int[] toIndex, IntArray from, int fromIndex) {
    set(toIndex, from.get(fromIndex));
  }

  @Override
  public void set(int toIndex, IntArray from, int[] fromIndex) {
    set(toIndex, from.get(fromIndex));
  }

  @Override
  public DoubleArray doubleArray() {
    return new AsDoubleArray(getArrayBackend(), getOffset(), getShape(), getStride()) {
      @Override
      protected double getElement(int i) {
        return AbstractIntArray.this.getElement(i);
      }

      @Override
      protected void setElement(int i, double value) {
        AbstractIntArray.this.setElement(i, (int) value);
      }

      @Override
      protected int elementSize() {
        return AbstractIntArray.this.elementSize();
      }
    };
  }

  @Override
  public IntArray intArray() {
    return this;
  }

  @Override
  public LongArray longArray() {
    return new AsLongArray(getArrayBackend(), getOffset(), getShape(), getStride()) {
      @Override
      public void setElement(int index, long value) {
        AbstractIntArray.this.setElement(index, (int) value);
      }

      @Override
      public long getElement(int index) {
        return AbstractIntArray.this.getElement(index);
      }

      @Override
      protected int elementSize() {
        return AbstractIntArray.this.elementSize();
      }
    };
  }

  @Override
  public ComplexArray complexArray() {
    return new AsComplexArray(getArrayBackend(), getOffset(), getShape(), getStride()) {
      @Override
      public Complex getElement(int index) {
        return Complex.valueOf(AbstractIntArray.this.getElement(index));
      }

      @Override
      public void setElement(int index, Complex value) {
        AbstractIntArray.this.setElement(index, (int) value.getReal());
      }

      @Override
      protected int elementSize() {
        return AbstractIntArray.this.elementSize();
      }
    };
  }

  @Override
  public IntArray copy() {
    IntArray matrix = newEmptyArray(getShape());
    for (int i = 0; i < size(); i++) {
      matrix.set(i, get(i));
    }
    return matrix;
  }

  @Override
  public void assign(int value) {
    for (int i = 0; i < size(); i++) {
      set(i, value);
    }
  }

  @Override
  public void assign(int[] data) {
    Check.dimension(this.size(), data.length);
    for (int i = 0; i < data.length; i++) {
      set(i, data[i]);
    }
  }

  @Override
  public void assign(IntSupplier supplier) {
    for (int i = 0; i < size(); i++) {
      set(i, supplier.getAsInt());
    }
  }

  @Override
  public void assign(IntArray array, IntUnaryOperator operator) {
    Check.dimension(this, array);
    for (int i = 0; i < size(); i++) {
      set(i, operator.applyAsInt(array.get(i)));
    }
  }

  @Override
  public void combineAssign(IntArray other, IntBinaryOperator combine) {
    org.briljantframework.array.Arrays.withBroadcast(this, other, (a, b) -> {
      Check.size(a, b);
      for (int i = 0, size = a.size(); i < size; i++) {
        a.set(i, combine.applyAsInt(a.get(i), b.get(i)));
      }
    });
  }

  @Override
  public void assign(ComplexArray other, ToIntFunction<? super Complex> function) {
    org.briljantframework.array.Arrays.withBroadcast(this, other, (a, b) -> {
      Check.size(a, b);
      for (int i = 0, size = a.size(); i < size; i++) {
        a.set(i, function.applyAsInt(b.get(i)));
      }
    });
  }

  @Override
  public void assign(DoubleArray other, DoubleToIntFunction function) {
    org.briljantframework.array.Arrays.withBroadcast(this, other, (a, b) -> {
      Check.size(a, b);
      for (int i = 0, size = a.size(); i < size; i++) {
        a.set(i, function.applyAsInt(b.get(i)));
      }
    });
  }

  @Override
  public void assign(LongArray other, LongToIntFunction function) {
    org.briljantframework.array.Arrays.withBroadcast(this, other, (a, b) -> {
      Check.size(a, b);
      for (int i = 0, size = a.size(); i < size; i++) {
        a.set(i, function.applyAsInt(b.get(i)));
      }
    });
  }

  @Override
  public void assign(BooleanArray other, ToIntFunction<Boolean> function) {
    org.briljantframework.array.Arrays.withBroadcast(this, other, (a, b) -> {
      Check.size(a, b);
      for (int i = 0, size = a.size(); i < size; i++) {
        a.set(i, function.applyAsInt(b.get(i)));
      }
    });
  }

  @Override
  public void apply(IntUnaryOperator operator) {
    for (int i = 0; i < size(); i++) {
      set(i, operator.applyAsInt(get(i)));
    }
  }

  @Override
  public IntArray map(IntUnaryOperator operator) {
    IntArray mat = newEmptyArray(getShape());
    for (int i = 0; i < size(); i++) {
      mat.set(i, operator.applyAsInt(get(i)));
    }
    return mat;
  }

  @Override
  public LongArray mapToLong(IntToLongFunction function) {
    LongArray matrix = getArrayBackend().getArrayFactory().newLongArray(getShape());
    for (int i = 0; i < size(); i++) {
      matrix.set(i, function.applyAsLong(get(i)));
    }
    return matrix;
  }

  @Override
  public DoubleArray mapToDouble(IntToDoubleFunction function) {
    DoubleArray matrix = getArrayBackend().getArrayFactory().newDoubleArray(getShape());
    for (int i = 0; i < size(); i++) {
      matrix.set(i, function.applyAsDouble(get(i)));
    }
    return matrix;
  }

  @Override
  public ComplexArray mapToComplex(IntFunction<Complex> function) {
    ComplexArray matrix = getArrayBackend().getArrayFactory().newComplexArray();
    for (int i = 0; i < size(); i++) {
      matrix.set(i, function.apply(get(i)));
    }
    return matrix;
  }

  @Override
  public <U> Array<U> mapToObj(IntFunction<? extends U> function) {
    Array<U> array = getArrayBackend().getArrayFactory().newArray(getShape());
    for (int i = 0; i < size(); i++) {
      array.set(i, function.apply(get(i)));
    }
    return array;
  }

  @Override
  public IntArray filter(IntPredicate operator) {
    IntList builder = new IntList();
    for (int i = 0; i < size(); i++) {
      int value = get(i);
      if (operator.test(value)) {
        builder.add(value);
      }
    }
    return getArrayBackend().getArrayFactory()
        .newIntVector(Arrays.copyOf(builder.elementData, builder.size()));
  }

  @Override
  public BooleanArray where(IntPredicate predicate) {
    BooleanArray bits = getArrayBackend().getArrayFactory().newBooleanArray(getShape());
    for (int i = 0; i < size(); i++) {
      bits.set(i, predicate.test(get(i)));
    }
    return bits;
  }

  @Override
  public BooleanArray where(IntArray other, IntBiPredicate predicate) {
    return org.briljantframework.array.Arrays.broadcast(this, other, (a, b) -> {
      BooleanArray out = getArrayBackend().getArrayFactory().newBooleanArray(a.getShape());
      for (int i = 0, size = a.size(); i < size; i++) {
        out.set(i, predicate.test(a.get(i), b.get(i)));
      }
      return out;
    });
  }

  @Override
  public void forEachPrimitive(IntConsumer consumer) {
    for (int i = 0; i < size(); i++) {
      consumer.accept(get(i));
    }
  }

  @Override
  public int reduce(int identity, IntBinaryOperator reduce) {
    return reduce(identity, reduce, IntUnaryOperator.identity());
  }

  @Override
  public int reduce(int identity, IntBinaryOperator reduce, IntUnaryOperator map) {
    for (int i = 0, size = size(); i < size; i++) {
      identity = reduce.applyAsInt(map.applyAsInt(get(i)), identity);
    }
    return identity;
  }

  @Override
  public IntArray reduceVectors(int dim, ToIntFunction<? super IntArray> accumulator) {
    Check.argument(dim < dims(), INVALID_DIMENSION, dim, dims());
    IntArray reduced = newEmptyArray(ArrayUtils.remove(getShape(), dim));
    int vectors = vectors(dim);
    for (int i = 0; i < vectors; i++) {
      int value = accumulator.applyAsInt(getVector(dim, i));
      reduced.set(i, value);
    }
    return reduced;
  }

  @Override
  public final void set(int index, int value) {
    Check.index(index, size());
    setElement(StrideUtils.index(index, getOffset(), stride, shape), value);
  }

  @Override
  public final int get(int i, int j) {
    Check.index(i, rows(), j, columns());
    return getElement(getOffset() + i * stride(0) + j * stride(1));
  }

  @Override
  public final void set(int i, int j, int value) {
    Check.index(i, rows(), j, columns());
    setElement(getOffset() + i * stride(0) + j * stride(1), value);
  }

  public final void set(int[] ix, int value) {
    Check.index(ix, shape);
    setElement(StrideUtils.index(ix, getOffset(), stride), value);
  }

  public final int get(int... ix) {
    Check.index(ix, shape);
    return getElement(StrideUtils.index(ix, getOffset(), stride));
  }

  @Override
  public final int get(int index) {
    Check.index(index, size());
    return getElement(StrideUtils.index(index, getOffset(), stride, shape));
  }

  @Override
  public IntStream intStream() {
    PrimitiveIterator.OfInt ofInt = new PrimitiveIterator.OfInt() {
      private int current = 0;

      @Override
      public int nextInt() {
        return get(current++);
      }

      @Override
      public boolean hasNext() {
        return current < size();
      }


    };
    Spliterator.OfInt spliterator = Spliterators.spliterator(ofInt, size(), Spliterator.SIZED);
    return StreamSupport.intStream(spliterator, false);
  }

  @Override
  public List<Integer> asList() {
    return new IntListView();
  }

  @Override
  public Array<Integer> asArray() {
    return new AsArray<Integer>(this) {

      @Override
      protected void setElement(int i, Integer value) {
        AbstractIntArray.this.setElement(i, value);
      }

      @Override
      protected Integer getElement(int i) {
        return AbstractIntArray.this.getElement(i);
      }

      @Override
      protected int elementSize() {
        return AbstractIntArray.this.elementSize();
      }
    };
  }

  @Override
  public void sort() {
    sort(Integer::compare);
  }

  @Override
  public void sort(IntComparator cmp) {
    QuickSort.quickSort(0, size(), (left, right) -> cmp.compare(get(left), get(right)), this);
  }

  @Override
  public IntArray times(IntArray other) {
    Check.size(this, other);
    IntArray m = newEmptyArray(getShape());
    for (int i = 0; i < size(); i++) {
      m.set(i, get(i) * other.get(i));
    }
    return m;
  }

  @Override
  public IntArray times(int scalar) {
    IntArray m = newEmptyArray(getShape());
    for (int i = 0; i < size(); i++) {
      m.set(i, get(i) * scalar);
    }
    return m;
  }

  @Override
  public IntArray plus(IntArray other) {
    Check.size(this, other);
    IntArray m = newEmptyArray(getShape());
    for (int i = 0; i < size(); i++) {
      m.set(i, get(i) + other.get(i));
    }
    return m;
  }

  @Override
  public IntArray plus(int scalar) {
    IntArray m = newEmptyArray(getShape());
    for (int i = 0; i < size(); i++) {
      m.set(i, get(i) + scalar);
    }
    return m;
  }

  @Override
  public void plusAssign(IntArray other) {
    combineAssign(other, Integer::sum);
  }

  @Override
  public void plusAssign(int scalar) {
    apply(i -> i + scalar);
  }

  @Override
  public IntArray minus(IntArray other) {
    Check.size(this, other);
    IntArray out = newEmptyArray(getShape());
    for (int i = 0; i < size(); i++) {
      out.set(i, get(i) - other.get(i));
    }
    return out;
  }

  @Override
  public IntArray minus(int scalar) {
    return plus(-scalar);
  }

  @Override
  public void minusAssign(IntArray other) {
    combineAssign(other, (a, b) -> a - b);
  }

  @Override
  public void minusAssign(int scalar) {
    apply(i -> i - scalar);
  }

  @Override
  public IntArray reverseMinus(int scalar) {
    IntArray out = newEmptyArray(getShape());
    for (int i = 0; i < size(); i++) {
      out.set(i, scalar - get(i));
    }
    return out;
  }

  @Override
  public void reverseMinusAssign(int scalar) {
    apply(i -> scalar - i);
  }

  @Override
  public IntArray div(IntArray other) {
    Check.size(this, other);
    IntArray out = newEmptyArray(getShape());
    for (int i = 0; i < size(); i++) {
      out.set(i, get(i) / other.get(i));
    }
    return out;
  }

  @Override
  public IntArray div(int other) {
    IntArray m = newEmptyArray(getShape());
    for (int i = 0; i < size(); i++) {
      m.set(i, get(i) / other);
    }
    return m;
  }

  @Override
  public void divAssign(IntArray other) {
    combineAssign(other, (a, b) -> a / b);
  }

  @Override
  public void divAssign(int other) {
    apply(i -> i / other);
  }

  @Override
  public IntArray reverseDiv(int other) {
    IntArray matrix = newEmptyArray(getShape());
    for (int i = 0; i < size(); i++) {
      matrix.set(i, other / get(i));
    }
    return matrix;
  }

  @Override
  public void reverseDivAssign(int other) {
    apply(i -> other / i);
  }

  @Override
  public IntArray negate() {
    IntArray n = newEmptyArray(getShape());
    for (int i = 0; i < size(); i++) {
      n.set(i, -get(i));
    }
    return n;
  }

  protected abstract int getElement(int i);

  protected abstract void setElement(int i, int value);

  @Override
  public int hashCode() {
    int result = 1;
    for (int i = 0; i < size(); i++) {
      int bits = get(i);
      result = 31 * result + bits;
    }

    return Objects.hash(getShape(), result);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof IntArray) {
      IntArray mat = (IntArray) obj;
      boolean equalShape;
      // This saves one array copy
      if (mat instanceof AbstractBaseArray) {
        equalShape = Arrays.equals(shape, ((AbstractBaseArray) mat).shape);
      } else {
        equalShape = Arrays.equals(shape, mat.getShape());
      }
      if (!equalShape) {
        return false;
      }
      for (int i = 0; i < size(); i++) {
        if (get(i) != mat.get(i)) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return ArrayPrinter.toString(this);
  }

  @Override
  public Iterator<Integer> iterator() {
    return asList().iterator();
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public boolean contains(Object o) {
    return asList().contains(o);
  }

  @Override
  public Object[] toArray() {
    Object[] data = new Object[size()];
    for (int i = 0; i < size(); i++) {
      data[i] = get(i);
    }
    return data;
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return asList().toArray(a);
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public final boolean add(Integer integer) {
    throw new UnsupportedOperationException();
  }

  @Override
  public final boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public final boolean containsAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public final boolean addAll(Collection<? extends Integer> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public final boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  private class IntListView extends AbstractList<Integer> {

    @Override
    public Integer get(int i) {
      return AbstractIntArray.this.get(i);
    }

    @Override
    public Integer set(int i, Integer value) {
      int old = AbstractIntArray.this.get(i);
      AbstractIntArray.this.set(i, value);
      return old;
    }

    @Override
    public int size() {
      return AbstractIntArray.this.size();
    }
  }
}
