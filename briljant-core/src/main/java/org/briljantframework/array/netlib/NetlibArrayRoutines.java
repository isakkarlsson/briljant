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

import org.briljantframework.Check;
import org.briljantframework.array.ArrayOperation;
import org.briljantframework.array.BaseArray;
import org.briljantframework.array.DoubleArray;
import org.briljantframework.array.api.AbstractArrayRoutines;
import org.briljantframework.array.api.ArrayBackend;

import com.github.fommil.netlib.BLAS;

/**
 * Array routines with fortran performance.
 * 
 * @author Isak Karlsson
 */
class NetlibArrayRoutines extends AbstractArrayRoutines {

  private final static BLAS blas = BLAS.getInstance();

  NetlibArrayRoutines(ArrayBackend backend) {
    super(backend);
  }

  // @Override
  // public void plus(DoubleArray a, DoubleArray b, DoubleArray out) {
  // Arrays.broadcastWith(a, b, (y, x) -> {
  // Check.size(x, out);
  // axpy(1.0, x, out);
  // });
  // }
  //
  // @Override
  // public void minus(DoubleArray a, DoubleArray b, DoubleArray out) {
  // Arrays.broadcastWith(out, a, (y, x) -> {
  // Check.size(x, out);
  // axpy(-1.0, x, y);
  // });
  // }

  @Override
  public double inner(DoubleArray a, DoubleArray b) {
    if (isContinuousNetlibArray(a) && isContinuousNetlibArray(b)) {
      Check.argument(a.isVector() && b.isVector(), VECTOR_REQUIRED);
      Check.size(a, b);
      int n = a.size();
      return blas.ddot(n, getBackingArray(a), a.getOffset(), getVectorMajorStride(a),
          getBackingArray(b), b.getOffset(), getVectorMajorStride(b));
    } else {
      return super.inner(a, b);
    }
  }

  private double[] getBackingArray(DoubleArray a) {
    return ((NetlibDoubleArray) a).getBackingArray();
  }

  @Override
  public double norm2(DoubleArray a) {
    if (isContinuousNetlibArray(a)) {
      Check.argument(a.isVector(), VECTOR_REQUIRED);
      return blas.dnrm2(a.size(), getBackingArray(a), a.getOffset(), getVectorMajorStride(a));
    } else {
      return super.norm2(a);
    }
  }

  @Override
  public double asum(DoubleArray a) {
    if (isContinuousNetlibArray(a)) {
      Check.argument(a.isVector(), VECTOR_REQUIRED);
      return blas.dasum(a.size(), getBackingArray(a), a.getOffset(), getVectorMajorStride(a));
    } else {
      return super.asum(a);
    }
  }

  @Override
  public int iamax(DoubleArray a) {
    if (isContinuousNetlibArray(a)) {
      Check.argument(a.isVector(), VECTOR_REQUIRED);
      return blas.idamax(a.size(), getBackingArray(a), a.getOffset(), getVectorMajorStride(a));
    } else {
      return super.iamax(a);
    }
  }

  @Override
  public void scal(double alpha, DoubleArray a) {
    if (isContinuousNetlibArray(a) && alpha != 1) {
      Check.argument(a.isVector(), VECTOR_REQUIRED);
      blas.dscal(a.size(), alpha, getBackingArray(a), a.getOffset(), getVectorMajorStride(a));
    } else {
      super.scal(alpha, a);
    }
  }

  @Override
  public void axpy(double alpha, DoubleArray x, DoubleArray y) {
    if (alpha == 0) {
      return;
    } // TODO: 11/01/16 we need alternative treatment of transposed vectors
    if (isContinuousNetlibArray(x) && isContinuousNetlibArray(y)) {
      // TODO: 5/27/16 we need to rework this
      // Check.argument(x.isVector() && y.isVector(), VECTOR_REQUIRED);
      // Check.size(x, y);
      blas.daxpy(x.size(), alpha, getBackingArray(x), x.getOffset(), getVectorMajorStride(x),
          getBackingArray(y), y.getOffset(), getVectorMajorStride(y));
    } else {
      super.axpy(alpha, x, y);
    }
  }

  @Override
  public void gemv(ArrayOperation transA, double alpha, DoubleArray a, DoubleArray x, double beta,
      DoubleArray y) {
    Check.argument(a.isMatrix());
    Check.argument(x.isVector());
    Check.argument(y.isVector());

    if (a instanceof NetlibDoubleArray && a.stride(0) == 1 && a.stride(1) >= a.size(1)
        && x instanceof NetlibDoubleArray && y instanceof NetlibDoubleArray) {

      int m = a.size(transA == ArrayOperation.KEEP ? 0 : 1);
      int n = a.size(transA == ArrayOperation.KEEP ? 1 : 0);
      // TODO: 5/2/16 ensure correctness
      blas.dgemv(transA.getCblasString(), m, n, alpha, getBackingArray(a), a.getOffset(),
          Math.max(1, a.stride(1)), getBackingArray(x), x.getOffset(), x.stride(0), beta,
          getBackingArray(y), y.getOffset(), y.stride(0));
    } else {
      super.gemv(transA, alpha, a, x, beta, y);
    }
  }

