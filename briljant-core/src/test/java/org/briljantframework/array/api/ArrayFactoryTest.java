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

import static org.briljantframework.array.BasicIndex.ALL;
import static org.junit.Assert.assertEquals;

import org.briljantframework.array.Array;
import org.briljantframework.array.Arrays;
import org.briljantframework.array.DoubleArray;
import org.junit.Test;

/**
 * @author Isak Karlsson <isak-kar@dsv.su.se>
 */
public abstract class ArrayFactoryTest {

  /**
   * @return the array factor under test
   */
  public abstract ArrayFactory getFactory();

  @Test
  public void testNewMatrix() throws Exception {
    String[][] data = { {"Hello", "world", "this"}, {"is", "not", "fun"}};
    Array<String> array = getFactory().newMatrix(data);
    assertEquals(
        getFactory().newVector(new String[]{"Hello", "is", "world", "not", "this", "fun"}).reshape(2, 3), array);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewMatrix_illegal_row() throws Exception {
    String[][] data = { {"a", "b"}, {"c", "d", "e"}};
    getFactory().newMatrix(data);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewMatrix_empty() throws Exception {
    String[][] data = { };
    getFactory().newMatrix(data);
  }

  @Test
  public void testArrayGet_BooleanArray() throws Exception {
    Array<Integer> a = getFactory().range(3 * 3).reshape(3, 3).boxed();
    Array<Integer> x = a.get(a.where(i -> i > 2));
    assertEquals(getFactory().newVector(new Integer[]{3, 4, 5, 6, 7, 8}), x);
  }

  @Test
  public void testArraySet_BooleanArray() throws Exception {
    Array<Integer> a = getFactory().range(3 * 3).reshape(3, 3).boxed();
    Array<Integer> b = getFactory().newArray(3, 3);
    b.set(a.where(i -> i > 2), 10);
    assertEquals(getFactory().newVector(new Integer[]{null, null, null, 10, 10, 10, 10, 10, 10}).reshape(3, 3), b);
  }

  @Test
  public void testDoubleArraySet_BooleanArray() throws Exception {
    DoubleArray a = getFactory().range(3 * 3).reshape(3, 3).doubleArray();
    DoubleArray b = getFactory().newDoubleArray(3, 3);
    b.set(a.where(i -> i > 2), 10);
    assertEquals(getFactory().newDoubleVector(0, 0, 0, 10, 10, 10, 10, 10, 10).reshape(3, 3), b);
  }

  @Test
  public void testDoubleArrayGet_BooleanArray() throws Exception {
    DoubleArray a = getFactory().range(3 * 3).reshape(3, 3).doubleArray();
    DoubleArray x = a.get(a.where(i -> i > 2));
    assertEquals(getFactory().newDoubleVector(3, 4, 5, 6, 7, 8), x);
  }

  @Test
  public void testDoubleArrayGet_Range() throws Exception {
    DoubleArray a = getFactory().range(9 * 3).reshape(9, 3).doubleArray();
    assertEquals(getFactory().newDoubleVector(1, 2, 10, 11).reshape(2, 2),
        a.get(getFactory().range(1, 3), getFactory().range(2)));
    assertEquals(getFactory().newDoubleVector(0, 2, 4, 6, 8, 9, 11, 13, 15, 17, 18, 20, 22, 24, 26)
        .reshape(5, 3), a.get(getFactory().range(0, 9, 2), ALL));
  }

  @Test
  public void testDoubleArray_assign_broadcast() throws Exception {
    DoubleArray zeros = getFactory().newDoubleArray(3, 3);
    zeros.assign(getFactory().newDoubleVector(1, 2, 3).reshape(1, 3));
    assertEquals(getFactory().newDoubleMatrix(new double[][] { {1, 2, 3}, {1, 2, 3}, {1, 2, 3}}),
        zeros);

  }

  // TODO: 15/01/16 fix these tests
  @Test
  public void testName() throws Exception {
    DoubleArray x = Arrays.linspace(0, 9, 10).reshape(2, 5).getRow(0).transpose();
    DoubleArray y = Arrays.linspace(0, 9, 10).reshape(2, 5).getRow(0);

    System.out.println(x);
    System.out.println(y);

    Arrays.axpy(1, x, y);
    System.out.println(y);
  }

  @Test
  public void testGetVector() throws Exception {
    DoubleArray x = DoubleArray.of(1, 2, 3, 4);
    DoubleArray y = DoubleArray.zeros(4);
    Arrays.axpy(2, x, y);
    System.out.println(y);
  }
}
