package org.briljantframework.matrix;

import org.briljantframework.sort.Swappable;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * <p>
 * The {@code Matrix} interface is a base interface for several different matrix implementations.
 *
 * There are four supported matrix types {@code double}, {@code int}, {@code boolean} and
 * {@link org.briljantframework.complex.Complex}, specialized in
 * {@link DoubleArray}, {@link IntArray}
 * , {@link BitArray} and
 * {@link ComplexArray} respectively.
 * </p>
 *
 * <p>
 * The {@code Matrix} interface provides ways to
 *
 * <ul>
 * <li>adapt one implementation to another.</li>
 * <li>get values of any type.</li>
 * <li>set values of any type.</li>
 * <li>set values of any type from another {@code Matrix}, possibly without boxing.</li>
 * <li>compare values of unknown types.</li>
 * </ul>
 * </p>
 *
 * <h1>Adapt {@code Matrix} to another matrix type</h1>
 * <p>
 * {@code Matrix} defines four methods for adapting the current implementation to any of the four
 * specialized types. However, there are some caveats when adapting matrices and perform mutations.
 *
 * For example, given a {@code DoubleMatrix d} which is adapted to a
 * {@code ComplexMatrix c = d.asComplexMatrix()}, then setting a position to a new {@code Complex}
 * with an imaginary part, e.g., {@code c.set(0, Complex.I)}, would just propagate the real part to
 * the underlying {@code DoubleMatrix}. Likewise, given an {@code IntMatrix} adapted to a
 * {@code DoubleMatrix}, setting a position to a double converts it to an {@code int} (using
 * {@link Math#round(double)}).
 *
 * Finally, if receiver is
 * <ul>
 * <li>{@link DoubleArray}, {@link #asDoubleMatrix()} must return
 * {@code this}</li>
 * <li>{@link IntArray}, {@link #asIntMatrix()} must return
 * {@code this}</li>
 * <li>{@link BitArray}, {@link #asBitMatrix()} must return
 * {@code this}</li>
 * <li>{@link ComplexArray}, {@link #asComplexMatrix()} must return
 * {@code this}</li>
 * </ul>
 * </p>
 * <h1>Implicit conversions</h1>
 * <ul>
 * <li>{@code Complex => double}: {@code value.real()}</li>
 * <li>{@code double => int}: {@code (int) Math.round(value)}</li>
 * <li>{@code int => boolean}: {@code value == 1 ? true : false}</li>
 * <li>{@code boolean => int}; {@code value ? 1 : 0}</li>
 * <li>{@code int => double}: {@code value}</li>
 * <li>{@code double => Complex}: {@code Complex.valueOf(value)}</li>
 * </ul>
 *
 * <p>
 * Remember that most subclasses provide, {@code get(int, int)} and {@code get(int)}, returning the
 * specialized type. For example, {@link DoubleArray#get(int, int)}.
 * </p>
 *
 * <h1>Avoid unboxing/type checking/truncating when transferring values between {@code
 * Matrix}es</h1>
 * <p>
 * Prefer:
 *
 * <pre>
 *   Matrix a = Doubles.randn(10, 1)
 *   Matrix b = Doubles.zeros(10, 1)
 *   a.set(3, b, 0)
 * </pre>
 *
 * to:
 *
 * <pre>
 *   swith(b.getDataType()) {
 *       DataType.COMPLEX: a.set(3, b.getAsComplex(0); break;
 *       ...
 *       ...
 *       ...
 *       default: ...;
 *   }
 * </pre>
 *
 * </p>
 *
 * @author Isak Karlsson
 */
public interface Array<E extends Array> extends Swappable {

  void set(int toIndex, E from, int fromIndex);

  void set(int toRow, int toColumn, E from, int fromRow, int fromColumn);

  int compare(int a, int b);

  /**
   * Assign {@code o} to {@code this}. Ensures that the shapes are equal.
   *
   * @param o the matrix
   * @return receiver modified
   */
  E assign(E o);

  void forEach(int dim, Consumer<E> consumer);

  void setRow(int i, E row);

  void setColumn(int i, E column);

  /**
   * Gets vector at {@code index}. Modifications will change the original matrix.
   *
   * @param index the index
   * @return the column
   */
  E getColumn(int index);

  /**
   * Get row vector at {@code i}. Modifications will change to original matrix.
   *
   * @param i row
   * @return a vector
   */
  E getRow(int i);

//  /**
//   * Reshape {@code this}. Returns a new matrix, with {@code this != this.reshape(..., ...)} but
//   * where modifications of the reshape propagates. I.e. the reshape is a view of the original
//   * matrix.
//   *
//   * @param rows    the new rows
//   * @param columns the new columns
//   * @return a new matrix
//   */
//  E reshape(int rows, int columns);

  E reshape(int... shape);

  E select(int index);

  E select(int dimension, int index);

  void setVector(int dim, int index, E other);

  E getVector(int dimension, int index);

  /**
   * <p> Gets a view of the diagonal of a 2-d array
   *
   * <pre>{@code
   *  >>> DoubleArray b = Bj.array(new double[]{1,2,3,4}).reshape(2, 2);
   *  >>> b.getDiagonal();
   *  [1,4]
   * }</pre>
   *
   * @return a diagonal view
   */
  E getDiagonal();

  /**
   * Get a view of row starting at {@code rowOffset} until {@code rowOffset + rows} and columns
   * starting at {@code colOffset} until {@code colOffset + columns}.
   *
   * For example,
   *
   * <pre>
   *   1 2 3
   *   4 5 6
   *   7 8 9
   * </pre>
   *
   * and {@code matrix.getView(1, 1, 2, 2)} produces
   *
   * <pre>
   *   5 6
   *   8 9
   * </pre>
   *
   * Please note that modifications of the view, mutates the original.
   *
   * @param rowOffset the row offset
   * @param colOffset the column offset
   * @param rows      number of rows after row offset
   * @param columns   number of columns after column offset
   * @return the matrix view
   */
  E getView(int rowOffset, int colOffset, int rows, int columns);

  /**
   * Basic slicing. Returns a view of the underlying matrix. Subclasses should specialize the
   * return type.
   *
   * @param rows    the rows to include
   * @param columns the columns to include
   * @return a view
   */
  E slice(Range rows, Range columns);

  /**
   * Basic slicing. Returns a view of the underlying matrix. The view is preserved as long as
   * possible. However, some operations (e.g. {@link #reshape(int, int)}) is unable to preserve
   * view.
   *
   * <p>The view return is a {@code row-vector}
   *
   * @param range the range
   * @return a view
   */
  E slice(Range range);

  /**
   * Complex slicing. Returns a copy of the matrix.
   *
   * @param rows    the rows to include
   * @param columns the columns to include
   * @return a new matrix with the same size as {@code this}
   */
  E slice(Collection<Integer> rows, Collection<Integer> columns);

  E slice(Collection<Integer> indexes);

  E slice(BitArray bits);

  /**
   * Element wise addition.
   *
   * @param o the other matrix
   * @return a new matrix
   */
  E add(E o);

  /**
   * Element wise subtraction. {@code this - other}.
   *
   * @param o the other matrix
   * @return a new matrix
   */
  E sub(E o);

  /**
   * Element wise <u>m</u>ultiplication
   *
   * @param o the matrix
   * @return a new matrix
   */
  E mul(E o);

  /**
   * Element wise division. {@code this / other}.
   *
   * @param o the other
   * @return a new matrix
   * @throws java.lang.ArithmeticException if {@code other} contains {@code 0}
   */
  E div(E o);

  /**
   * <u>m</u>atrix<u>m</u>ultiplication
   *
   * @param o the other
   * @return r r
   */
  E mmul(E o);

  /**
   * The number of rows.
   *
   * @return number or rows
   */
  int rows();

  /**
   * The number of columns.
   *
   * @return number of columns
   */
  int columns();

  /**
   * Returns the linearized size of this matrix. If {@code rows()} or {@code columns()} return 1,
   * then {@code size()} is intuitive. However, if not, size is {@code rows() * columns()} and used
   * when iterating using {@code getAs...(int)}. To avoid cache misses,
   * {@code for(int i = 0; i < m.size(); i++) m.set(i, o.get(i))} should be preferred to
   *
   * <pre>
   * for(int i = 0; i < m.rows(); i++)
   *   for(int j = 0; j < m.columns(); j++)
   *      m.set(i, j, o.get(i, j))
   * </pre>
   *
   * Since, {@code set(int, int, ....)} shouldn't be used in conjunction with
   * {@code getAs...(int, int)}, the example above should be written as
   *
   * <pre>
   * for (int i = 0; i &lt; m.rows(); i++)
   *   for (int j = 0; j &lt; m.columns(); j++)
   *     m.set(i, j, m, i, j);
   * // or
   * for (int i = 0; i &lt; m.size(); i++)
   *   m.set(i, o, i);
   * </pre>
   *
   * @return the size
   */
  int size();

  int size(int dim);

  /**
   * Get the number of of vectors along the {@code i}:th dimension.
   *
   * @param i the dimension
   * @return the number of vectors along the dimension
   */
  int vectors(int i);

  int dims();

  int stride(int i);

  int[] getShape();

  int[] getStride();

  /**
   * @return true if rows() == columns()
   */
  default boolean isSquare() {
    return rows() == columns();
  }

  boolean isVector();

  boolean isMatrix();

  /**
   * @param other the other
   * @return the boolean
   */
  default boolean hasEqualShape(Array other) {
    return rows() == other.rows() && columns() == other.columns();
  }

  E newEmptyArray(int... shape);

  boolean isView();

  /**
   * @return this matrix as a {@link DoubleArray}.
   */
  DoubleArray asDoubleMatrix();

  /**
   * @return this matrix as an {@link IntArray}.
   */
  IntArray asIntMatrix();

  /**
   * @return return this matrix as a {@link LongArray}
   */
  LongArray asLongMatrix();

  /**
   * @return this matrix as an {@link BitArray}.
   */
  BitArray asBitMatrix();

  /**
   * @return this matrix as a {@link ComplexArray}.
   */
  ComplexArray asComplexMatrix();

  /**
   * @return the transpose of {@code this}.
   */
  E transpose();

  /**
   * Create a copy of this matrix.
   *
   * @return the copy
   */
  E copy();

  BitArray lt(E other);

  BitArray gt(E other);

  BitArray eq(E other);

  BitArray lte(E other);

  BitArray gte(E other);

}
