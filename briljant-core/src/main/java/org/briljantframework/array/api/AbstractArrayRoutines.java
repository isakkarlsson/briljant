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
package org.briljantframework.array.api;


import static org.briljantframework.array.StrideUtils.columnMajor;
import static org.briljantframework.array.StrideUtils.rowMajor;

import java.util.Comparator;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;
import org.briljantframework.Check;
import org.briljantframework.array.*;
import org.briljantframework.data.statistics.FastStatistics;
import org.briljantframework.exceptions.MultiDimensionMismatchException;
import org.briljantframework.util.complex.MutableComplex;

/**
 * Base array routines implemented in Java.
 *
 * @author Isak Karlsson
 */
public class AbstractArrayRoutines implements ArrayRoutines {

  protected static final String VECTOR_REQUIRED = "1d-array required";
  private static final double LOG_2 = Math.log(2);
  private static final double EPS = 1e-10;
  protected final ArrayBackend backend;

  protected AbstractArrayRoutines(ArrayBackend backend) {
    this.backend = backend;
  }

  @Override
  public double mean(DoubleArray x) {
    Check.argument(x.isVector(), VECTOR_REQUIRED);
    return sum(x) / x.size();
  }

  @Override
  public DoubleArray mean(int dim, DoubleArray x) {
    return x.reduceVectors(dim, this::mean);
  }

  @Override
  public double var(DoubleArray x) {
    Check.argument(x.isVector(), VECTOR_REQUIRED);
    FastStatistics s = new FastStatistics();
    x.forEachDouble(s::addValue);
    return s.getVariance();
  }

  @Override
  public DoubleArray var(int dim, DoubleArray x) {
    return x.reduceVectors(dim, this::var);
  }

  @Override
  public double std(DoubleArray x) {
    Check.argument(x.isVector(), VECTOR_REQUIRED);
    return Math.sqrt(var(x));
  }

  @Override
  public DoubleArray std(int dim, DoubleArray x) {
    return x.reduceVectors(dim, this::std);
  }

  @Override
  public double min(DoubleArray x) {
    Check.argument(x.isVector(), VECTOR_REQUIRED);
    return x.reduce(Double.POSITIVE_INFINITY, Math::min);
  }

  @Override
  public int min(IntArray x) {
    Check.argument(x.isVector(), VECTOR_REQUIRED);
    return x.reduce(Integer.MAX_VALUE, Math::min);
  }

  @Override
  public long min(LongArray x) {
    Check.argument(x.isVector(), VECTOR_REQUIRED);
    return x.reduce(Long.MAX_VALUE, Math::min);
  }

  @Override
  public <T extends Comparable<T>> T min(Array<T> x) {
    Check.argument(x.isVector(), VECTOR_REQUIRED);
    return min(x, Comparable::compareTo);
  }

  @Override
  public <T> T min(Array<T> x, Comparator<T> cmp) {
    Check.argument(x.isVector(), VECTOR_REQUIRED);
    if (x.size() < 1) {
      return null;
    }
    return x.reduce(x.get(0), (o, n) -> {
      if (cmp.compare(o, n) < 0) {
        return o;
      } else {
        return n;
      }
    });
  }

  @Override
  public DoubleArray min(int dim, DoubleArray x) {
    return x.reduceVectors(dim, this::min);
  }

  @Override
  public IntArray min(int dim, IntArray x) {
    return x.reduceVectors(dim, this::min);
  }

  @Override
  public LongArray min(int dim, LongArray x) {
    return x.reduceVector(dim, this::min);
  }

  @Override
  public <T extends Comparable<T>> Array<T> min(int dim, Array<T> x) {
    return x.reduceVector(dim, this::min);
  }

  @Override
  public <T> Array<T> min(int dim, Array<T> x, Comparator<T> cmp) {
    return x.reduceVector(dim, v -> this.min(v, cmp));
  }

