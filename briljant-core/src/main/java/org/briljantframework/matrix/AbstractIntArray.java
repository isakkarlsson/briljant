package org.briljantframework.matrix;

import com.carrotsearch.hppc.IntArrayList;

import org.briljantframework.Check;
import org.briljantframework.complex.Complex;
import org.briljantframework.exceptions.NonConformantException;
import org.briljantframework.function.IntBiPredicate;
import org.briljantframework.function.ToIntObjIntBiFunction;
import org.briljantframework.matrix.api.ArrayFactory;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.StringJoiner;
import java.util.function.DoubleToIntFunction;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.LongToIntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static org.briljantframework.matrix.Indexer.columnMajor;
import static org.briljantframework.matrix.Indexer.rowMajor;

/**
 * @author Isak Karlsson
 */
public abstract class AbstractIntArray extends AbstractArray<IntArray> implements IntArray {

  protected AbstractIntArray(ArrayFactory bj, int size) {
    super(bj, size);
  }

  public AbstractIntArray(ArrayFactory bj, int... shape) {
    super(bj, shape);
  }

  public AbstractIntArray(ArrayFactory bj, int offset, int[] shape, int[] stride) {
    super(bj, offset, shape, stride);
  }

  @Override
  public IntArray assign(IntArray o) {
    Check.equalShape(this, o);
    for (int i = 0; i < size(); i++) {
      set(i, o.get(i));
    }
    return this;
  }

  @Override
  public void swap(int a, int b) {
    int tmp = get(a);
    set(a, get(b));
    set(b, tmp);
  }

  public final void set(int[] ix, int value) {
    Check.argument(ix.length == dims());
    setElement(Indexer.columnMajorStride(getOffset(), ix, getStride()), value);
  }

  public final int get(int... ix) {
    Check.argument(ix.length == dims());
    return getElement(Indexer.columnMajorStride(getOffset(), ix, getStride()));
  }

  @Override
  public final void set(int i, int j, int value) {
    Check.argument(isMatrix());
    setElement(getOffset() + i * stride(0) + j * stride(1), value);
  }

  @Override
  public final int get(int i, int j) {
    Check.argument(isMatrix());
    return getElement(getOffset() + i * stride(0) + j * stride(1));
  }

  @Override
  public final void set(int index, int value) {
    setElement(index * stride(0) + getOffset(), value);
  }

  @Override
  public final int get(int index) {
    return getElement(index * stride(0) + getOffset());
  }

  protected abstract void setElement(int i, int value);

  protected abstract int getElement(int i);

  @Override
  public void set(int toIndex, IntArray from, int fromIndex) {
    set(toIndex, from.get(fromIndex));
  }

  @Override
  public void set(int toRow, int toColumn, IntArray from, int fromRow, int fromColumn) {
    set(toRow, toColumn, from.get(fromRow, fromColumn));
  }

  @Override
  public int compare(int a, int b) {
    return Integer.compare(get(a), get(b));
  }

  @Override
  public void setRow(int index, IntArray row) {
    Check.size(columns(), row);
    for (int j = 0; j < columns(); j++) {
      set(index, j, row.get(j));
    }
  }

  @Override
  public void setColumn(int index, IntArray column) {
    Check.size(rows(), column.size());
    for (int i = 0; i < rows(); i++) {
      set(i, index, column.get(i));
    }
  }

  @Override
  public DoubleArray asDoubleMatrix() {
    return new AsDoubleArray(
        getMatrixFactory(), getOffset(), getShape().clone(), getStride().clone()) {
      @Override
      protected double getElement(int i) {
        return AbstractIntArray.this.getElement(i);
      }

      @Override
      protected void setElement(int i, double value) {
        AbstractIntArray.this.setElement(i, (int) value);
      }
    };
  }

  @Override
  public IntArray assign(int value) {
    for (int i = 0; i < size(); i++) {
      set(i, value);
    }
    return this;
  }

