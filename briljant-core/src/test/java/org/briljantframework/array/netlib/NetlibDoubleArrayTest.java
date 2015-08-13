/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Isak Karlsson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.briljantframework.array.netlib;

import org.briljantframework.array.DoubleArray;
import org.briljantframework.array.Op;
import org.briljantframework.array.api.ArrayFactory;
import org.junit.Test;

import static org.briljantframework.array.ArrayAssert.assertMatrixEquals;

public class NetlibDoubleArrayTest {

  ArrayFactory f = new NetlibArrayBackend().getArrayFactory();

  @Test
  public void testMatrixMultiplication() throws Exception {
    DoubleArray a = f.array(new double[][]{
        {1, 2, 3},
        {1, 2, 3}
    });
    DoubleArray b = f.array(new double[][]{
        {1, 1},
        {2, 2},
        {3, 3}
    });

    DoubleArray expected = f.array(new double[][]{
        {2, 4, 6},
        {4, 8, 12},
        {6, 12, 18}
    });

    assertMatrixEquals(expected, b.mmul(a), 1e-10);
    assertMatrixEquals(expected.mul(2), b.mmul(2, a), 1e-10);
    assertMatrixEquals(expected, a.mmul(Op.TRANSPOSE, a, Op.KEEP), 1e-10);
    assertMatrixEquals(expected.mul(2), a.mmul(2, Op.TRANSPOSE, a, Op.KEEP), 1e-10);
  }

  @Test
  public void testTransposeAndMatrixMultiply() throws Exception {
    DoubleArray a = f.array(new double[][]{
        {1, 2, 3},
        {1, 2, 3}
    });
    DoubleArray expected = f.array(new double[][]{
        {2, 4, 6},
        {4, 8, 12},
        {6, 12, 18}
    });
    assertMatrixEquals(expected, a.transpose().mmul(a), 1e-10);
  }
}