  @Override
  public void ger(double alpha, DoubleArray x, DoubleArray y, DoubleArray a) {
    Check.argument(a.isMatrix() && x.isVector() && y.isVector());
    Check.dimension(x.size(), a.rows());
    Check.dimension(y.size(), a.columns());
    if (x instanceof NetlibDoubleArray && y instanceof NetlibDoubleArray
        && a instanceof NetlibDoubleArray && a.stride(0) == 1 && a.stride(1) >= a.size(1)) {
      // TODO: 5/3/16 ensure correctness
      blas.dger(a.rows(), a.columns(), alpha, getBackingArray(x), x.getOffset(), y.stride(0),
          getBackingArray(y), y.getOffset(), y.stride(0), getBackingArray(a), a.getOffset(),
          Math.max(1, a.stride(1)));
    } else {
      super.ger(alpha, x, y, a);
    }
  }

  @Override
  public void gemm(ArrayOperation transA, ArrayOperation transB, double alpha, DoubleArray a,
      DoubleArray b, double beta, DoubleArray c) {
    Check.argument(a.dims() == 2, "'a' has %s dims", a.dims());
    Check.argument(b.dims() == 2, "'b' has %s dims", a.dims());
    Check.argument(c.dims() == 2, "'c' has %s dims", a.dims());

    if (b.size(transB == ArrayOperation.KEEP ? 0 : 1) != a
        .size(transA == ArrayOperation.KEEP ? 1 : 0)) {
      boolean ta = transA == ArrayOperation.KEEP;
      boolean tb = transB == ArrayOperation.KEEP;
      throw new IllegalArgumentException(String.format("a has size (%d, %d), b has size(%d, %d)",
          a.size(ta ? 0 : 1), a.size(ta ? 1 : 0), b.size(tb ? 0 : 1), b.size(tb ? 1 : 0)));
    }
    int m = a.size(transA == ArrayOperation.KEEP ? 0 : 1);
    int n = b.size(transB == ArrayOperation.KEEP ? 1 : 0);
    int k = a.size(transA == ArrayOperation.KEEP ? 1 : 0);

    if (m != c.size(0) || n != c.size(1)) {
      throw new IllegalArgumentException(
          String.format("a has size (%d,%d), b has size (%d,%d), c has size (%d, %d)", m, k, k, n,
              c.size(0), c.size(1)));
    }

    // Issue: is a or b is non-netlib arrays it might be beneficial to copy here if
    // the array is a small view of a large array since the view performs a copy of
    // the large array and, while the copy here might be small.
    a = !a.isView() && a.isContiguous() && a.stride(0) == 1 ? a : a.copy();
    b = !b.isView() && b.isContiguous() && b.stride(0) == 1 ? b : b.copy();
    DoubleArray maybeC =
        c instanceof NetlibDoubleArray && c.isContiguous() && c.stride(0) == 1 ? c : c.copy();


    blas.dgemm(transA.getCblasString(), transB.getCblasString(), m, n, k, alpha, getBackingArray(a),
        a.getOffset(), Math.max(1, a.stride(1)), getBackingArray(b), b.getOffset(),
        Math.max(1, b.stride(1)), beta, getBackingArray(maybeC), maybeC.getOffset(),
        Math.max(1, maybeC.stride(1)));

    // If c was copied, maybeC and c won't be the same instance.
    // To simulate an out parameter, c is assigned the new data if this is the case.
    if (maybeC != c) {
      c.assign(maybeC);
    }
  }

  @Override
  public <T extends BaseArray<T>> void copy(T from, T to) {
    // This is a naive optimization that relies on the fact that netlib arrays expose their
    // internal data representation
    if (from instanceof NetlibDoubleArray && to instanceof NetlibDoubleArray && !from.isView()
        && from.stride(0) == 1 && !to.isView() && to.stride(0) == 1) {
      System.arraycopy(getBackingArray(((NetlibDoubleArray) from)), from.getOffset(),
          getBackingArray(((NetlibDoubleArray) to)), to.getOffset(), from.size());
    } else {
      super.copy(from, to);
    }
  }

  /**
   * Returns true if the given array is an a proper array with the proper strides.
   *
   * @param x the
   * @return
   */
  private boolean isContinuousNetlibArray(DoubleArray x) {
    return x instanceof NetlibDoubleArray && x.stride(0) == 1;
  }

  private int getVectorMajorStride(BaseArray<?> array) {
    switch (array.dims()) {
      case 1:
        return array.stride(0);
      case 2:
        // take the second stride of row-vectors
        if (array.size(0) == 1 && array.size(1) >= 1) {
          return array.stride(1);
        } else if (array.size(0) >= 1 && array.size(1) == 1) {
          return array.stride(0);
        } else {
          return array.stride(0);
        }
      default:
        return array.stride(0);
      // throw new IllegalArgumentException(
      // String.format("Can't get series stride of %dd-array", array.dims()));
    }
  }

}
