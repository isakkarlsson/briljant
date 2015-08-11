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

import org.briljantframework.array.ArrayPrinter;
import org.briljantframework.array.DoubleArray;
import org.briljantframework.array.Op;
import org.briljantframework.array.api.ArrayBackend;
import org.briljantframework.array.api.ArrayFactory;
import org.briljantframework.array.api.ArrayRoutines;
import org.junit.Test;

import static org.briljantframework.array.ArrayAssert.assertMatrixEquals;

public class NetlibArrayRoutinesTest {

  private ArrayBackend backend = new NetlibArrayBackend();
  private ArrayFactory bj = backend.getArrayFactory();
  private ArrayRoutines bjr = backend.getArrayRoutines();

//  private DoubleArray a = bj.doubleArray(10000).assign(10);
//  private DoubleArray b = bj.doubleArray(10000).assign(10);
//  private DoubleArray c = bj.doubleArray(10000, 10000).assign(32);

  static {
    ArrayPrinter.setPrintSlices(3);
    ArrayPrinter.setVisiblePerSlice(3);
    ArrayPrinter.setMinimumTruncateSize(1000);
  }

  @Test
  public void testGemv() throws Exception {
    DoubleArray a = bj.array(new double[][]{
        new double[]{1, 2, 3},
        new double[]{1, 2, 3},
        new double[]{1, 2, 3}
    });

    DoubleArray b = a.getRow(0);


    DoubleArray x = bj.array(new double[]{1, 2, 3});
    DoubleArray y = bj.doubleArray(4).assign(3).asView(1, new int[]{3}, new int[]{1});

    bjr.gemv(Op.TRANSPOSE, 1, a, x, 1, y);
    System.out.println(y.asView(0, new int[]{4}, new int[]{1}));
    assertMatrixEquals(bj.array(new double[]{9,15,21}), y, 0.0);
  }
}