package org.briljantframework.matrix.base;

import org.briljantframework.matrix.AbstractLongArray;
import org.briljantframework.matrix.LongArray;
import org.briljantframework.matrix.api.ArrayFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Isak Karlsson
 */
class BaseLongArray extends AbstractLongArray {

  private long[] data;

  BaseLongArray(ArrayFactory bj, int... shape) {
    super(bj, shape);
    this.data = new long[size()];
  }

  BaseLongArray(ArrayFactory bj, long[] data) {
    super(bj, checkNotNull(data).length);
    this.data = data;
  }

  BaseLongArray(ArrayFactory bj, int offset, int[] shape, int[] stride, long[] data) {
    super(bj, offset, shape, stride);
    this.data = data;
  }

  @Override
  public LongArray newEmptyArray(int... shape) {
    return new BaseLongArray(getMatrixFactory(), shape);
  }

  @Override
  protected LongArray makeView(int offset, int[] shape, int[] stride) {
    return new BaseLongArray(getMatrixFactory(), offset, shape, stride, data);
  }

  @Override
  public boolean isView() {
    return false;
  }

  @Override
  public long getElement(int index) {
    return data[index];
  }

  @Override
  public void setElement(int index, long value) {
    data[index] = value;
  }
}