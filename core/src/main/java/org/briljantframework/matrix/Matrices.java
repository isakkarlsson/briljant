package org.briljantframework.matrix;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import com.github.fommil.netlib.BLAS;

import org.briljantframework.Check;
import org.briljantframework.IndexComparator;
import org.briljantframework.QuickSort;
import org.briljantframework.Utils;
import org.briljantframework.complex.Complex;
import org.briljantframework.exceptions.TypeConversionException;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Isak Karlsson
 */
public final class Matrices {

  public static final Random RANDOM = Utils.getRandom();
  public static final double LOG_2 = Math.log(2);

  private static final BLAS BLAS = com.github.fommil.netlib.BLAS.getInstance();


  private final static Map<Class, MatrixFactory<?>> NATIVE_TO_FACTORY;

  static {
    NATIVE_TO_FACTORY = ImmutableMap.of(Double.class, new MatrixFactory<Double>() {
      @Override
      public Matrix newVector(int size) {
        return new DefaultDoubleMatrix(size);
      }

      @Override
      public Matrix newMatrix(int rows, int columns) {
        return new DefaultDoubleMatrix(rows, columns);
      }

      @Override
      public Matrix newVectorFilledWith(int size, Double fill) {
        return new DefaultDoubleMatrix(size).assign(fill);
      }

      @Override
      public Matrix newMatrixFilledWith(int rows, int columns, Double fill) {
        return new DefaultDoubleMatrix(rows, columns).assign(fill);
      }
    }, Integer.class, new MatrixFactory<Number>() {
      @Override
      public Matrix newVector(int size) {
        return new DefaultIntMatrix(size);
      }

      @Override
      public Matrix newMatrix(int rows, int columns) {
        return new DefaultIntMatrix(rows, columns);
      }

      @Override
      public Matrix newVectorFilledWith(int size, Number fill) {
        return new DefaultIntMatrix(size).assign(fill.intValue());
      }

      @Override
      public Matrix newMatrixFilledWith(int rows, int columns, Number fill) {
        return new DefaultIntMatrix(rows, columns).assign(fill.intValue());
      }
    }, Complex.class, new MatrixFactory<Complex>() {
      @Override
      public Matrix newVector(int size) {
        return new DefaultComplexMatrix(size);
      }

      @Override
      public Matrix newMatrix(int rows, int columns) {
        return new DefaultComplexMatrix(rows, columns);
      }

      @Override
      public Matrix newVectorFilledWith(int size, Complex fill) {
        return new DefaultComplexMatrix(size, fill);
      }

      @Override
      public Matrix newMatrixFilledWith(int rows, int columns, Complex fill) {
        return new DefaultComplexMatrix(rows, columns, fill);
      }
    }, Boolean.class, new MatrixFactory<Boolean>() {
      @Override
      public Matrix newVector(int size) {
        return new DefaultBitMatrix(size);
      }

      @Override
      public Matrix newMatrix(int rows, int columns) {
        return new DefaultBitMatrix(rows, columns);
      }

      @Override
      public Matrix newVectorFilledWith(int size, Boolean fill) {
        return null;
      }

      @Override
      public Matrix newMatrixFilledWith(int rows, int columns, Boolean fill) {
        return new DefaultBitMatrix(rows, columns, fill);
      }
    }, Long.class, new MatrixFactory<Long>() {
      @Override
      public Matrix newVector(int size) {
        return new DefaultLongMatrix(size);
      }

      @Override
      public Matrix newMatrix(int rows, int columns) {
        return new DefaultLongMatrix(rows, columns);
      }

      @Override
      public Matrix newVectorFilledWith(int size, Long fill) {
        return new DefaultLongMatrix(size).assign(fill);
      }

      @Override
      public Matrix newMatrixFilledWith(int rows, int columns, Long fill) {
        return new DefaultLongMatrix(rows, columns).assign(fill);
      }
    });
  }

  private static final Pattern ROW_SEPARATOR = Pattern.compile(";");
  private static final Pattern VALUE_SEPARATOR = Pattern.compile(",");

  private Matrices() {
  }

