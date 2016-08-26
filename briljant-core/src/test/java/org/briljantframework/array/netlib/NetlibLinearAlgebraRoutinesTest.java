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
package org.briljantframework.array.netlib;

import com.github.fommil.netlib.F2jLAPACK;
import com.github.fommil.netlib.LAPACK;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Precision;
import org.briljantframework.array.*;
import org.briljantframework.array.api.ArrayBackend;
import org.briljantframework.array.api.ArrayFactory;
import org.briljantframework.array.api.LinearAlgebraRoutines;
import org.briljantframework.array.linalg.decomposition.SingularValueDecomposition;
import org.junit.Assume;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class NetlibLinearAlgebraRoutinesTest {
  private static ArrayBackend b = new NetlibArrayBackend();
  private static ArrayFactory bj = b.getArrayFactory();
  private static LinearAlgebraRoutines linalg = b.getLinearAlgebraRoutines();

  @Test
  public void rank() throws Exception {

  }

  @Test
  public void geev() throws Exception {
    Assume.assumeFalse(LAPACK.getInstance().getClass().equals(F2jLAPACK.class));

    final int n = 5;
    DoubleArray a = bj
        .newDoubleVector(-1.01, 3.98, 3.30, 4.43, 7.31, 0.86, 0.53, 8.26, 4.96, -6.43, -4.60, -7.04,
            -3.89, -7.66, -6.16, 3.31, 5.29, 8.20, -7.33, 2.47, -4.81, 3.55, -1.51, 6.18, 5.58)
        .reshape(n, n).copy();
    DoubleArray wr = bj.newDoubleArray(n);
    DoubleArray wi = bj.newDoubleArray(n);
    DoubleArray vl = bj.newDoubleArray(n, n);
    DoubleArray vr = bj.newDoubleArray(n, n);
    linalg.geev('v', 'v', a, wr, wi, vl, vr);
    ArrayAssert.assertArrayEquals(bj.newComplexVector(Complex.valueOf(2.858132878, 10.7627498307),
        Complex.valueOf(2.858132878, -10.7627498307), Complex.valueOf(-0.6866745133, 4.7042613406),
        Complex.valueOf(-0.6866745133, -4.7042613406), Complex.valueOf(-10.4629167295)),
        toComplex(wr, wi));
  }

  @Test
  public void ormqr() throws Exception {

  }

  @Test
  public void geqrf() throws Exception {
    DoubleArray a = bj.newDoubleVector(0.000000, 2.000000, 2.000000, -1.000000, 2.000000, -1.000000,
        0.000000, 1.500000, 2.000000, -1.000000, 2.000000, -1.000000).reshape(2, 6).transpose();
    DoubleArray tau = bj.newDoubleArray(2);
    linalg.geqrf(a, tau);
    ArrayAssert.assertArrayEquals(bj.newDoubleVector(1, 1.4), tau, 0.01);

    ArrayAssert.assertArrayEquals(bj.newDoubleMatrix(
        new double[][] {new double[] {-4, 2}, new double[] {0.5, 2.5}, new double[] {0.5, 0.286},
            new double[] {0, -0.429}, new double[] {0.5, 0.286}, new double[] {0.5, 0.286}}),
        a, 0.01);

  }

  @Test
  public void syev() throws Exception {
    // @formatter:off
    double[][] expected = {
        {3.292, 0.507,  0.876, 0.176, -0.177},
        {0,     0.891, -1.111, 0.082,  0.185},
        {0,     0,      4.561, 1.671, -0.424},
        {0,     0,      0,     4.877,  1.616},
        {0,     0,      0,     0,      3.54}
    };
    
    double[][] actual = {
        {0.670, -0.200,  0.190, -1.060,  0.460},
        {0.000,  3.820, -0.130,  1.060, -0.480},
        {0.000,  0.000,  3.270,  0.110,  1.100},
        {0.000,  0.000,  0.000,  5.860, -0.980},
        {0.000,  0.000,  0.000,  0.000,  3.54}
    };
    // @formatter:on

    DoubleArray a = bj.newDoubleMatrix(actual);
    double abstol = -1;
    int il = 1;
    int ul = 3;
    double vl = 0;
    double vu = 0;
    int n = a.rows();
    DoubleArray w = bj.newDoubleArray(n);
    DoubleArray z = bj.newDoubleArray(n, 3);
    IntArray isuppz = bj.newIntArray(n);

    int m = linalg.syevr('v', 'i', 'u', a, vl, vu, il, ul, abstol, w, z, isuppz);

    assertEquals(3, m);

    ArrayAssert.assertArrayEquals(bj.newDoubleVector(0.433, 2.145, 3.368), w.get(bj.range(3)),
        0.01);
    ArrayAssert.assertArrayEquals(bj.newDoubleMatrix(expected), a, 0.001);

  }

  @Test
  public void syevd() throws Exception {
    // @formatter:off
    double[][] input = {
        {6.39, 0.13, -8.23, 5.71, -3.18},
        {0.13, 8.37, -4.46, -6.10, 7.21},
        {-8.23, -4.46, -9.58, -9.25, -7.42},
        {5.71, -6.10, -9.25, 3.72, 8.54},
        {-3.18, 7.21, -7.42, 8.54, 2.51}
    };

    double[][] expected = {
        {-0.26 ,  0.31 , -0.74 ,  0.33 ,  0.42},
        {-0.17 , -0.39 , -0.38 , -0.80 ,  0.16},
        {-0.89 ,  0.04 ,  0.09 ,  0.03 , -0.45},
        {-0.29 , -0.59 ,  0.34 ,  0.31 ,  0.60},
        {-0.19 ,  0.63 ,  0.44 , -0.38 ,  0.48}
    };
    // @formatter:on

    DoubleArray actual = bj.newDoubleMatrix(input);
    DoubleArray w = bj.newDoubleArray(actual.size(0));
    linalg.syevd('v', 'u', actual, w);

    ArrayAssert.assertArrayEquals(bj.newDoubleVector(-17.44, -11.96, 6.72, 14.25, 19.84), w, 0.01);
    ArrayAssert.assertArrayEquals(bj.newDoubleMatrix(expected), actual, 0.01);
  }

  @Test
  public void syevr() throws Exception {
    DoubleArray a = bj
        .newDoubleVector(1.96, 0.00, 0.00, 0.00, 0.00, -6.49, 3.80, 0.00, 0.00, 0.00, -0.47, -6.39,
            4.17, 0.00, 0.00, -7.20, 1.50, -1.51, 5.70, 0.00, -0.65, -6.34, 2.67, 1.80, -7.10)
        .reshape(5, 5);
    DoubleArray t = DoubleArray.zeros(5, 5);
    for (int i = 0; i < 5; i++) {
      for (int j = i; j < 5; j++) {
        t.set(i, j, a.get(i, j));
      }
    }
    for (int i = 0; i < 5; i++) {
      for (int j = i; j < 5; j++) {
        t.set(j, i, a.get(i, j));
      }
    }

    System.out.println(t);
    DoubleArray w = bj.newDoubleArray(a.rows());
    System.out.println(a);
    linalg.syev('v', 'u', a, w);

    System.out.println(a);
    System.out.println(w);

    System.out.println(Matrices.toArray(new EigenDecomposition(Matrices.asRealMatrix(t)).getV()));

  }

  @Test
  public void getrf() throws Exception {
    DoubleArray d = bj.newDoubleMatrix(new double[][] {new double[] {1.80, 2.88, 2.05, -0.89},
        new double[] {5.25, -2.95, -0.95, -3.80}, new double[] {1.58, -2.69, -2.9, -1.4},
        new double[] {-1.11, -0.66, -0.59, 0.8}});

    IntArray ipiv1 = bj.newIntArray(4);
    linalg.getrf(d, ipiv1);
    ArrayAssert.assertArrayEquals(bj.newIntVector(2, 2, 3, 4), ipiv1);
  }

  @Test
  public void getri() throws Exception {

  }

  @Test
  public void gelsy() throws Exception {

  }

  @Test
  public void gesv() throws Exception {
    DoubleArray a = bj.newDoubleVector(6.80, -2.11, 5.66, 5.97, 8.23, -6.05, -3.30, 5.36, -4.44,
        1.08, -0.45, 2.58, -2.70, 0.27, 9.04, 8.32, 2.71, 4.35, -7.17, 2.14, -9.67, -5.14, -7.26,
        6.08, -6.87).reshape(5, 5);

    DoubleArray b = bj.newDoubleVector(4.02, 6.19, -8.22, -7.57, -3.03, -1.56, 4.00, -8.67, 1.75,
        2.86, 9.81, -4.09, -4.57, -8.61, 8.99).reshape(5, 3);

    IntArray ipiv = bj.newIntArray(5);
    linalg.gesv(a, ipiv, b);

    ArrayAssert.assertArrayEquals(bj.newIntVector(5, 5, 3, 4, 5), ipiv);
    ArrayAssert
        .assertArrayEquals(bj.newDoubleMatrix(new double[][] {new double[] {-0.80, -0.39, 0.96},
            new double[] {-0.70, -0.55, 0.22}, new double[] {0.59, 0.84, 1.90},
            new double[] {1.32, -0.10, 5.36}, new double[] {0.57, 0.11, 4.04}}), b, 0.01);
    ArrayAssert.assertArrayEquals(
        bj.newDoubleMatrix(new double[][] {new double[] {8.23, 1.08, 9.04, 2.14, -6.87},
            new double[] {0.83, -6.94, -7.92, 6.55, -3.99},
            new double[] {0.69, -0.67, -14.18, 7.24, -5.19},
            new double[] {0.73, 0.75, 0.02, -13.82, 14.19},
            new double[] {-0.26, 0.44, -0.59, -0.34, -3.43}}),
        a, 0.01);
  }

  @Test
  public void gesvd() throws Exception {

  }

  @Test
  public void gesdd() throws Exception {

  }

  private ComplexArray toComplex(DoubleArray r, DoubleArray i) {
    ComplexArray c = bj.newComplexArray(r.getShape());
    for (int j = 0; j < r.size(); j++) {
      c.set(j, Complex.valueOf(r.get(j), i.get(j)));
    }
    return c;
  }

  @Test
  public void testCi() throws Exception {
    final int n = 3;
    // DoubleArray a =
    // bj.newDoubleVector(-1.01, 3.98, 3.30, 4.43, 7.31, 0.86, 0.53, 8.26, 4.96, -6.43, -4.60,
    // -7.04, -3.89, -7.66, -6.16, 3.31, 5.29, 8.20, -7.33, 2.47, -4.81, 3.55, -1.51, 6.18,
    // 5.58).reshape(n, n);
    // System.out.println(a);

    DoubleArray a = bj.newDoubleMatrix(new double[][] {{0, 2, 3}, {2, 0, 2}, {3, 2, 0}});

    DoubleArray wr = bj.newDoubleArray(n);
    DoubleArray wi = bj.newDoubleArray(n);
    DoubleArray vl = bj.newDoubleArray(n, n);
    DoubleArray vr = bj.newDoubleArray(n, n);
    DoubleArray copy = a.copy();
    // linalg.geev('v', 'v', copy, wr, wi, vl, vr);
    linalg.syev('v', 'u', copy, wr);

    wr.sort((i, j) -> Double.compare(j, i));
    System.out.println(copy);
    System.out.println(wr);

    ComplexArray v = bj.newComplexArray(n, n);
    for (int i = 0; i < n; i++) {
      if (Precision.equals(wi.get(i), 0, 1e-6)) {
        v.setColumn(i, vr.getColumn(i).complexArray());
      } else {
        DoubleArray real = vr.getColumn(i);
        DoubleArray imag = vr.getColumn(i + 1);
        v.setColumn(i, toComplex(real, imag));
        v.setColumn(i + 1, toComplex(real, imag.negate()));
        i++;
      }
    }
    // System.out.println(v);

    RealMatrix matrix = Matrices.asRealMatrix(a);
    EigenDecomposition d = new EigenDecomposition(matrix);
    System.out.println(Matrices.toArray(d.getD()));
    System.out.println(Matrices.toArray(d.getV()));
    // System.out.println(d.getEigenvector(0));
    System.out.println(Arrays.toString(d.getRealEigenvalues()));
    // System.out.println(Arrays.toString(d.getImagEigenvalues()));
  }

  @Test
  public void testRank() throws Exception {
    DoubleArray x = bj.range(9).reshape(3, 3).doubleArray();
    System.out.println(x);
    System.out.println(linalg.rank(x));
  }

  @Test
  public void testInv() throws Exception {
    DoubleArray x = bj.newDoubleVector(1.80, 2.88, 2.05, -0.89, 5.25, -2.95, -0.95, -3.80, 1.58,
        -2.69, -2.90, -1.04, -1.11, -0.66, -0.59, 0.80).reshape(4, 4).transpose();
    System.out.println(x);
    System.out.println(linalg.inv(x));
  }

  @Test
  public void testPinv() throws Exception {
    DoubleArray x =
        bj.newDoubleMatrix(new double[][] {new double[] {1, 2, 3}, new double[] {1, 2, 3}});
    DoubleArray p = linalg.pinv(x.transpose());
   // assertArrayEquals(new double[] {0.035714285714285705, 0.03571428571428572, 0.07142857142857141,
    //    0.07142857142857144, 0.10714285714285711, 0.10714285714285715}, p.data(), 1e-6);
  }

  @Test
  public void testSvd() throws Exception {
    DoubleArray x = bj.newDoubleMatrix(
        new double[][] {new double[] {1, 2, 3}, new double[] {2, 3, 8}, new double[] {9, 7, 1}});
    SingularValueDecomposition svd = linalg.svd(x);
  }
}
