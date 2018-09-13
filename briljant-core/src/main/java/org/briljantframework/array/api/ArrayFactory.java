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

import org.apache.commons.math3.complex.Complex;
import org.briljantframework.array.*;


/**
 * An array factory creates new arrays.
 * 
 * @author Isak Karlsson
 */
public interface ArrayFactory {

  /**
   * Create a new array from the given data.
   * 
   * @param data the data
   * @param <T> the class
   * @return a new array
   */
  <T> Array<T> newVector(T[] data);

  /**
   * Create a new array from the given data.
   *
   * @param data the data
   * @param <T> the class
   * @return a new array
   */
  <T> Array<T> newMatrix(T[][] data);

  /**
   * Creates a new array of the specified shape.
   * 
   * @param shape the shape
   * @param <T> the type of element
   * @return a new array
   */
  <T> Array<T> newArray(int... shape);

  /**
   * Create a {@code BitMatrix} with given data
   *
   * @param data the data
   * @return a new matrix
   */
  BooleanArray newBooleanMatrix(boolean[][] data);

  BooleanArray newBooleanVector(boolean... data);

  /**
   * Create an {@code BitMatrix} with designated shape filled with {@code false}.
   *
   * @param shape the rows
   * @return a new matrix
   */
  BooleanArray newBooleanArray(int... shape);

  /**
   * Create an array with the given data
   *
   * @param data the data array
   * @return a new matrix
   */
  IntArray newIntMatrix(int[][] data);

  /**
   * Create a series with the given data.
   *
   * @param data the data array
   * @return a new matrix
   */
  IntArray newIntVector(int... data);

  /**
   * Create an {@code IntMatrix} with designated shape filled with {@code 0}.
   *
   * @param shape the rows
   * @return a new matrix
   */
  IntArray newIntArray(int... shape);

  /**
   * Create an array with the given data
   *
   * @param data the data
   * @return a new matrix
   */
  LongArray newLongMatrix(long[][] data);

  /**
   * Create a series with the given data.
   *
   * @param data the data array
   * @return a new matrix
   */
  LongArray newLongVector(long... data);

  /**
   * Create an {@code LongMatrix} with designated shape filled with {@code 0}.
   *
   * @param shape the rows
   * @return a new matrix
   */
  LongArray newLongArray(int... shape);

  /**
   * Create a matrix with given data in row-major order.
   *
   * <p>
   * Example
   *
   * <pre>
   * {@code
   * > double[][] data = {
   *     {1, 2, 3},
   *     {1, 2 ,3}
   *   };
   * > f.array(data);
   * 
   * array([[1, 2, 3],
   *        [1, 2, 3]] type: double)
   * }
   * </pre>
   *
   * @param data the data
   * @return a new matrix
   */
  DoubleArray newDoubleMatrix(double[][] data);

  /**
   * Create a series with the given data.
   *
   * @param data the data array
   * @return a new matrix
   */
  DoubleArray newDoubleVector(double... data);

  /**
   * Construct an empty {@code double} are with the given shape. Note that for most implementations
   * the resulting array is initialized with {@code 0}. This is however no guarantee.
   *
   * @param shape the shape
   * @return a new array
   */
  DoubleArray newDoubleArray(int... shape);

  /**
   * Create a matrix with given data
   *
   * @param data the data
   * @return a new matrix
   * @see #newDoubleMatrix(double[][])
   */
  ComplexArray newComplexMatrix(Complex[][] data);

  ComplexArray newComplexVector(Complex... data);

  ComplexArray newComplexVector(double... data);

  /**
   * Create an {@code ComplexMatrix} with designated shape filled with {@code 0+0i}.
   *
   * @param shape the rows
   * @return a new matrix
   */
  ComplexArray newComplexArray(int... shape);

  /**
   * Create a 1d-array with values sampled from the normal (gaussian) distribution with mean
   * {@code 0} and standard deviation {@code 1}.
   *
   * <p>
   * Example
   *
   * <pre>
   * {@code
   * > Arrays.randn(9).reshape(3, 3);
   * array([[0.168, -0.297, -0.374],
   *        [1.030, -1.465,  0.636],
   *        [0.957, -0.990,  0.498]] type: double)
   * }
   * </pre>
   *
   * @param size the size of the array
   * @return a new 1d-array
   */
  DoubleArray randn(int size);

  /**
   * Create a 1d-array with values sampled uniformly from the range {@code [-1, 1]}
   * <p>
   * Example
   *
   * <pre>
   * {@code
   * > Arrays.rand(4).reshape(2,2)
   * array([[0.467, 0.898],
   *        [0.568, 0.103]] type: double)
   * }
   * </pre>
   *
   * @param size the size of the array
   * @return a new 1d-array
   */
  DoubleArray rand(int size);

  /**
   * Creates a double array initialized with ones.
   * 
   * @param shape the shape of the array
   * @return a new double array
   */
  DoubleArray ones(int... shape);

  /**
   * Return a row series of evenly spaced values
   *
   * @param start start value
   * @param end end value
   * @param step step size
   * @return a new row series
   */
  Range range(int start, int end, int step);

  /**
   * Return a row series of evenly spaced values (step = 1)
   *
   * @param start start value
   * @param end end value
   * @return a new row series
   */
  Range range(int start, int end);

  /**
   * Return a row series of evenly spaced values (start = 0, step = 1)
   *
   * @param end end value
   * @return a new row series
   */
  Range range(int end);

  /**
   * Return a row series of linearly spaced values
   *
   * @param start start value
   * @param end end value
   * @param size the size of the returned series
   * @return a new row series
   */
  DoubleArray linspace(double start, double end, int size);

  DoubleArray eye(int size);
}