  @SuppressWarnings("unchecked")
  public static <T> MatrixFactory<T> getMatrixFactory(Class<T> type) {
    MatrixFactory<T> f = (MatrixFactory<T>) NATIVE_TO_FACTORY.get(type);
    if (f == null) {
      throw new TypeConversionException(type.toString());
    }
    return f;
  }

  public static Matrix zeros(int size, Class<?> type) {
    return getMatrixFactory(type).newVector(size);
  }

  public static Matrix zeros(int rows, int columns, Class<?> type) {
    return getMatrixFactory(type).newMatrix(rows, columns);
  }

  public static DoubleMatrix zeros(int size) {
    return DoubleMatrix.newVector(size);
  }

  public static DoubleMatrix zeros(int rows, int columns) {
    return DoubleMatrix.newMatrix(rows, columns);
  }

  public static DoubleMatrix ones(int size) {
    return zeros(size).assign(1);
  }

  public static DoubleMatrix ones(int rows, int columns) {
    return zeros(rows, columns).assign(1);
  }

  public static Matrix filledWith(int size, double value) {
    return zeros(size).assign(value);
  }

  public static Matrix filledWith(int rows, int columns, double value) {
    return zeros(rows, columns).assign(value);
  }

  public static <T> Matrix filledWith(int size, Class<T> type, T value) {
    return getMatrixFactory(type).newVectorFilledWith(size, value);
  }

  public static <T> Matrix filledWith(int rows, int columns, Class<T> type, T value) {
    return getMatrixFactory(type).newMatrixFilledWith(rows, columns, value);
  }

  public static DoubleMatrix randn(int rows, int cols) {
    return DoubleMatrix.newMatrix(rows, cols).assign(RANDOM::nextGaussian);
  }

  public static DoubleMatrix randn(int size) {
    return DoubleMatrix.newVector(size).assign(RANDOM::nextGaussian);
  }

  public static DoubleMatrix rand(int rows, int cols) {
    return DoubleMatrix.newMatrix(rows, cols).assign(RANDOM::nextDouble);
  }

  public static DoubleMatrix rand(int size) {
    return DoubleMatrix.newVector(size).assign(RANDOM::nextDouble);
  }

  /**
   * Identity matrix of {@code size}
   *
   * @param size the size
   * @return the identity matrix
   */
  public static DoubleMatrix eye(int size) {
    double[] diagonal = new double[size];
    for (int i = 0; i < size; i++) {
      diagonal[i] = 1;
    }
    return Diagonal.of(size, size, diagonal);
  }

  public static double max(DoubleMatrix matrix) {
    return matrix.reduce(Double.NEGATIVE_INFINITY, Math::max);
  }


  /**
   * @param matrix the matrix
   * @return the index of the maximum value
   */
  public static int argmax(DoubleMatrix matrix) {
    int index = 0;
    double largest = matrix.get(0);
    for (int i = 1; i < matrix.size(); i++) {
      double v = matrix.get(i);
      if (v > largest) {
        index = i;
        largest = v;
      }
    }
    return index;
  }

  /**
   * @param matrix the matrix
   * @return the index of the minimum value
   */
  public static int argmin(DoubleMatrix matrix) {
    int index = 0;
    double smallest = matrix.get(0);
    for (int i = 1; i < matrix.size(); i++) {
      double v = matrix.get(i);
      if (v < smallest) {
        smallest = v;
        index = i;
      }
    }
    return index;
  }


  public static IntMatrix range(int start, int end) {
    return range(start, end, 1);
  }

  public static IntMatrix range(int start, int end, int step) {
    return Range.range(start, end, step).copy();
  }

  /**
   * <p>
   * Take values in {@code a}, using the indexes in {@code indexes}. For example,
   * </p>
   *
   * @param a       the source matrix
   * @param indexes the indexes of the values to extract
   * @return a new matrix; the returned matrix has the same type as {@code a} (as returned by
   * {@link org.briljantframework.matrix.Matrix#newEmptyMatrix(int, int)}).
   */
  public static IntMatrix take(IntMatrix a, IntMatrix indexes) {
    IntMatrix taken = a.newEmptyVector(indexes.size());
    for (int i = 0; i < indexes.size(); i++) {
      taken.set(i, a.get(indexes.get(i)));
    }
    a.slice(indexes.flat());
    return taken;
  }