  @Override
  public IntArray assign(int[] data) {
    Check.size(this.size(), data.length);
    for (int i = 0; i < data.length; i++) {
      set(i, data[i]);
    }
    return this;
  }

  @Override
  public IntArray assign(IntSupplier supplier) {
    for (int i = 0; i < size(); i++) {
      set(i, supplier.getAsInt());
    }
    return this;
  }

  @Override
  public IntArray assign(IntArray matrix, IntUnaryOperator operator) {
    Check.equalShape(this, matrix);
    for (int i = 0; i < size(); i++) {
      set(i, operator.applyAsInt(matrix.get(i)));
    }
    return this;
  }

  @Override
  public IntArray assign(IntArray matrix, IntBinaryOperator combine) {
    Check.equalShape(this, matrix);
    for (int i = 0; i < size(); i++) {
      set(i, combine.applyAsInt(get(i), matrix.get(i)));
    }
    return this;
  }

  @Override
  public IntArray assign(ComplexArray matrix, ToIntFunction<? super Complex> function) {
    Check.size(this, matrix);
    for (int i = 0; i < size(); i++) {
      set(i, function.applyAsInt(matrix.get(i)));
    }
    return this;
  }

  @Override
  public IntArray asIntMatrix() {
    return this;
  }

  @Override
  public IntArray assign(DoubleArray matrix, DoubleToIntFunction function) {
    Check.size(this, matrix);
    for (int i = 0; i < matrix.size(); i++) {
      set(i, function.applyAsInt(matrix.get(i)));
    }
    return this;
  }

  @Override
  public IntArray assign(LongArray matrix, LongToIntFunction operator) {
    Check.size(this, matrix);
    for (int i = 0; i < size(); i++) {
      set(i, operator.applyAsInt(matrix.get(i)));
    }
    return this;
  }

  @Override
  public IntArray assign(BitArray matrix, ToIntObjIntBiFunction<Boolean> function) {
    Check.equalShape(this, matrix);
    for (int i = 0; i < size(); i++) {
      set(i, function.applyAsInt(matrix.get(i), get(i)));
    }
    return this;
  }

  @Override
  public IntArray update(IntUnaryOperator operator) {
    for (int i = 0; i < size(); i++) {
      set(i, operator.applyAsInt(get(i)));
    }
    return this;
  }

  @Override
  public IntArray map(IntUnaryOperator operator) {
    IntArray mat = newEmptyArray(getShape().clone());
    for (int i = 0; i < size(); i++) {
      mat.set(i, operator.applyAsInt(get(i)));
    }
    return mat;
  }

  @Override
  public LongArray mapToLong(IntToLongFunction function) {
    LongArray matrix = bj.longArray(getShape().clone());
    for (int i = 0; i < size(); i++) {
      matrix.set(i, function.applyAsLong(get(i)));
    }
    return matrix;
  }

  @Override
  public LongArray asLongMatrix() {
    return new AsLongArray(
        getMatrixFactory(), getOffset(), getShape().clone(), getStride().clone()) {
      @Override
      public long getElement(int index) {
        return AbstractIntArray.this.getElement(index);
      }

      @Override
      public void setElement(int index, long value) {
        AbstractIntArray.this.setElement(index, (int) value);
      }
    };
  }

  @Override
  public DoubleArray mapToDouble(IntToDoubleFunction function) {
    DoubleArray matrix = bj.doubleArray(getShape().clone());
    for (int i = 0; i < size(); i++) {
      matrix.set(i, function.applyAsDouble(get(i)));
    }
    return matrix;
  }

  @Override
  public ComplexArray mapToComplex(IntFunction<Complex> function) {
    ComplexArray matrix = bj.complexArray();
    for (int i = 0; i < size(); i++) {
      matrix.set(i, function.apply(get(i)));
    }
    return matrix;
  }