  @Override
  public double max(DoubleArray x) {
    Check.argument(x.isVector(), VECTOR_REQUIRED);
    return x.reduce(Double.NEGATIVE_INFINITY, Math::max);
  }

  @Override
  public int max(IntArray x) {
    Check.argument(x.isVector(), VECTOR_REQUIRED);
    return x.reduce(Integer.MIN_VALUE, Math::max);
  }

  @Override
  public long max(LongArray x) {
    Check.argument(x.isVector(), VECTOR_REQUIRED);
    return x.reduce(Long.MIN_VALUE, Math::max);
  }

  @Override
  public <T extends Comparable<T>> T max(Array<T> x) {
    Check.argument(x.isVector(), VECTOR_REQUIRED);
    return max(x, Comparable::compareTo);
  }

  @Override
  public <T> T max(Array<T> x, Comparator<T> cmp) {
    Check.argument(x.isVector(), VECTOR_REQUIRED);
    if (x.size() < 1) {
      return null;
    }
    return x.reduce(x.get(0), (o, n) -> {
      if (cmp.compare(o, n) > 0) {
        return o;
      } else {
        return n;
      }
    });
  }

  @Override
  public DoubleArray max(int dim, DoubleArray x) {
    return x.reduceVectors(dim, this::max);
  }

  @Override
  public IntArray max(int dim, IntArray x) {
    return x.reduceVectors(dim, this::max);
  }

  @Override
  public LongArray max(int dim, LongArray x) {
    return x.reduceVector(dim, this::max);
  }

  @Override
  public <T extends Comparable<T>> Array<T> max(int dim, Array<T> x) {
    return x.reduceVector(dim, this::max);
  }

  @Override
  public <T> Array<T> max(int dim, Array<T> x, Comparator<T> cmp) {
    return x.reduceVector(dim, v -> this.max(v, cmp));
  }

  @Override
  public double sum(DoubleArray x) {
    return x.reduce(0, Double::sum);
  }

  @Override
  public int sum(IntArray x) {
    return x.reduce(0, Integer::sum);
  }

  @Override
  public long sum(LongArray x) {
    return x.reduce(0, Long::sum);
  }

  @Override
  public Complex sum(ComplexArray x) {
    MutableComplex sum = new MutableComplex(0);
    for (int i = 0; i < x.size(); i++) {
      sum.plus(x.get(i));
    }
    return sum.toComplex();
  }

  @Override
  public DoubleArray sum(int dim, DoubleArray x) {
    return x.reduceVectors(dim, this::sum);
  }

  @Override
  public IntArray sum(int dim, IntArray x) {
    return x.reduceVectors(dim, this::sum);
  }

  @Override
  public LongArray sum(int dim, LongArray x) {
    return x.reduceVector(dim, this::sum);
  }

  /**
   * Returns the sum along the specified dimension.
   *
   * @param dim the dimension
   * @param x the array
   * @return an array of sums
   */
  @Override
  public ComplexArray sum(int dim, ComplexArray x) {
    return x.reduceVectors(dim, this::sum);
  }

  @Override
  public double prod(DoubleArray x) {
    Check.argument(x.isVector(), VECTOR_REQUIRED);
    double prod = x.get(0);
    for (int i = 1; i < x.size(); i++) {
      prod *= x.get(i);
    }
    return prod;
  }

  @Override
  public DoubleArray prod(int dim, DoubleArray x) {
    return x.reduceVectors(dim, this::prod);
  }

  @Override
  public DoubleArray cumsum(DoubleArray x) {
    Check.argument(x.isVector(), VECTOR_REQUIRED);
    DoubleArray n = x.newEmptyArray(x.rows(), x.columns());
    double sum = 0;
    for (int i = 0; i < n.size(); i++) {
      sum += x.get(i);
      n.set(i, sum);
    }
    return n;
  }