  /**
   * <p>
   * Changes the values of a copy of {@code a} according to the values of the {@code mask} and the
   * values in {@code values}. The value at {@code i} in a copy of {@code a} is set to value at
   * {@code i} from {@code values} if the boolean at {@code i} in {@code mask} is {@code true}.
   * </p>
   *
   * @param a      a source array
   * @param mask   the mask; same shape as {@code a}
   * @param values the values; same shape as {@code a}
   * @return a new matrix; the returned matrix has the same type as {@code a}.
   */
  public static IntMatrix mask(IntMatrix a, BitMatrix mask, IntMatrix values) {
    Check.equalShape(a, mask);
    Check.equalShape(a, values);

    IntMatrix masked = a.copy();
    putMask(masked, mask, values);
    return masked;
  }

  /**
   * <p>
   * Changes the values of {@code a} according to the values of the {@code mask} and the values in
   * {@code values}.
   * </p>
   *
   * @param a      the target matrix
   * @param mask   the mask; same shape as {@code a}
   * @param values the mask; same shape as {@code a}
   */
  public static void putMask(IntMatrix a, BitMatrix mask, IntMatrix values) {
    Check.equalShape(a, mask);
    Check.equalShape(a, values);
    for (int i = 0; i < a.size(); i++) {
      if (mask.get(i)) {
        a.set(i, values.get(i));
      }
    }
  }

  /**
   * <p>
   * Selects the values in {@code a} according to the values in {@code where}, replacing those not
   * selected with {@code replace}.
   * </p>
   *
   * @param a       the source matrix
   * @param where   the selection matrix; same shape as {@code a}
   * @param replace the replacement value
   * @return a new matrix; the returned matrix has the same type as {@code a}.
   */
  public static IntMatrix select(IntMatrix a, BitMatrix where, int replace) {
    Check.equalShape(a, where);
    return a.copy().assign(where, (b, i) -> b ? replace : i);
  }

  // /**
  // * <pre>
  // * > import org.briljantframework.matrix.*;
  // * DoubleMatrix a = Doubles.randn(10, 1)
  // * DoubleMatrix x = Anys.sort(a).asDoubleMatrix()
  // *
  // * -1.8718
  // * -0.8834
  // * -0.6161
  // * -0.0953
  // * 0.0125
  // * 0.3538
  // * 0.4326
  // * 0.4543
  // * 1.0947
  // * 1.1936
  // * shape: 10x1 type: double
  // * </pre>
  // *
  // * @param matrix the source matrix
  // * @return a new matrix; the returned matrix has the same type as {@code a}
  // */
  // public static Matrix sort(Matrix matrix) {
  // return sort(matrix, Matrix::compare);
  // }

  /**
   * <p>
   * Create a vector of length {@code num} with evenly spaced values between {@code start} and
   * {@code end}.
   * </p>
   *
   * @param start the start value
   * @param stop  the end value
   * @param num   the number of steps (i.e. intermediate values)
   * @return a vector
   */
  public static DoubleMatrix linspace(double start, double stop, int num) {
    DoubleMatrix values = DoubleMatrix.newVector(num);
    double step = (stop - start) / (num - 1);
    double value = start;
    for (int index = 0; index < num; index++) {
      values.set(index, value);
      value += step;
    }
    return values;
  }

  public static DoubleMatrix map(DoubleMatrix in, DoubleUnaryOperator operator) {
    return in.newEmptyMatrix(in.rows(), in.columns()).assign(in, operator);
  }

  public static DoubleMatrix sqrt(DoubleMatrix matrix) {
    return map(matrix, Math::sqrt);
  }

  public static DoubleMatrix log(DoubleMatrix in) {
    return map(in, Math::log);
  }

  public static DoubleMatrix log2(DoubleMatrix in) {
    return map(in, x -> Math.log(x) / LOG_2);
  }