  @Override
  public IntArray filter(IntPredicate operator) {
    Builder builder = new Builder();
    for (int i = 0; i < size(); i++) {
      int value = get(i);
      if (operator.test(value)) {
        builder.add(value);
      }
    }
    return builder.build();
  }

  @Override
  public BitArray satisfies(IntPredicate predicate) {
    BitArray bits = bj.booleanArray();
    for (int i = 0; i < size(); i++) {
      bits.set(i, predicate.test(get(i)));
    }
    return bits;
  }

  @Override
  public BitArray satisfies(IntArray matrix, IntBiPredicate predicate) {
    Check.equalShape(this, matrix);
    BitArray bits = bj.booleanArray();
    for (int i = 0; i < size(); i++) {
      bits.set(i, predicate.test(get(i), matrix.get(i)));
    }
    return bits;
  }

  @Override
  public void forEach(IntConsumer consumer) {
    for (int i = 0; i < size(); i++) {
      consumer.accept(get(i));
    }
  }

  @Override
  public BitArray asBitMatrix() {
    return new AsBitArray(
        getMatrixFactory(), getOffset(), getShape().clone(), getStride().clone()) {

      @Override
      public void setElement(int index, boolean value) {
        AbstractIntArray.this.set(index, value ? 1 : 0);
      }

      @Override
      public boolean getElement(int index) {
        return AbstractIntArray.this.getElement(index) == 1;
      }

    };
  }

  @Override
  public int reduce(int identity, IntBinaryOperator reduce) {
    return reduce(identity, reduce, IntUnaryOperator.identity());
  }

  @Override
  public int reduce(int identity, IntBinaryOperator reduce, IntUnaryOperator map) {
    for (int i = 0; i < size(); i++) {
      identity = reduce.applyAsInt(map.applyAsInt(get(i)), identity);
    }
    return identity;
  }

  @Override
  public IntArray reduceColumns(ToIntFunction<? super IntArray> reduce) {
    IntArray mat = newEmptyArray(1, columns());
    for (int i = 0; i < columns(); i++) {
      mat.set(i, reduce.applyAsInt(getColumn(i)));
    }
    return mat;
  }

  @Override
  public IntArray reduceRows(ToIntFunction<? super IntArray> reduce) {
    IntArray mat = newEmptyArray(rows(), 1);
    for (int i = 0; i < rows(); i++) {
      mat.set(i, reduce.applyAsInt(getRow(i)));
    }
    return mat;
  }