  @Override
  public DoubleArray cumsum(int dim, DoubleArray x) {
    DoubleArray n = x.newEmptyArray(x.rows(), x.columns());
    int vectors = x.vectors(dim);
    for (int i = 0; i < vectors; i++) {
      n.setVector(dim, i, cumsum(n.getVector(dim, i)));
    }

    return n;
  }

  @Override
  public double inner(DoubleArray a, DoubleArray b) {
    Check.argument(a.isVector() && b.isVector(), VECTOR_REQUIRED);
    Check.size(a, b);
    double s = 0;
    for (int i = 0; i < a.size(); i++) {
      s += a.get(i) * b.get(i);
    }
    return s;
  }

  @Override
  public Complex inner(ComplexArray a, ComplexArray b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Complex conjugateInner(ComplexArray a, ComplexArray b) {
    return null;
  }

  @Override
  public double norm2(DoubleArray a) {
    Check.argument(a.isVector(), VECTOR_REQUIRED);
    double sum = 0;
    for (int i = 0; i < a.size(); i++) {
      double v = a.get(i);
      sum += v * v;
    }

    return Math.sqrt(sum);
  }

  @Override
  public DoubleArray norm2(int dim, DoubleArray a) {
    return a.reduceVectors(dim, this::norm2);
  }

  @Override
  public Complex norm2(ComplexArray a) {
    Check.argument(a.isVector(), VECTOR_REQUIRED);
    MutableComplex c = new MutableComplex(a.get(0).pow(2));
    for (int i = 1; i < a.size(); i++) {
      c.plus(a.get(i).pow(2));
    }
    return c.toComplex().sqrt();
  }

  @Override
  public double asum(DoubleArray a) {
    Check.argument(a.isVector(), VECTOR_REQUIRED);
    double sum = 0;
    for (int i = 0; i < a.size(); i++) {
      sum += Math.abs(a.get(i));
    }
    return sum;
  }

  @Override
  public double asum(ComplexArray a) {
    Check.argument(a.isVector(), VECTOR_REQUIRED);
    double s = 0;
    for (int i = 0; i < a.size(); i++) {
      s += a.get(i).abs();
    }
    return s;
  }

  @Override
  public int iamax(DoubleArray x) {
    Check.argument(x.isVector(), VECTOR_REQUIRED);
    int i = 0;
    double m = Math.abs(x.get(0));
    for (int j = 1; j < x.size(); j++) {
      double d = Math.abs(x.get(j));
      if (d > m) {
        i = j;
        m = d;
      }
    }
    return i;
  }

  @Override
  public int iamax(ComplexArray x) {
    Check.argument(x.isVector(), VECTOR_REQUIRED);
    throw new UnsupportedOperationException();
  }

  @Override
  public void scal(double alpha, DoubleArray x) {
    Check.argument(x.isVector(), VECTOR_REQUIRED);
    if (alpha == 1) {
      return;
    }
    final int n = x.size();
    for (int i = 0; i < n; i++) {
      x.set(i, x.get(i) * alpha);
    }
  }

  @Override
  public double trace(DoubleArray x) {
    int min = Math.min(x.rows(), x.columns());
    double sum = 0;
    for (int i = 0; i < min; i++) {
      sum += x.get(i, i);
    }
    return sum;
  }

  @Override
  public void axpy(double alpha, DoubleArray x, DoubleArray y) {
    // Check.argument(x.isVector() && y.isVector(), VECTOR_REQUIRED);
    Check.size(x, y);
    if (alpha == 0) {
      return;
    }
    int size = x.size();
    for (int i = 0; i < size; i++) {
      y.set(i, alpha * x.get(i) + y.get(i));
    }
  }

  @Override
  public void gemv(ArrayOperation transA, double alpha, DoubleArray a, DoubleArray x, double beta,
      DoubleArray y) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void ger(double alpha, DoubleArray x, DoubleArray y, DoubleArray a) {
    Check.argument(x.isVector() && y.isVector(), VECTOR_REQUIRED);
    Check.dimension(x.size(), a.rows());
    Check.dimension(y.size(), a.columns());
    for (int i = 0; i < x.size(); i++) {
      for (int j = 0; j < y.size(); j++) {
        a.set(i, j, alpha * x.get(i) * y.get(j));
      }
    }
  }

  @Override
  public void gemm(ArrayOperation transA, ArrayOperation transB, double alpha, DoubleArray a,
      DoubleArray b, double beta, DoubleArray c) {

    int thisRows = a.rows();
    int thisCols = a.columns();
    if (transA.isTranspose()) {
      thisRows = a.columns();
      thisCols = a.rows();
    }
    int otherRows = b.rows();
    int otherColumns = b.columns();
    if (transB.isTranspose()) {
      otherRows = b.columns();
      otherColumns = b.rows();
    }

    if (thisCols != otherRows) {
      throw new MultiDimensionMismatchException(thisRows, thisCols, otherRows, otherColumns);
    }
    int m = a.size(transA == ArrayOperation.KEEP ? 0 : 1);
    int n = b.size(transB == ArrayOperation.KEEP ? 1 : 0);
    int dk = a.size(transA == ArrayOperation.KEEP ? 1 : 0);
    if (m != c.size(0) || n != c.size(1)) {
      throw new IllegalArgumentException(
          String.format("a has size (%d,%d), b has size (%d,%d), c has size (%d, %d)", m, dk, dk, n,
              c.size(0), c.size(1)));
    }

    for (int row = 0; row < thisRows; row++) {
      for (int col = 0; col < otherColumns; col++) {
        double sum = 0.0;
        for (int k = 0; k < thisCols; k++) {
          int thisIndex = transA.isTranspose() ? rowMajor(row, k, thisRows, thisCols)
              : columnMajor(0, row, k, thisRows, thisCols);
          int otherIndex = transB.isTranspose() ? rowMajor(k, col, otherRows, otherColumns)
              : columnMajor(0, k, col, otherRows, otherColumns);
          sum += a.get(thisIndex) * b.get(otherIndex);
        }
        c.set(row, col, alpha * sum + beta * c.get(row, col));
      }
    }
  }

  @Override
  public <T extends BaseArray<T>> void copy(T from, T to) {
    Check.size(from, to);
    for (int i = 0; i < from.size(); i++) {
      to.setFrom(i, from, i);
    }
  }

  @Override
  public <T extends BaseArray<T>> void swap(T a, T b) {
    Check.dimension(a, b);
    T tmp = a.newEmptyArray(1);
    for (int i = 0; i < a.size(); i++) {
      tmp.setFrom(0, a, i);
      a.setFrom(i, b, i);
      b.setFrom(i, tmp, 0);
    }
  }

  @Override
  public DoubleArray plus(DoubleArray a, DoubleArray b) {
    return Arrays.broadcastCombine(a, b, (x, y) -> {
      DoubleArray out = x.newEmptyArray(x.getShape());
      for (int i = 0, size = x.size(); i < size; i++) {
        out.set(i, x.get(i) + y.get(i));
      }
      return out;
    });
  }

  @Override
  public void plusAssign(DoubleArray a, final DoubleArray out) {
    Arrays.broadcastWith(out, a, (x, y) -> {
      for (int i = 0, size = x.size(); i < size; i++) {
        x.set(i, x.get(i) + y.get(i));
      }
    });
  }

  @Override
  public DoubleArray minus(DoubleArray a, DoubleArray b) {
    return Arrays.broadcastCombine(a, b, (x, y) -> {
      DoubleArray out = x.newEmptyArray(x.getShape());
      for (int i = 0, size = x.size(); i < size; i++) {
        out.set(i, x.get(i) - y.get(i));
      }
      return out;
    });
  }

  @Override
  public void minusAssign(DoubleArray a, DoubleArray out) {
    Arrays.broadcastWith(out, a, (x, y) -> {
      for (int i = 0, size = x.size(); i < size; i++) {
        x.set(i, y.get(i) - x.get(i));
      }
    });
  }

  @Override
  public DoubleArray times(DoubleArray a, DoubleArray b) {
    return Arrays.broadcastCombine(a, b, (x, y) -> {
      DoubleArray out = x.newEmptyArray(x.getShape());
      for (int i = 0, size = x.size(); i < size; i++) {
        out.set(i, x.get(i) * y.get(i));
      }
      return out;
    });
  }

  @Override
  public void timesAssign(DoubleArray a, DoubleArray out) {
    Arrays.broadcastWith(out, a, (x, y) -> {
      for (int i = 0, size = x.size(); i < size; i++) {
        x.set(i, x.get(i) * y.get(i));
      }
    });
  }

  @Override
  public DoubleArray div(DoubleArray nominator, DoubleArray denominator) {
    return Arrays.broadcastCombine(nominator, denominator, (x, y) -> {
      DoubleArray out = x.newEmptyArray(x.getShape());
      for (int i = 0, size = x.size(); i < size; i++) {
        out.set(i, x.get(i) / y.get(i));
      }
      return out;
    });
  }

  @Override
  public void divAssign(DoubleArray nominator, DoubleArray denominatorOut) {
    Arrays.broadcastWith(denominatorOut, nominator, (x, y) -> {
      for (int i = 0, size = x.size(); i < size; i++) {
        x.set(i, y.get(i) / x.get(i));
      }
    });
  }

  @Override
  public IntArray plus(IntArray a, IntArray b) {
    return Arrays.broadcastCombine(a, b, (x, y) -> {
      IntArray out = x.newEmptyArray(x.getShape());
      for (int i = 0, size = x.size(); i < size; i++) {
        out.set(i, x.get(i) + y.get(i));
      }
      return out;
    });
  }

  @Override
  public void plusAssign(IntArray a, final IntArray out) {
    Arrays.broadcastWith(out, a, (x, y) -> {
      for (int i = 0, size = x.size(); i < size; i++) {
        x.set(i, x.get(i) + y.get(i));
      }
    });
  }

  @Override
  public IntArray minus(IntArray a, IntArray b) {
    return Arrays.broadcastCombine(a, b, (x, y) -> {
      IntArray out = x.newEmptyArray(x.getShape());
      for (int i = 0, size = x.size(); i < size; i++) {
        out.set(i, x.get(i) - y.get(i));
      }
      return out;
    });
  }

  @Override
  public void minusAssign(IntArray a, IntArray out) {
    Arrays.broadcastWith(out, a, (x, y) -> {
      for (int i = 0, size = x.size(); i < size; i++) {
        x.set(i, y.get(i) - x.get(i));
      }
    });
  }

  @Override
  public IntArray times(IntArray a, IntArray b) {
    return Arrays.broadcastCombine(a, b, (x, y) -> {
      IntArray out = x.newEmptyArray(x.getShape());
      for (int i = 0, size = x.size(); i < size; i++) {
        out.set(i, x.get(i) * y.get(i));
      }
      return out;
    });
  }

  @Override
  public void timesAssign(IntArray a, IntArray out) {
    Arrays.broadcastWith(out, a, (x, y) -> {
      for (int i = 0, size = x.size(); i < size; i++) {
        x.set(i, x.get(i) * y.get(i));
      }
    });
  }

  @Override
  public IntArray div(IntArray nominator, IntArray denominator) {
    return Arrays.broadcastCombine(nominator, denominator, (x, y) -> {
      IntArray out = x.newEmptyArray(x.getShape());
      for (int i = 0, size = x.size(); i < size; i++) {
        out.set(i, x.get(i) / y.get(i));
      }
      return out;
    });
  }

  @Override
  public void divAssign(IntArray nominator, IntArray denominatorOut) {
    Arrays.broadcastWith(denominatorOut, nominator, (x, y) -> {
      for (int i = 0, size = x.size(); i < size; i++) {
        x.set(i, y.get(i) / x.get(i));
      }
    });
  }

  @Override
  public LongArray plus(LongArray a, LongArray b) {
    return Arrays.broadcastCombine(a, b, (x, y) -> {
      LongArray out = x.newEmptyArray(x.getShape());
      for (int i = 0, size = x.size(); i < size; i++) {
        out.set(i, x.get(i) + y.get(i));
      }
      return out;
    });
  }

  @Override
  public void plusAssign(LongArray a, final LongArray out) {
    Arrays.broadcastWith(out, a, (x, y) -> {
      for (int i = 0, size = x.size(); i < size; i++) {
        x.set(i, x.get(i) + y.get(i));
      }
    });
  }

  @Override
  public LongArray minus(LongArray a, LongArray b) {
    return Arrays.broadcastCombine(a, b, (x, y) -> {
      LongArray out = x.newEmptyArray(x.getShape());
      for (int i = 0, size = x.size(); i < size; i++) {
        out.set(i, x.get(i) - y.get(i));
      }
      return out;
    });
  }

  @Override
  public void minusAssign(LongArray a, LongArray out) {
    Arrays.broadcastWith(out, a, (x, y) -> {
      for (int i = 0, size = x.size(); i < size; i++) {
        x.set(i, y.get(i) - x.get(i));
      }
    });
  }

  @Override
  public LongArray times(LongArray a, LongArray b) {
    return Arrays.broadcastCombine(a, b, (x, y) -> {
      LongArray out = x.newEmptyArray(x.getShape());
      for (int i = 0, size = x.size(); i < size; i++) {
        out.set(i, x.get(i) * y.get(i));
      }
      return out;
    });
  }

  @Override
  public void timesAssign(LongArray a, LongArray out) {
    Arrays.broadcastWith(out, a, (x, y) -> {
      for (int i = 0, size = x.size(); i < size; i++) {
        x.set(i, x.get(i) * y.get(i));
      }
    });
  }

  @Override
  public LongArray div(LongArray nominator, LongArray denominator) {
    return Arrays.broadcastCombine(nominator, denominator, (x, y) -> {
      LongArray out = x.newEmptyArray(x.getShape());
      for (int i = 0, size = x.size(); i < size; i++) {
        out.set(i, x.get(i) / y.get(i));
      }
      return out;
    });
  }

  @Override
  public void divAssign(LongArray nominator, LongArray denominatorOut) {
    Arrays.broadcastWith(denominatorOut, nominator, (x, y) -> {
      for (int i = 0, size = x.size(); i < size; i++) {
        x.set(i, y.get(i) / x.get(i));
      }
    });
  }

  @Override
  public ComplexArray plus(ComplexArray a, ComplexArray b) {
    return Arrays.broadcastCombine(a, b, (x, y) -> {
      ComplexArray out = x.newEmptyArray(x.getShape());
      for (int i = 0, size = x.size(); i < size; i++) {
        out.set(i, x.get(i).add(y.get(i)));
      }
      return out;
    });
  }

  @Override
  public void plusAssign(ComplexArray a, final ComplexArray out) {
    Arrays.broadcastWith(out, a, (x, y) -> {
      for (int i = 0, size = x.size(); i < size; i++) {
        x.set(i, x.get(i).add(y.get(i)));
      }
    });
  }

  @Override
  public ComplexArray minus(ComplexArray a, ComplexArray b) {
    return Arrays.broadcastCombine(a, b, (x, y) -> {
      ComplexArray out = x.newEmptyArray(x.getShape());
      for (int i = 0, size = x.size(); i < size; i++) {
        out.set(i, x.get(i).subtract(y.get(i)));
      }
      return out;
    });
  }

  @Override
  public void minusAssign(ComplexArray a, ComplexArray out) {
    Arrays.broadcastWith(out, a, (x, y) -> {
      for (int i = 0, size = x.size(); i < size; i++) {
        x.set(i, y.get(i).subtract(x.get(i)));
      }
    });
  }

  @Override
  public ComplexArray times(ComplexArray a, ComplexArray b) {
    return Arrays.broadcastCombine(a, b, (x, y) -> {
      ComplexArray out = x.newEmptyArray(x.getShape());
      for (int i = 0, size = x.size(); i < size; i++) {
        out.set(i, x.get(i).multiply(y.get(i)));
      }
      return out;
    });
  }

  @Override
  public void timesAssign(ComplexArray a, ComplexArray out) {
    Arrays.broadcastWith(out, a, (x, y) -> {
      for (int i = 0, size = x.size(); i < size; i++) {
        x.set(i, x.get(i).multiply(y.get(i)));
      }
    });
  }

  @Override
  public ComplexArray div(ComplexArray nominator, ComplexArray denominator) {
    return Arrays.broadcastCombine(nominator, denominator, (x, y) -> {
      ComplexArray out = x.newEmptyArray(x.getShape());
      for (int i = 0, size = x.size(); i < size; i++) {
        out.set(i, x.get(i).divide(y.get(i)));
      }
      return out;
    });
  }

  @Override
  public void divAssign(ComplexArray nominator, ComplexArray denominatorOut) {
    Arrays.broadcastWith(denominatorOut, nominator, (x, y) -> {
      for (int i = 0, size = x.size(); i < size; i++) {
        x.set(i, y.get(i).divide(x.get(i)));
      }
    });
  }

  @Override
  public BooleanArray and(BooleanArray a, BooleanArray b) {
    return Arrays.broadcastCombine(a, b, (x, y) -> {
      BooleanArray out = x.newEmptyArray(x.getShape());
      for (int i = 0, size = x.size(); i < size; i++) {
        out.set(i, x.get(i) && y.get(i));
      }
      return out;
    });
  }

  @Override
  public BooleanArray or(BooleanArray a, BooleanArray b) {
    return Arrays.broadcastCombine(a, b, (x, y) -> {
      BooleanArray out = x.newEmptyArray(x.getShape());
      for (int i = 0, size = x.size(); i < size; i++) {
        out.set(i, x.get(i) || y.get(i));
      }
      return out;
    });
  }

  @Override
  public BooleanArray xor(BooleanArray a, BooleanArray b) {
    return Arrays.broadcastCombine(a, b, (x, y) -> {
      BooleanArray out = x.newEmptyArray(x.getShape());
      for (int i = 0, size = x.size(); i < size; i++) {
        out.set(i, x.get(i) ^ y.get(i));
      }
      return out;
    });
  }

  @Override
  public DoubleArray sin(DoubleArray array) {
    return array.map(Math::sin);
  }

  @Override
  public ComplexArray sin(ComplexArray array) {
    return array.map(Complex::sin);
  }

  @Override
  public DoubleArray cos(DoubleArray array) {
    return array.map(Math::cos);
  }

  @Override
  public ComplexArray cos(ComplexArray array) {
    return array.map(Complex::cos);
  }

  @Override
  public DoubleArray tan(DoubleArray array) {
    return array.map(Math::tan);
  }

  @Override
  public ComplexArray tan(ComplexArray array) {
    return array.map(Complex::tan);
  }

  @Override
  public DoubleArray asin(DoubleArray array) {
    return array.map(Math::asin);
  }

  @Override
  public ComplexArray asin(ComplexArray array) {
    return array.map(Complex::asin);
  }

  @Override
  public DoubleArray acos(DoubleArray array) {
    return array.map(Math::acos);
  }

  @Override
  public ComplexArray acos(ComplexArray array) {
    return array.map(Complex::acos);
  }

  @Override
  public DoubleArray atan(DoubleArray array) {
    return array.map(Math::atan);
  }

  @Override
  public ComplexArray atan(ComplexArray array) {
    return array.map(Complex::atan);
  }

  @Override
  public DoubleArray sinh(DoubleArray array) {
    return array.map(Math::sinh);
  }

  @Override
  public ComplexArray sinh(ComplexArray array) {
    return array.map(Complex::sinh);
  }

  @Override
  public DoubleArray cosh(DoubleArray array) {
    return array.map(Math::cosh);
  }

  @Override
  public ComplexArray cosh(ComplexArray array) {
    return array.map(Complex::cosh);
  }

  @Override
  public DoubleArray tanh(DoubleArray array) {
    return array.map(Math::tanh);
  }

  @Override
  public ComplexArray tanh(ComplexArray array) {
    return array.map(Complex::tanh);
  }

  @Override
  public DoubleArray exp(DoubleArray array) {
    return array.map(Math::exp);
  }

  @Override
  public ComplexArray exp(ComplexArray array) {
    return array.map(Complex::exp);
  }

  @Override
  public DoubleArray cbrt(DoubleArray array) {
    return array.map(Math::cbrt);
  }

  @Override
  public DoubleArray ceil(DoubleArray array) {
    return array.map(Math::ceil);
  }

  @Override
  public ComplexArray ceil(ComplexArray array) {
    return array.map(v -> new Complex(Math.ceil(v.getReal()), Math.ceil(v.getImaginary())));
  }

  @Override
  public DoubleArray floor(DoubleArray array) {
    return array.map(Math::floor);
  }

  @Override
  public ComplexArray floor(ComplexArray array) {
    return array.map(v -> new Complex(Math.floor(v.getReal()), Math.floor(v.getImaginary())));
  }

  @Override
  public IntArray abs(IntArray array) {
    return array.map(Math::abs);
  }

  @Override
  public LongArray abs(LongArray array) {
    return array.map(Math::abs);
  }

  @Override
  public DoubleArray abs(DoubleArray array) {
    return array.map(Math::abs);
  }

  @Override
  public DoubleArray abs(ComplexArray array) {
    return array.mapToDouble(Complex::abs);
  }

  @Override
  public DoubleArray scalb(DoubleArray array, int scaleFactor) {
    return array.map(v -> Math.scalb(v, scaleFactor));
  }

  @Override
  public DoubleArray sqrt(DoubleArray array) {
    return array.map(Math::sqrt);
  }

  @Override
  public ComplexArray sqrt(ComplexArray array) {
    return array.map(Complex::sqrt);
  }

  @Override
  public DoubleArray log(DoubleArray array) {
    return array.map(Math::log);
  }

  @Override
  public ComplexArray log(ComplexArray array) {
    return array.map(Complex::log);
  }

  @Override
  public DoubleArray log2(DoubleArray array) {
    return array.map(x -> Math.log(x) / LOG_2);
  }

  @Override
  public DoubleArray pow(DoubleArray in, double power) {
    if (Precision.equals(power, 2, EPS)) {
      return in.map(x -> x * x);
    } else if (Precision.equals(power, 3, EPS)) {
      return in.map(x -> x * x * x);
    } else if (Precision.equals(power, 4, EPS)) {
      return in.map(x -> x * x * x * x);
    } else {
      return in.map(x -> FastMath.pow(x, power));
    }
  }

  @Override
  public DoubleArray log10(DoubleArray in) {
    return in.map(Math::log10);
  }

  @Override
  public DoubleArray signum(DoubleArray in) {
    return in.map(Math::signum);
  }

  @Override
  public LongArray round(DoubleArray in) {
    LongArray out = in.longArray().newEmptyArray(in.getShape());
    out.assign(in, Math::round);
    return out;
  }
}