  public static DoubleMatrix pow(DoubleMatrix in, double power) {
    switch ((int) power) {
      case 2:
        return map(in, x -> x * x);
      case 3:
        return map(in, x -> x * x * x);
      case 4:
        return map(in, x -> x * x * x * x);
      default:
        return map(in, x -> Math.pow(x, power));
    }
  }

  public static DoubleMatrix log10(DoubleMatrix in) {
    return map(in, Math::log10);
  }

  public static DoubleMatrix signum(DoubleMatrix in) {
    return map(in, Math::signum);
  }

  public static DoubleMatrix abs(DoubleMatrix in) {
    return map(in, Math::abs);
  }

  public static LongMatrix round(DoubleMatrix in) {
    return LongMatrix.newMatrix(in.rows(), in.columns()).assign(in, Math::round);
  }

  public static double trace(DoubleMatrix matrix) {
    int min = Math.min(matrix.rows(), matrix.columns());
    double sum = 0;
    for (int i = 0; i < min; i++) {
      sum += matrix.get(i, i);
    }
    return sum;
  }

  public static long sum(LongMatrix matrix) {
    return matrix.reduce(0, Long::sum);
  }

  public static double sum(DoubleMatrix matrix) {
    return matrix.reduce(0, Double::sum);
  }

  public static int sum(IntMatrix matrix) {
    return matrix.reduce(0, Integer::sum);
  }

  public static int sum(BitMatrix matrix) {
    int sum = 0;
    for (int i = 0; i < matrix.size(); i++) {
      sum += matrix.get(i) ? 1 : 0;
    }
    return sum;
  }

  /**
   * Sum t.
   *
   * @param m    the m
   * @param axis the axis
   * @return the t
   */
  public static DoubleMatrix sum(DoubleMatrix m, Axis axis) {
    switch (axis) {
      case ROW:
        return m.reduceRows(Matrices::sum);
      case COLUMN:
        return m.reduceColumns(Matrices::sum);
      default:
        throw new IllegalArgumentException();
    }
  }

  public static ComplexMatrix newComplexMatrix(int rows, int columns) {
    return new DefaultComplexMatrix(rows, columns);
  }

  public static <T extends Matrix> T shuffle(T matrix) {
    Utils.permute(matrix.size(), matrix);
    return matrix;
  }

  /**
   * Stacks matrices vertically, i.e. a 2-by-3 matrix vstacked with a 10-by-3 matrix
   * resuls in a 12-by-3 matrix.
   *
   * @param matrices a sequence of matrices; all having the same {@code columns}
   * @param <T>      the matrix type
   * @return a new matrix; {@code shape = [sum-of-rows, columns]}
   */
  public static <T extends Matrix<T>> T vstack(Collection<T> matrices) {
    checkArgument(matrices.size() > 0);
    int rows = 0;
    int columns = 0;
    T first = null;
    for (T matrix : matrices) {
      if (first == null) {
        first = matrix;
        columns = first.columns();
      }
      checkArgument(columns == matrix.columns(),
                    "Can't vstack %s with %s.", matrix.getShape(), first.getShape());
      rows += matrix.rows();
    }

    T newMatrix = first.newEmptyMatrix(rows, columns);
    int pad = 0;
    for (T matrix : matrices) {
      for (int j = 0; j < matrix.columns(); j++) {
        for (int i = 0; i < matrix.rows(); i++) {
          newMatrix.set(i + pad, j, matrix, i, j);
        }
      }
      pad += matrix.rows();
    }
    return newMatrix;
  }

  /**
   * Stacks matrices horizontally, i.e. a 3-by-2 matrix hstacked with a 3-by-10 matrix
   * results in a 3-by-12 matrix.
   *
   * @param matrices a sequence of matrices; all having the same {@code rows}
   * @param <T>      the matrix type
   * @return a new matrix; {@code shape = [rows, sum-of-columns]}
   */
  public static <T extends Matrix<T>> T hstack(Collection<T> matrices) {
    checkArgument(matrices.size() > 0);
    int columns = 0;
    int rows = 0;
    T first = null;
    for (T matrix : matrices) {
      if (first == null) {
        first = matrix;
        rows = first.rows();
      }
      Preconditions.checkArgument(rows == matrix.rows(),
                                  "Can't hstack %s with %s.", matrix.getShape(), first.getShape());
      columns += matrix.columns();
    }
    T newMatrix = first.newEmptyMatrix(rows, columns);
    int pad = 0;
    for (T matrix : matrices) {
      for (int j = 0; j < matrix.columns(); j++) {
        for (int i = 0; i < matrix.rows(); i++) {
          newMatrix.set(i, j + pad, matrix, i, j);
        }
      }
      pad += matrix.columns();
    }
    return newMatrix;
  }

