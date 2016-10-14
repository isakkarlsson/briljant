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
package org.briljantframework.data.dataframe;

import java.util.HashSet;
import java.util.Set;

import org.briljantframework.data.index.Index;
import org.briljantframework.data.series.AbstractSeries;
import org.briljantframework.data.series.Series;
import org.briljantframework.data.series.Type;
import org.briljantframework.data.series.Types;

/**
 * View a row in a data frame.
 * 
 * @author Isak Karlsson
 */
class RowView extends AbstractSeries {

  private final DataFrame parent;
  private final int row;
  private final Type type;

  RowView(DataFrame parent, int row) {
    this(parent, row, findUnionType(parent));
  }

  RowView(DataFrame parent, int row, Type type) {
    this.parent = parent;
    this.type = type;
    this.row = row;
  }

  @Override
  public Index index() {
    return parent.getColumnIndex();
  }

  // @Override
  // protected void setElement(int index, Object value) {
  // parent.loc().set(row, index, value);
  // }

  @Override
  public Object get(Object key) {
    // todo: consider if row should be a key instead of a location.
    return parent.get(key).values().get(row);
  }

  @Override
  public void set(Object key, Object value) {

  }

  /**
   * For data-frames where the columns have the same type, the type of a record is the same
   * otherwise we return the most generic type (i.e. Object).
   */
  private static Type findUnionType(DataFrame df) {
    Set<Type> types = new HashSet<>();

    // This could be improved by finding the 'highest-common-supertype'.
    for (Series column : df.getColumns()) {
      types.add(column.getType());
    }
    return types.size() == 1 ? types.iterator().next() : Types.OBJECT;
  }

  @Override
  public Type getType() {
    return type;
  }

  // @Override
  // public <T> T getElement(Class<T> cls, int index) {
  // return parent.loc().get(cls, row, index);
  // }
  //
  // @Override
  // public double getDoubleElement(int i) {
  // return parent.loc().getDouble(row, i);
  // }
  //
  // @Override
  // public int getIntElement(int i) {
  // return parent.loc().getInt(row, i);
  // }

  @Override
  public Series reindex(Index index) {
    Series series = newCopyBuilder().build();
    return series.reindex(index);
  }

  @Override
  public Builder newCopyBuilder() {
    return newBuilder().setAll(this);
  }

  @Override
  public Builder newBuilder() {
    return getType().newBuilder();
  }

  @Override
  public Builder newBuilder(int size) {
    return getType().newBuilder(size);
  }

  @Override
  public int size() {
    return parent.columns();
  }
}
