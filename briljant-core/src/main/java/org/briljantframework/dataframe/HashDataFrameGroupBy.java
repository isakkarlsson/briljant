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

package org.briljantframework.dataframe;

import org.briljantframework.function.Aggregator;
import org.briljantframework.vector.Vector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * @author Isak Karlsson
 */
class HashDataFrameGroupBy implements DataFrameGroupBy {

  private final HashMap<Object, Vector> groups;
  private final DataFrame dataFrame;
  private final Function<?, ?> transform = a -> a;

  HashDataFrameGroupBy(DataFrame dataFrame, HashMap<Object, ? extends Vector.Builder> groups) {
    this.dataFrame = dataFrame;
    this.groups = new HashMap<>();
    for (Map.Entry<Object, ? extends Vector.Builder> e : groups.entrySet()) {
      this.groups.put(e.getKey(), e.getValue().build());
    }
  }

  @Override
  public Iterator<Group> iterator() {
    return new Iterator<Group>() {
      private final Iterator<Map.Entry<Object, Vector>> it = groups().iterator();

      @Override
      public boolean hasNext() {
        return it.hasNext();
      }

      @Override
      public Group next() {
        Map.Entry<Object, Vector> e = it.next();
        Object key = e.getKey();
        Vector indices = e.getValue();
        return new Group(key, createDataFrame(indices));
      }
    };
  }

  @Override
  public Set<Map.Entry<Object, Vector>> groups() {
    return groups.entrySet();
  }

  @Override
  public DataFrame get(Object key) {
    Vector indices = groups.get(key);
    if (indices == null) {
      throw new IllegalArgumentException(String.format("Missing key: %s", key));
    }
    return createDataFrame(indices);
  }

  protected DataFrame createDataFrame(Vector indices) {
    DataFrame.Builder builder = dataFrame.newBuilder();
    if (indices.size() > 0) {
      for (int j = 0; j < dataFrame.columns(); j++) {
        builder.addColumnBuilder(dataFrame.getType(j));
        for (int i = 0; i < indices.size(); i++) {
          builder.set(i, j, dataFrame, indices.getAsInt(i), j);
        }
      }
    }
    DataFrame df = builder.build();
    df.setColumnIndex(dataFrame.getColumnIndex());
    return df;
  }

  @Override
  public DataFrame aggregate(Function<Vector, Object> function) {
    DataFrame.Builder builder = dataFrame.newBuilder();
    Index.Builder recordIndex = new HashIndex.Builder();
    Index.Builder columnIndex = new HashIndex.Builder();
    for (int j = 0; j < dataFrame.columns(); j++) {
      columnIndex.add(dataFrame.getColumnIndex().get(j));
    }
    int row = 0;
    for (Map.Entry<Object, Vector> e : groups()) {
      recordIndex.add(e.getKey());
      Vector groupIndex = e.getValue();
      int column = 0;
      for (int j = 0; j < dataFrame.columns(); j++) {
        Vector col = dataFrame.get(j);
        Vector.Builder reduce = col.newBuilder(groupIndex.size());
        for (int i = 0; i < groupIndex.size(); i++) {
          reduce.set(i, col, groupIndex.getAsInt(i));
        }
        builder.set(row, column++, function.apply(reduce.build()));
      }
      row += 1;
    }

    return finalizeDataFrame(builder, recordIndex, columnIndex);
  }

  @Override
  public <T, C> DataFrame aggregate(Class<? extends T> cls,
                                    Aggregator<? super T, ? extends T, C> aggregator) {
    DataFrame.Builder builder = dataFrame.newBuilder();
    Index.Builder recordIndex = new HashIndex.Builder();
    Index.Builder columnIndex = new HashIndex.Builder();
    for (int j = 0; j < dataFrame.columns(); j++) {
      if (cls.isAssignableFrom(dataFrame.getType(j).getDataClass())) {
        columnIndex.add(dataFrame.getColumnIndex().get(j));
      }
    }

    int row = 0;
    for (Map.Entry<Object, Vector> e : groups.entrySet()) {
      recordIndex.add(e.getKey());
      int colIndex = 0;
      for (int j = 0; j < dataFrame.columns(); j++) {
        if (cls.isAssignableFrom(dataFrame.getType(j).getDataClass())) {
          Vector groupIndex = e.getValue();
          Vector col = dataFrame.get(j);
          C c = aggregator.supplier().get();
          for (int i = 0; i < groupIndex.size(); i++) {
            aggregator.accumulator().accept(c, col.get(cls, groupIndex.getAsInt(i)));
          }
          T t = aggregator.finisher().apply(c);
          builder.set(row, colIndex++, t);
        }
      }
      row++;
    }
    return finalizeDataFrame(builder, recordIndex, columnIndex);
  }

  @Override
  public <T> DataFrameGroupBy transform(Class<T> cls, UnaryOperator<T> op) {
    return null;
  }

  protected DataFrame finalizeDataFrame(DataFrame.Builder builder, Index.Builder recordIndex,
                                        Index.Builder columnIndex) {
    DataFrame df = builder.build();
    df.setRecordIndex(recordIndex.build());
    df.setColumnIndex(columnIndex.build());
    return df;
  }
}