  /**
   * Split matrix horizontally (i.e. column-wise). A 3-by-3 matrix hsplit into 3 parts
   * return a (lazy) list of 3 3-by-1 matrices.
   *
   * <p>The returned list is lazy, i.e. no splitting is done before {@link List#get(int)} is
   * called. To get a computed list, use {@code new ArrayList<>(Matrices.hsplit(m, 3))}.
   * This is useful when {@link List#get(int)} is used multiple times.
   *
   * @param matrix matrix to be split
   * @param parts  parts to split matrix (must evenly devide {@code matrix.columns()})
   * @param <T>    the matrix type
   * @return a (lazy) list of {@code part} elements
   */
  public static <T extends Matrix<T>> List<T> hsplit(T matrix, int parts) {
    checkNotNull(matrix);
    checkArgument(matrix.rows() % parts == 0, "Parts does not evenly dived columns.");
    int partColumns = matrix.columns() / parts;
    return new AbstractList<T>() {

      @NotNull
      @Override
      public T get(int index) {
        checkElementIndex(index, size());
        T part = matrix.newEmptyMatrix(matrix.rows(), partColumns);
        for (int j = 0; j < part.columns(); j++) {
          for (int i = 0; i < part.rows(); i++) {
            part.set(i, j, matrix, i, j + partColumns * index);
          }
        }
        return part;
      }

      @Override
      public int size() {
        return parts;
      }
    };
  }

  /**
   * Split matrix vertically (i.e. row-wise). A 3-by-3 matrix hsplit into 3 parts
   * return a (lazy) list of 3 1-by-3 matrices.
   *
   * <p>The returned list is lazy, i.e. no splitting is done before {@link List#get(int)} is
   * called. To get a computed list, use {@code new ArrayList<>(Matrices.hsplit(m, 3))}.
   * This is useful when {@link List#get(int)} is used multiple times.
   *
   * @param matrix matrix to be split
   * @param parts  parts to split matrix (must evenly devide {@code matrix.columns()})
   * @param <T>    the matrix type
   * @return a (lazy) list of {@code part} elements
   */
  public static <T extends Matrix<T>> List<T> vsplit(T matrix, int parts) {
    checkNotNull(matrix);
    checkArgument(matrix.rows() % parts == 0, "Parts does not evenly divide rows.");
    int partRows = matrix.rows() / parts;
    return new AbstractList<T>() {
      @NotNull
      @Override
      public T get(int index) {
        checkElementIndex(index, size());
        T part = matrix.newEmptyMatrix(partRows, matrix.columns());
        for (int j = 0; j < part.columns(); j++) {
          for (int i = 0; i < part.rows(); i++) {
            part.set(i, j, matrix, i + partRows * index, j);
          }
        }
        return part;
      }

      @Override
      public int size() {
        return parts;
      }
    };
  }

  public static void shuffle(Matrix matrix, Axis axis) {
    if (axis == Axis.ROW) {
      for (int i = 0; i < matrix.rows(); i++) {
        shuffle(matrix.getRowView(i));
      }
    } else {
      for (int i = 0; i < matrix.columns(); i++) {
        shuffle(matrix.getColumnView(i));
      }
    }
  }

