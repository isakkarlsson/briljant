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

import java.util.Objects;

import net.mintern.primitive.Primitive;
import net.mintern.primitive.comparators.LongComparator;

import org.briljantframework.array.AbstractLongArray;
import org.briljantframework.array.LongArray;
import org.briljantframework.array.api.ArrayBackend;

/**
 * @author Isak Karlsson
 */
class NetlibLongArray extends AbstractLongArray {

  private long[] data;

  NetlibLongArray(ArrayBackend bj, int[] shape) {
    super(bj, shape);
    this.data = new long[size()];
  }

  NetlibLongArray(ArrayBackend bj, long[] data) {
    super(bj, new int[] {Objects.requireNonNull(data).length});
    this.data = data;
  }

  private NetlibLongArray(ArrayBackend bj, int offset, int[] shape, int[] stride, long[] data) {
    super(bj, offset, shape, stride);
    this.data = data;
  }

  @Override
  public void sort(LongComparator cmp) {
    if (!isView() && isVector() && stride(0) == 1) {
      Primitive.sort(data(), getOffset(), size(), cmp);
    } else {
      super.sort(cmp);
    }
  }

  @Override
  public void setElement(int index, long value) {
    data[index] = value;
  }

  @Override
  public long getElement(int index) {
    return data[index];
  }

  @Override
  public LongArray asView(int offset, int[] shape, int[] stride) {
    return new NetlibLongArray(getArrayBackend(), offset, shape, stride, data);
  }

  @Override
  public LongArray newEmptyArray(int... shape) {
    return new NetlibLongArray(getArrayBackend(), shape);
  }

  @Override
  protected int elementSize() {
    return data.length;
  }
}