  @Override
  public int hashCode() {
    int result = 1;
    for (int i = 0; i < size(); i++) {
      long bits = get(i);
      result = 31 * result + (int) (bits ^ (bits >>> 32));
    }

    return Objects.hash(rows(), columns(), result);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof IntArray) {
      IntArray mat = (IntArray) obj;
      if (!mat.hasEqualShape(this)) {
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
  public ComplexArray asComplexMatrix() {
    return new AsComplexArray(
        getMatrixFactory(), getOffset(), getShape().clone(), getStride().clone()) {
      @Override
      public void setElement(int index, Complex value) {
        AbstractIntArray.this.setElement(index, value.intValue());
      }

      @Override
      public Complex getElement(int index) {
        return Complex.valueOf(AbstractIntArray.this.getElement(index));
      }

    };
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
//    try {
//      MatrixPrinter.print(builder, this);
//    } catch (IOException e) {
//      return getClass().getSimpleName();
//    }
    print(builder, this);
    return builder.toString();
  }

  void print(StringBuilder builder, IntArray arr) {
    if (arr.dims() == 1) {
      StringJoiner j = new StringJoiner(",", "[", "]");
      for (int i = 0; i < arr.size(); i++) {
        j.add(arr.get(i) + "");
      }
//      System.out.print(j.toString());
      builder.append(j.toString());
    } else {
      int len = arr.getShape()[0];
      for (int i = 0; i < len; i++) {
        print(builder, arr.select(i));
        builder.append("\n");
//        System.out.println();
      }
    }
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
    public Iterator<Integer> iterator() {
      return new Iterator<Integer>() {
        private int index = 0;

        @Override
        public boolean hasNext() {
          return index < size();
        }

        @Override
        public Integer next() {
          return get(index++);
        }
      };
    }

    @Override
    public int size() {
      return AbstractIntArray.this.size();
    }
  }

  @Override
  public IntArray copy() {
    IntArray matrix = newEmptyArray(rows(), columns());
    for (int i = 0; i < size(); i++) {
      matrix.set(i, get(i));
    }
    return matrix;
  }

  @Override
  public void addTo(int index, int value) {
    set(index, get(index) + value);
  }

  @Override
  public void addTo(int i, int j, int value) {
    set(i, j, get(i, j) + value);
  }

  @Override
  public void update(int index, IntUnaryOperator operator) {
    set(index, operator.applyAsInt(get(index)));
  }

  @Override
  public void update(int i, int j, IntUnaryOperator operator) {
    set(i, j, operator.applyAsInt(get(i, j)));
  }

  @Override
  public IntStream stream() {
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
  public List<Integer> flat() {
    return new IntListView();
  }

  @Override
  public IntArray mmul(IntArray other) {
    return mmul(1, other);
  }

  @Override
  public IntArray slice(Collection<Integer> rows, Collection<Integer> columns) {
    IntArray m = newEmptyArray(rows.size(), columns.size());
    int i = 0;
    for (int row : rows) {
      int j = 0;
      for (int column : columns) {
        m.set(i, j++, get(row, column));
      }
      i++;
    }
    return m;
  }

  @Override
  public IntArray mmul(int alpha, IntArray other) {
    return mmul(alpha, Op.KEEP, other, Op.KEEP);
  }

  @Override
  public IntArray mmul(Op a, IntArray other, Op b) {
    return mmul(1, a, other, b);
  }

  @Override
  public IntArray mmul(int alpha, Op a, IntArray other, Op b) {
    int thisRows = rows();
    int thisCols = columns();
    if (a == Op.TRANSPOSE) {
      thisRows = columns();
      thisCols = rows();
    }
    int otherRows = other.rows();
    int otherColumns = other.columns();
    if (b == Op.TRANSPOSE) {
      otherRows = other.columns();
      otherColumns = other.rows();
    }

    if (thisCols != otherRows) {
      throw new NonConformantException(thisRows, thisCols, otherRows, otherColumns);
    }

    IntArray result = newEmptyArray(thisRows, otherColumns);
    for (int row = 0; row < thisRows; row++) {
      for (int col = 0; col < otherColumns; col++) {
        int sum = 0;
        for (int k = 0; k < thisCols; k++) {
          int thisIndex =
              a == Op.TRANSPOSE ? rowMajor(row, k, thisRows, thisCols) : columnMajor(0, row, k,
                                                                                     thisRows,
                                                                                     thisCols);
          int otherIndex =
              b == Op.TRANSPOSE ? rowMajor(k, col, otherRows, otherColumns) : columnMajor(0, k, col,
                                                                                          otherRows,
                                                                                          otherColumns);
          sum += get(thisIndex) * other.get(otherIndex);
        }
        result.set(row, col, alpha * sum);
      }
    }
    return result;
  }

  @Override
  public IntArray mul(IntArray other) {
    return mul(1, other, 1);
  }

  @Override
  public IntArray mul(int alpha, IntArray other, int beta) {
    Check.size(this, other);
    IntArray m = newEmptyArray(rows(), columns());
    for (int j = 0; j < columns(); j++) {
      for (int i = 0; i < rows(); i++) {
        m.set(i, j, alpha * get(i, j) * other.get(i, j) * beta);
      }
    }
    return m;
  }

  @Override
  public IntArray mul(int scalar) {
    IntArray m = newEmptyArray(rows(), columns());
    for (int j = 0; j < columns(); j++) {
      for (int i = 0; i < rows(); i++) {
        m.set(i, j, get(i, j) * scalar);
      }
    }
    return m;
  }

  @Override
  public IntArray add(IntArray other) {
    return add(1, other, 1);
  }

  @Override
  public IntArray add(int scalar) {
    IntArray matrix = newEmptyArray(rows(), columns());
    for (int j = 0; j < columns(); j++) {
      for (int i = 0; i < rows(); i++) {
        matrix.set(i, j, get(i, j) + scalar);
      }
    }
    return matrix;
  }

  @Override
  public IntArray slice(Collection<Integer> indexes) {
    Builder builder = new Builder();
    indexes.forEach(index -> builder.add(get(index)));
    return builder.build();
  }

  @Override
  public IntArray add(int alpha, IntArray other, int beta) {
    Check.size(this, other);
    IntArray matrix = newEmptyArray(rows(), columns());
    for (int j = 0; j < columns(); j++) {
      for (int i = 0; i < rows(); i++) {
        matrix.set(i, j, alpha * get(i, j) + other.get(i, j) * beta);
      }
    }
    return matrix;
  }

  @Override
  public IntArray sub(IntArray other) {
    return sub(1, other, 1);
  }

  @Override
  public IntArray sub(int scalar) {
    return add(-scalar);
  }

  @Override
  public IntArray sub(int alpha, IntArray other, int beta) {
    Check.size(this, other);
    IntArray matrix = newEmptyArray(rows(), columns());
    for (int j = 0; j < columns(); j++) {
      for (int i = 0; i < rows(); i++) {
        matrix.set(i, j, alpha * get(i, j) - other.get(i, j) * beta);
      }
    }
    return matrix;
  }

  @Override
  public IntArray slice(Range rows, Range columns) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IntArray rsub(int scalar) {
    IntArray matrix = newEmptyArray(rows(), columns());
    for (int j = 0; j < columns(); j++) {
      for (int i = 0; i < rows(); i++) {
        matrix.set(i, j, scalar - get(i, j));
      }
    }
    return matrix;
  }

  @Override
  public IntArray div(IntArray other) {
    Check.size(this, other);
    IntArray matrix = newEmptyArray(rows(), columns());
    for (int j = 0; j < columns(); j++) {
      for (int i = 0; i < rows(); i++) {
        matrix.set(i, j, get(i, j) / other.get(i, j));
      }
    }
    return matrix;
  }

  @Override
  public IntArray div(int other) {
    IntArray m = newEmptyArray(rows(), columns());
    for (int i = 0; i < size(); i++) {
      m.set(i, get(i) / other);
    }
    return m;
  }

  @Override
  public IntArray slice(Range range) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IntArray rdiv(int other) {
    IntArray matrix = newEmptyArray(rows(), columns());
    for (int i = 0; i < size(); i++) {
      matrix.set(i, other / get(i));
    }
    return matrix;
  }

  @Override
  public IntArray negate() {
    IntArray n = newEmptyArray(rows(), columns());
    for (int i = 0; i < size(); i++) {
      n.set(i, -get(i));
    }
    return n;
  }

  @Override
  public int[] data() {
    int[] array = new int[size()];
    for (int i = 0; i < size(); i++) {
      array[i] = get(i);
    }
    return array;
  }


  @Override
  public IntArray slice(BitArray bits) {
    Check.equalShape(this, bits);
    Builder builder = new Builder();
    for (int i = 0; i < size(); i++) {
      if (bits.get(i)) {
        builder.add(get(i));
      }
    }
    return builder.build();
  }

  private class Builder {

    private IntArrayList buffer = new IntArrayList();

    public void add(int value) {
      buffer.add(value);
    }

    public IntArray build() {
      return bj.array(buffer.toArray());
    }
  }

}