  /**
   * Parse a matrix in the format
   *
   * <pre>
   *     row :== double<sub>1</sub>, {double<sub>n</sub>}
   *     matrix :== row<sub>1</sub>; {row<sub>m</sub>}
   * </pre>
   * <p>
   * For example, {@code 1, 2, 3, 4; 1, 2, 3, 4;1, 2, 3, 4} is a 3-by-4 matrix with ones in the
   * first column, twos in the second column etc.
   * <p>
   * Returns an {@link org.briljantframework.matrix.DefaultDoubleMatrix}.
   *
   * @param str the input matrix as a string
   * @return a matrix
   */
  public static DoubleMatrix parseMatrix(String str) {
    checkArgument(str != null && str.length() > 0);

    String[] rows = ROW_SEPARATOR.split(str);
    if (rows.length < 1) {
      throw new NumberFormatException("Illegally formatted Matrix");
    }

    DoubleMatrix matrix = null;
    for (int i = 0; i < rows.length; i++) {
      String[] values = VALUE_SEPARATOR.split(rows[i]);
      if (i == 0) {
        matrix = new DefaultDoubleMatrix(rows.length, values.length);
      }

      for (int j = 0; j < values.length; j++) {
        matrix.set(i, j, Double.parseDouble(values[j].trim()));
      }
    }

    return matrix;
  }

  /**
   * Eye diagonal.
   *
   * @param rows the rows
   * @param cols the cols
   * @return the diagonal
   */
  public static Diagonal eye(int rows, int cols) {
    double[] diagonal = new double[rows * cols];
    for (int i = 0; i < diagonal.length; i++) {
      diagonal[i] = 1;
    }
    return Diagonal.of(rows, cols, diagonal);
  }

  /**
   * <p>
   * Sorts the source matrix {@code a} in the order specified by {@code comparator}. For example,
   * reversed sorted
   * </p>
   *
   * <pre>
   *  > import org.briljantframework.matrix.*;
   *    DoubleMatrix a = Matrices.randn(12, 1)
   *    DoubleMatrix x = Matrices.sort(a, (c, i, j) -> -c.compare(a, b));
   * </pre>
   * <p>
   * {@link org.briljantframework.complex.Complex} and {@link ComplexMatrix} do not have a natural
   * sort order.
   * </p>
   *
   * <pre>
   *  > import org.briljantframework.matrix.*;
   *    ComplexMatrix a = randn(12, 1).asComplexMatrix().map(Complex::sqrt)
   *    ComplexMatrix x = sort(a, (c, i, j) -> Double.compare(c.get(i).abs(), c.get(j).abs());
   *
   *    0.1499 + 0.0000i
   *    0.5478 + 0.0000i
   *    0.5725 + 0.0000i
   *    0.0000 + 0.5916i
   *    0.0000 + 0.6856i
   *    0.0000 + 0.8922i
   *    0.0000 + 0.9139i
   *    0.0000 + 1.0130i
   *    0.0000 + 1.1572i
   *    1.1912 + 0.0000i
   *    1.2493 + 0.0000i
   *    1.2746 + 0.0000i
   *    shape: 12x1 type: complex
   * </pre>
   *
   * @param matrix the source matrix
   * @param cmp    the comparator; first argument is the container, and the next are indexes
   * @return a new sorted matrix; the returned matrix has the same type as {@code a}
   */
  public static <T extends Matrix<T>> T sort(T matrix, IndexComparator<T> cmp) {
    T out = matrix.copy();
    QuickSort.quickSort(0, out.size(), (a, b) -> cmp.compare(out, a, b), out);
    return out;
  }

  public static <T extends Matrix<T>> T sort(T matrix, IndexComparator<T> cmp, Axis axis) {
    T out = matrix.copy();
    if (axis == Axis.ROW) {
      for (int i = 0; i < matrix.rows(); i++) {
        T row = out.getRowView(i);
        QuickSort.quickSort(0, row.size(), (a, b) -> cmp.compare(row, b, b), row);
      }
    } else {
      for (int i = 0; i < matrix.columns(); i++) {
        T col = out.getColumnView(i);
        QuickSort.quickSort(0, col.size(), (a, b) -> cmp.compare(col, a, b), col);
      }
    }
    return out;
  }

  /**
   * Computes the mean of the matrix.
   *
   * @param matrix the matrix
   * @return the mean
   */
  public static double mean(DoubleMatrix matrix) {
    return matrix.reduce(0, Double::sum) / matrix.size();
  }

  /**
   * @param matrix the matrix
   * @param axis   the axis
   * @return a mean matrix; if {@code axis == ROW} with shape = {@code [1, columns]}; or
   * {@code axis == COLUMN} with shape {@code [rows, 1]}.
   */
  public static DoubleMatrix mean(DoubleMatrix matrix, Axis axis) {
    if (axis == Axis.ROW) {
      return matrix.reduceRows(Matrices::mean);
    } else {
      return matrix.reduceColumns(Matrices::mean);
    }
  }

  /**
   * @param vector the vector
   * @return the standard deviation
   */
  public static double std(DoubleMatrix vector) {
    return std(vector, mean(vector));
  }

  /**
   * @param vector the vector
   * @param mean   the mean
   * @return the standard deviation
   */
  public static double std(DoubleMatrix vector, double mean) {
    double var = var(vector, mean);
    return Math.sqrt(var / (vector.size() - 1));
  }

  public static DoubleMatrix std(DoubleMatrix matrix, Axis axis) {
    if (axis == Axis.ROW) {
      return matrix.reduceRows(Matrices::std);
    } else {
      return matrix.reduceColumns(Matrices::std);
    }
  }

  /**
   * @param matrix the vector
   * @param mean   the mean
   * @return the variance
   */
  public static double var(DoubleMatrix matrix, double mean) {
    return matrix.reduce(0, (v, acc) -> acc + (v - mean) * (v - mean));
  }

  /**
   * @param vector the vector
   * @return the variance
   */
  public static double var(DoubleMatrix vector) {
    return var(vector, mean(vector));
  }

  public static DoubleMatrix var(DoubleMatrix matrix, Axis axis) {
    if (axis == Axis.ROW) {
      return matrix.reduceRows(Matrices::var);
    } else {
      return matrix.reduceColumns(Matrices::var);
    }
  }

  /**
   * Simple wrapper around
   * {@link com.github.fommil.netlib.BLAS#dgemm(String, String, int, int, int, double, double[],
   * int, double[], int, double, double[], int)}
   * <p>
   * Performs no additional error checking.
   *
   * @param t     left hand side
   * @param alpha scaling for lhs
   * @param other right hand side
   * @param beta  scaling for rhs
   * @param tmp   result is written to {@code tmp}
   */
  public static void mmul(DoubleMatrix t, double alpha, DoubleMatrix other, double beta,
                          double[] tmp) {
    BLAS.dgemm("n", "n", t.rows(), other.columns(),
               other.rows(), alpha, t.asDoubleArray(), t.rows(),
               other.asDoubleArray(), other.rows(), beta, tmp, t.rows());
  }

  public static void mmul(DoubleMatrix t, double alpha, Transpose a, DoubleMatrix other,
                          double beta, Transpose b, double[] tmp) {
    String transA = "n";
    int thisRows = t.rows();
    if (a.transpose()) {
      thisRows = t.columns();
      transA = "t";
    }

    String transB = "n";
    int otherRows = other.rows();
    int otherColumns = other.columns();
    if (b.transpose()) {
      otherRows = other.columns();
      otherColumns = other.rows();
      transB = "t";
    }
    BLAS.dgemm(transA, transB, thisRows, otherColumns, otherRows, alpha, t.asDoubleArray(),
               t.rows(), other.asDoubleArray(), other.rows(), beta, tmp,
               thisRows);
  }

  public static ComplexMatrix newComplexVector(Complex... values) {
    return new DefaultComplexMatrix(values);
  }

  public static int argmaxnot(DoubleMatrix m, int not) {
    double max = Double.NEGATIVE_INFINITY;
    int argMax = -1;
    for (int i = 0; i < m.size(); i++) {
      if (not != i && m.get(i) > max) {
        argMax = i;
        max = m.get(i);
      }
    }
    return argMax;
  }

  public static double maxnot(DoubleMatrix m, int not) {
    double max = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < m.size(); i++) {
      if (not != i && m.get(i) > max) {
        max = m.get(i);
      }
    }
    return max;
  }

  private static interface MatrixFactory<T> {

    Matrix newVector(int size);

    Matrix newMatrix(int rows, int columns);

    Matrix newVectorFilledWith(int size, T fill);

    Matrix newMatrixFilledWith(int rows, int columns, T fill);
  }
}
