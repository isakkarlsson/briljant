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

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;

import org.briljantframework.Check;
import org.briljantframework.array.*;
import org.briljantframework.array.Arrays;
import org.briljantframework.data.Is;
import org.briljantframework.data.index.HashIndex;
import org.briljantframework.data.index.Index;
import org.briljantframework.data.index.RangeIndex;
import org.briljantframework.data.reader.DataEntry;
import org.briljantframework.data.reader.EntryReader;
import org.briljantframework.data.series.Convert;
import org.briljantframework.data.series.Series;
import org.briljantframework.data.series.Storage;
import org.briljantframework.data.series.TypeInferenceBuilder;
import org.briljantframework.util.primitive.IntList;

/**
 * Provides a skeletal implementation of a {@link DataFrame}.
 *
 * @author Isak Karlsson
 */
public abstract class AbstractDataFrame implements DataFrame {

  private final LocationIndexer locationIndexer = new LocationIndexerImpl();
  private final LabelIndexer labelIndexer = new LabelIndexerImpl();

  private ColumnList columnList = null; // Lazy initialization
  private RecordList recordList = null; // Lazy initialization
  private Index index = null;
  private Index columnIndex = null;

  /**
   * @param columnIndex {@code null} is allowed
   * @param index {@code null} is allowed
   */
  protected AbstractDataFrame(Index columnIndex, Index index) {
    this.columnIndex = columnIndex;
    this.index = index;
  }

  protected AbstractDataFrame() {
    this(null, null);
  }

  @Override
  public final DataFrame indexOn(Object key) {
    HashIndex index = HashIndex.of(get(key)); // fail fast. throws on duplicates
    DataFrame.Builder builder = newEmptyBuilder();
    for (Object columnKey : getIndex()) {
      if (!Objects.equals(columnKey, key)) {
        builder.setColumn(columnKey, get(columnKey));
      }
    }
    DataFrame df = builder.build();
    df.setIndex(index);
    return df;
  }

  @Override
  public final <T> DataFrame map(Class<T> cls, Function<? super T, ?> mapper) {
    DataFrame.Builder builder = newEmptyBuilder();
    for (int j = 0; j < columns(); j++) {
      Series column = getColumnElement(j);

      // Not affected by ISSUE#7
      Series.Builder newColumn = new TypeInferenceBuilder();
      for (int i = 0, size = column.size(); i < size; i++) {
        Object transformed = mapper.apply(column.values().get(cls, i));
        if (Is.NA(transformed)) {
          newColumn.addNA();
        } else {
          newColumn.add(transformed);
        }
      }
      builder.addColumn(newColumn);
    }

    DataFrame df = builder.build();
    df.setIndex(getIndex());
    df.setColumnIndex(getColumnIndex());
    return df;
  }

  @Override
  public final DataFrame apply(Function<? super Series, ? extends Series> transform) {
    DataFrame.Builder builder = newEmptyBuilder();
    for (Object columnKey : this.getColumnIndex()) {
      Series column = get(columnKey);
      builder.setColumn(columnKey, transform.apply(column));
    }
    DataFrame df = builder.build();
    df.setIndex(getIndex());
    return df;
  }

  @Override
  public final <T, C> DataFrame apply(Class<T> cls,
      Collector<? super T, C, ? extends Series> collector) {
    DataFrame.Builder builder = newEmptyBuilder();
    for (Object columnKey : getColumnIndex().keySet()) {
      Series column = get(columnKey);
      C accumulator = collector.supplier().get();
      for (int i = 0, size = column.size(); i < size; i++) {
        collector.accumulator().accept(accumulator, column.values().get(cls, i));
      }
      Series transformed = collector.finisher().apply(accumulator);
      builder.setColumn(columnKey, transformed);
    }
    return builder.build();
  }

  @Override
  public final <T> Series reduce(Class<? extends T> cls, T init, BinaryOperator<T> op) {
    Series.Builder builder = Series.Builder.of(cls);
    for (Object columnKey : this.getColumnIndex()) {
      Series column = get(columnKey);

      // TODO: 4/25/16 Fix this?
      // if (column.getType().isAssignableTo(cls)) {
      T result = init;
      for (int i = 0, size = column.size(); i < size; i++) {
        result = op.apply(result, column.values().get(cls, i));
      }
      builder.set(columnKey, result);
      // }
    }
    return builder.build();
  }

  @Override
  public final DataFrameGroupBy groupBy(Object columnKey) {
    HashMap<Object, IntList> groups = new LinkedHashMap<>();
    Series column = get(columnKey);
    Storage loc = column.values();
    for (int i = 0, size = column.size(); i < size; i++) {
      groups.computeIfAbsent(loc.get(Object.class, i), a -> new IntList()).add(i);
    }
    return new HashDataFrameGroupBy(this, groups, columnKey);
  }

  @Override
  public final DataFrameGroupBy groupBy(Collection<?> keys) {
    return groupBy(Function.identity(), keys);
  }

  @Override
  public final <T> DataFrameGroupBy groupBy(Class<T> cls, Function<? super T, Object> map,
      Object columnKey) {
    HashMap<Object, IntList> groups = new LinkedHashMap<>();
    Series column = get(columnKey);
    Storage loc = column.values();
    for (int i = 0, size = column.size(); i < size; i++) {
      T value = loc.get(cls, i);
      // Ignore NA values (group them separately)
      groups.computeIfAbsent(Is.NA(value) ? value : map.apply(value), a -> new IntList()).add(i);
    }
    return new HashDataFrameGroupBy(this, groups, columnKey);
  }

  @Override
  public DataFrameGroupBy groupBy(Function<? super Series, Object> combiner, Collection<?> keys) {
    HashMap<Object, IntList> groups = new LinkedHashMap<>();
    for (int i = 0, size = rows(); i < size; i++) {
      Series.Builder cs = new TypeInferenceBuilder();
      for (Object key : keys) {
        cs.add(get(key).values().get(i));
      }
      groups.computeIfAbsent(combiner.apply(cs.build()), a -> new IntList()).add(i);
    }
    return new HashDataFrameGroupBy(this, groups, keys);
  }

  @Override
  public final DataFrameGroupBy groupBy(UnaryOperator<Object> keyFunction) {
    return groupBy(Object.class, keyFunction);
  }

  @Override
  public <T> DataFrameGroupBy groupBy(Class<T> cls, Function<? super T, ?> keyFunction) {
    HashMap<Object, IntList> groups = new LinkedHashMap<>();
    for (Index.Entry entry : getIndex().indexSet()) {
      T key = Convert.to(cls, entry.getKey());
      groups.computeIfAbsent(Is.NA(key) ? key : keyFunction.apply(key), // ignore NA keys
          a -> new IntList()).add(entry.getValue());
    }
    return new HashDataFrameGroupBy(this, groups);
  }

  @Override
  public final Series get(Object key) {
    return ImmutableIndexSeries.newInstance(getColumnElement(getColumnIndex().getLocation(key)),
        getIndex());
  }

  @Override
  public void set(Object key, Series column) {
    int position;
    if (getColumnIndex().contains(key)) {
      position = getColumnIndex().getLocation(key);
    } else {
      Index.Builder columnIndex = getColumnIndex().newCopyBuilder();
      position = columnIndex.getOrAdd(key);
      this.columnIndex = columnIndex.build();
    }
    setColumnElement(position, column);

    // TODO: 5/11/16 Check size; align index etc.
  }

  @Override
  public void setAll(Map<?, Series> columns) {
    for (Map.Entry<?, Series> entry : columns.entrySet()) {
      set(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public DataFrame getAll(Collection<?> keys) {
    DataFrame.Builder builder = newEmptyBuilder();
    for (Object key : keys) {
      builder.setColumn(key, getColumnElement(getColumnIndex().getLocation(key)).newCopyBuilder());
    }
    DataFrame df = builder.build();
    df.setIndex(getIndex());
    return df;
  }

  @Override
  public DataFrame drop(Object key) {
    return dropAll(Collections.singleton(key));
  }

  @Override
  public DataFrame dropAll(Collection<?> keys) {
    if (keys.isEmpty()) {
      return this;
    }
    DataFrame.Builder builder = newEmptyBuilder();
    Set<Object> drop = new HashSet<>(keys);
    getColumnIndex().stream().filter(k -> !drop.contains(k)).forEach(k -> builder.setColumn(k,
        getColumnElement(getColumnIndex().getLocation(k)).newCopyBuilder()));
    DataFrame df = builder.build();
    df.setIndex(getIndex());
    return df;
  }

  @Override
  public DataFrame dropIf(Predicate<? super Series> predicate) {
    Builder builder = newEmptyBuilder();
    for (Object columnKey : getColumnIndex().keySet()) {
      Series column = get(columnKey);
      if (predicate.test(column)) {
        builder.setColumn(columnKey, column.newCopyBuilder());
      }
    }
    DataFrame df = builder.build();
    df.setIndex(getIndex());
    return df;
  }

  @Override
  public final DataFrame filter(Collection<?> keys) {
    DataFrame.Builder builder = newEmptyBuilder();
    for (Object recordKey : keys) {
      builder.setRow(recordKey, ix().getRow(recordKey).newCopyBuilder());
    }
    DataFrame df = builder.build();
    df.setColumnIndex(getColumnIndex());
    return df;
  }

  @Override
  public Series getDiagonal() {
    Series.Builder builder = Series.Builder.of(Object.class);
    for (int i = 0; i < rows(); i++) {
      builder.add(loc().get(Object.class, i, i));
    }
    return builder.build();
  }

  @Override
  public <T> BooleanArray where(Class<T> cls, Predicate<? super T> predicate) {
    BooleanArray array = Arrays.booleanArray(rows(), columns());
    for (int i = 0; i < rows(); i++) {
      for (int j = 0; j < columns(); j++) {
        array.set(i, j, predicate.test(loc().get(cls, i, j)));
      }
    }
    return array;
  }

  @Override
  public DataFrame get(BooleanArray array) {
    DataFrame.Builder builder = newEmptyBuilder(true);
    DataFrame df;
    if (array.isMatrix()) { // Select values; setting false values to NA
      Check.argument(array.rows() == rows() && array.columns() == columns(), "Illegal shape");
      for (int j = 0; j < array.columns(); j++) {
        Series column = loc().get(j);
        Series.Builder columnBuilder = column.newBuilder();
        for (int i = 0; i < array.rows(); i++) {
          if (array.get(i, j)) {
            columnBuilder.addFromLocation(column, i);
          } else {
            columnBuilder.addNA();
          }
        }
        builder.setColumn(getColumnIndex().get(j), columnBuilder);
      }

      df = builder.build();
      df.setIndex(getIndex());
    } else if (array.isVector()) { // Select rows;
      for (int i = 0; i < array.size(); i++) {
        if (array.get(i)) {
          builder.setRow(getIndex().get(i), loc().getRow(i).newCopyBuilder());
        }
      }
      df = builder.build();
      df.setColumnIndex(getColumnIndex());
    } else {
      throw new IllegalArgumentException("Illegal array dimension " + array.dims());
    }

    return df;
  }

  @Override
  public void set(BooleanArray array, Object value) {
    if (array.isMatrix()) { // Select values; setting false values to NA
      Check.argument(array.rows() == rows() && array.columns() == columns(), "Illegal shape");
      for (int j = 0; j < array.columns(); j++) {
        Series column = loc().get(j);
        for (int i = 0; i < array.rows(); i++) {
          if (array.get(i, j)) {
            column.values().set(i, value);
          }
        }
      }
    } else if (array.isVector()) { // Select rows;
      for (int i = 0; i < array.size(); i++) {

        // Object key = getIndex().get(i);
        if (array.get(i)) {
          Series row = loc().getRow(i);
          for (int j = 0; j < row.size(); j++) {
            row.values().set(j, value);
          }
          // builder.setRow(key, Series.repeat(value, columns()).newCopyBuilder());
        }
      }
      // df = builder.build();
      // df.setColumnIndex(getColumnIndex());
    } else {
      throw new IllegalArgumentException("Illegal array dimension " + array.dims());
    }
  }

  @Override
  public final DataFrame limit(int n) {
    Check.argument(n >= 0 && n <= rows(), "Illegal number of records");
    if (n == rows()) {
      return this;
    }
    DataFrame.Builder builder = newEmptyBuilder();
    int i = 0;
    for (Object key : getIndex().keySet()) {
      if (i >= n) {
        break;
      }
      builder.setRow(key, ix().getRow(key).newCopyBuilder());
      i++;
    }
    DataFrame df = builder.build();
    df.setColumnIndex(getColumnIndex());
    return df;
  }

  @Override
  public DataFrame transpose() {
    DataFrame.Builder builder = newEmptyBuilder();
    for (Object columnKey : getColumnIndex().keySet()) {
      builder.setRow(columnKey, get(columnKey).newCopyBuilder());
    }
    DataFrame df = builder.build();
    df.setColumnIndex(getIndex());
    return df;
  }

  @Override
  public void set(Object row, Object col, Object value) {
    setElement(getIndex().getLocation(row), getIndex().getLocation(col), value);
  }

  @Override
  public final DataFrame filter(Predicate<? super Series> predicate) {
    DataFrame.Builder builder = newEmptyBuilder();
    for (Object recordKey : getIndex().keySet()) {
      Series record = ix().getRow(recordKey);
      if (predicate.test(record)) {
        builder.setRow(recordKey, record.newCopyBuilder());
      }
    }

    DataFrame df = builder.build();
    df.setColumnIndex(getColumnIndex());
    return df;
  }

  @Override
  public final List<Series> getColumns() {
    if (columnList == null) {
      columnList = new ColumnList();
    }
    return columnList;
  }

  @Override
  public final List<Series> getRows() {
    if (recordList == null) {
      recordList = new RecordList();
    }
    return recordList;
  }

  @Override
  public final <T> T get(Class<T> cls, Object row, Object col) {
    return getElement(cls, getIndex().getLocation(row), getColumnIndex().getLocation(col));
  }

  @Override
  public final double getDouble(Object row, Object col) {
    return getDoubleElement(getIndex().getLocation(row), getColumnIndex().getLocation(col));
  }

  @Override
  public final int getInt(Object row, Object col) {
    return getIntElement(getIndex().getLocation(row), getColumnIndex().getLocation(col));
  }

  @Override
  public final boolean isNA(Object row, Object col) {
    return isElementNa(getIndex().getLocation(row), getColumnIndex().getLocation(col));
  }

  @Override
  public final DataFrame copy() {
    return reindex(getColumnIndex(), getIndex());
  }

  @Override
  public final DataFrame resetIndex() {
    Check.state(!getColumnIndex().contains("index"),
        "cannot reset index. Column 'index' already exists");
    DataFrame.Builder builder = newEmptyBuilder();
    Series.Builder indexColumn = new TypeInferenceBuilder();
    getIndex().keySet().forEach(indexColumn::add);
    builder.setColumn("index", indexColumn);
    for (Object columnKey : getColumnIndex().keySet()) {
      builder.setColumn(columnKey, get(columnKey).newCopyBuilder());
    }

    return builder.build();
  }

  @Override
  public final Index getIndex() {
    if (index == null) {
      index = new RangeIndex(0, rows());
    }
    return index;
  }

  @Override
  public final void setIndex(Index index) {
    Objects.requireNonNull(index);
    Check.dimension(rows(), index.size());
    this.index = index;
  }

  public final Index getColumnIndex() {
    if (columnIndex == null) {
      columnIndex = new RangeIndex(0, columns());
    }
    return columnIndex;
  }

  @Override
  public final void setColumnIndex(Index index) {
    Objects.requireNonNull(index);
    Check.dimension(columns(), index.size());
    this.columnIndex = index;
  }

  @Override
  public final LocationIndexer loc() {
    return locationIndexer;
  }

  @Override
  public LabelIndexer ix() {
    return labelIndexer;
  }

  @Override
  public final Series reduce(Function<Series, ?> op) {
    // ISSUE#7 use an improved TypeInferenceBuilder here
    Series.Builder builder = new TypeInferenceBuilder();
    for (Object columnKey : this.getColumnIndex()) {
      Series column = get(columnKey);
      Object value = op.apply(column);
      if (Is.NA(value)) {
        builder.setNA(columnKey);
      } else {
        builder.set(columnKey, value);
      }
    }
    return builder.build();
  }

  @Override
  public final <T, C> Series collect(Class<T> cls, Collector<? super T, C, ?> collector) {
    // Affected by ISSUE#7
    Series.Builder builder = new TypeInferenceBuilder();
    return doCollect(cls, collector, builder);
  }

  private <T, C> Series doCollect(Class<T> cls, Collector<? super T, C, ?> collector,
      Series.Builder builder) {

    for (Object columnKey : this.getColumnIndex()) {
      Series column = get(columnKey);
      // if (column.getType().isAssignableTo(cls)) {
      C accumulator = collector.supplier().get();
      for (int i = 0, size = column.size(); i < size; i++) {
        collector.accumulator().accept(accumulator, column.values().get(cls, i));
      }
      builder.set(columnKey, collector.finisher().apply(accumulator));
      // }
    }
    return builder.build();
  }

  /**
   * Returns true if value at {@code row, column} is {@code NA}.
   *
   * @param row the row
   * @param column the column
   * @return true or false
   */
  protected abstract boolean isElementNa(int row, int column);

  /**
   * Get value at {@code row} and {@code column} as {@code int}.
   *
   * @param row the row
   * @param column the column
   * @return the value
   */
  protected abstract int getIntElement(int row, int column);

  /**
   * Get value at {@code row} and {@code column} as {@code double}.
   *
   * @param row the row
   * @param column the column
   * @return the value
   */
  protected abstract double getDoubleElement(int row, int column);

  /**
   * Get value at {@code row} and {@code column} as an instance of {@code T}. If conversion fails,
   * return {@code NA} as defined by {@link org.briljantframework.data.Na#of(Class)}. The conversion
   * is performed according to the convention found in
   * {@link org.briljantframework.data.series.Convert#to(Class, Object)}
   *
   * @param <T> the type of the returned value
   * @param cls the class
   * @param row the row
   * @param column the column
   * @return an instance of {@code T}
   */
  protected abstract <T> T getElement(Class<T> cls, int row, int column);

  protected abstract Series getRowElement(int index);

  /**
   * Returns the column at {@code index}. This implementation supplies a view into the underlying
   * data frame.
   *
   * @param index the index
   * @return a view of column {@code index}
   */
  protected abstract Series getColumnElement(int index);

  protected abstract void setColumnElement(int pos, Series column);

  protected abstract void setRowElement(int pos, Series row);

  protected abstract void setElement(int r, int c, Object element);

  @Override
  public final int hashCode() {
    return Objects.hash(getColumnIndex(), getIndex(), getColumns());
  }

  @Override
  public final boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }

    if (obj.getClass().equals(getClass())) {
      DataFrame o = (DataFrame) obj;
      if (o.rows() == rows() && getColumnIndex().equals(o.getColumnIndex())
          && getIndex().equals(o.getIndex())) {
        for (Object columnKey : this.getColumnIndex()) {
          if (!get(columnKey).equals(o.get(columnKey))) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  protected DataFrame getIndexElement(IntArray indices) {
    DataFrame.Builder builder = newEmptyBuilder();
    Index.Builder columnIndex = getColumnIndex().newBuilder();
    for (int i = 0; i < indices.size(); i++) {
      columnIndex.add(getColumnIndex().get(indices.get(i)));
      builder.addColumn(getColumnElement(indices.get(i)));
    }
    DataFrame df = builder.build();
    df.setIndex(getIndex());
    df.setColumnIndex(columnIndex.build());
    return df;
  }

  /**
   * @param indexes
   * @return
   */
  protected DataFrame getRowIndexElement(IntArray indexes) {
    Builder builder = newEmptyBuilder();
    for (Integer index : indexes) {
      builder.setRow(getIndex().get(index), getRowElement(index).newCopyBuilder());
    }
    DataFrame df = builder.build();
    df.setColumnIndex(getColumnIndex());
    return df;
  }

  /**
   * Constructs a new DataFrame by dropping {@code index}.
   *
   * @param index the index
   * @return a new data frame as created by {@link #newCopyBuilder()}
   */
  protected abstract DataFrame dropColumnElement(int index);

  /**
   * Constructs a new DataFrame by dropping the columns in {@code indexes}.
   *
   * This implementations rely on {@link #newEmptyBuilder()} returning a builder and that
   * {@link org.briljantframework.data.dataframe.DataFrame.Builder#addColumn(Series)} adds a series.
   *
   * @param indexes collection of indexes
   * @return a new data frame as created by {@link #newEmptyBuilder()}
   */
  protected DataFrame dropColumnIndexElement(IntArray indexes) {
    indexes.sort();
    Builder builder = newEmptyBuilder();
    for (int i = 0; i < columns(); i++) {
      if (Arrays.binarySearch(indexes, i) < 0) {
        builder.setColumn(getColumnIndex().get(i), getColumnElement(i).newCopyBuilder());
      }
    }
    DataFrame df = builder.build();
    df.setIndex(getIndex());
    return df;
  }

  /**
   * Returns a tabular string representation of this DataFrame.
   *
   * @return the string representation
   */
  @Override
  public final String toString() {
    return DataFrames.toString(this);
  }

  /**
   * This class provides a skeletal implementation of the
   * {@link org.briljantframework.data.dataframe.DataFrame.Builder} interface to minimize the effort
   * required to implement this interface, including the handling of key and index-based setters.
   *
   * <p>
   * To implement this builder, the programmer needs to implement the following abstract methods
   * (the documentation for each method provides information on how to implement them):
   * <ul>
   * <li>{@link #setNaAt(int, int)}</li>
   * <li>{@link #setElement(int, int, Object)}</li>
   * <li>{@link #setColumnElement(int, Series.Builder)}</li>
   * <li>{@link #setRowElement(int, Series.Builder)}</li>
   * <li>{@link #setElementFromLocation(int, int, Series, int)}</li>
   * <li>{@link #readEntry(org.briljantframework.data.reader.DataEntry)}</li>
   * </ul>
   *
   * and the following methods from the
   * {@link org.briljantframework.data.dataframe.DataFrame.Builder} interface
   * <ul>
   * <li>{@link #getRows()}</li>
   * <li>{@link #getColumns()}</li>
   * <li>{@link #build()}</li>
   * </ul>
   *
   * <h3>Notes for object keys</h3>
   * <p>
   * For object keys, the position can be retrieved using {@link #getOrAddColumnIndex(Object)} and
   * {@link #getOrAddIndex(Object)}
   *
   * <h3>Notes for the abstract methods</h3>
   * <p>
   * If the indexes are larger than {@link #getRows()} and {@link #getColumns()} respectively
   * additional rows and columns, filled with {@code NA}, are inserted between the specified indexes
   * and the current size.
   */
  protected static abstract class AbstractBuilder implements Builder {

    /**
     * The column index. When getting the index of a key, prefer
     * {@link #getOrAddColumnIndex(Object)}.
     */
    private Index.Builder columnIndexBuilder;

    /**
     * The record index. When getting the index of a key, prefer {@link #getOrAddIndex(Object)}.
     */
    private Index.Builder rowIndexBuilder;

    /**
     * Provide initial indexes
     *
     * @param from the dataframe to copy
     */
    protected AbstractBuilder(DataFrame from) {
      Index ci = from.getColumnIndex();
      Index ri = from.getIndex();
      this.columnIndexBuilder = ci instanceof RangeIndex ? null : ci.newCopyBuilder();
      this.rowIndexBuilder = ri instanceof RangeIndex ? null : ri.newCopyBuilder();
    }

    /**
     * Implicit default constructor for constructing default {@code int}-based indexes
     */
    protected AbstractBuilder() {}

    /**
     * Extend both column and record index with the specified number of entries.
     */
    private void extendIndex(int record, int columns) {
      extendIndex(record);
      extendColumnIndex(columns);
    }

    /**
     * Extend the column index with the specified number of entries. For example, if the index
     * contains {@code 3} items and the argument is {@code 5}, index {@code 4} and {@code 5} will be
     * added.
     */
    private void extendColumnIndex(int c) {
      if (columnIndexBuilder != null) {
        columnIndexBuilder.extend(c);
      }
    }

    /**
     * See explanation of {@link #extendColumnIndex(int)}
     */
    private void extendIndex(int r) {
      if (rowIndexBuilder != null) {
        rowIndexBuilder.extend(r);
      }
    }

    protected final Index getIndex(int rows) {
      if (rowIndexBuilder == null) {
        return new RangeIndex(0, rows);
      } else {
        return rowIndexBuilder.build();
      }
    }

    protected Index getColumnIndex(int columns) {
      if (columnIndexBuilder == null) {
        return new RangeIndex(0, columns);
      } else {
        return columnIndexBuilder.build();
      }
    }

    /**
     * Set the element at the specified position to {@code NA}.
     *
     * @param r the row position
     * @param c the column position
     */
    protected abstract void setNaAt(int r, int c);

    protected abstract void removeAt(int c);

    protected abstract void removeRowElement(int r);

    protected abstract void swapAt(int a, int b);

    protected abstract void swapRowElement(int a, int b);

    @Override
    public final Builder setFrom(Object tr, Object tc, DataFrame from, Object fr, Object fc) {
      int r = getOrAddIndex(tr);
      int c = getOrAddColumnIndex(tc);
      setElementFrom(r, c, from, from.getIndex().getLocation(fr),
          from.getColumnIndex().getLocation(fc));
      return this;
    }

    @Override
    public final Builder setFrom(Object row, Object column, Series from, Object key) {
      int r = getOrAddIndex(row);
      int c = getOrAddColumnIndex(column);
      setElementFromLocation(r, c, from, from.index().getLocation(key));
      return this;
    }

    @Override
    public final Builder set(Object row, Object column, Object value) {
      int r = getOrAddIndex(row);
      int c = getOrAddColumnIndex(column);
      setElement(r, c, value);
      return this;
    }

    @Override
    public final Builder setColumn(Object key, Series.Builder columnBuilder) {
      int columnIndex = getOrAddColumnIndex(key);
      extendIndex(columnBuilder.size());
      setColumnElement(columnIndex, columnBuilder);
      return this;
    }

    @Override
    public final DataFrame.Builder addColumn(Series.Builder columnBuilder) {
      extendIndex(columnBuilder.size());
      extendColumnIndex(columns());
      setColumnElement(columns(), columnBuilder);
      return this;
    }

    @Override
    public final Builder setRow(Object key, Series.Builder rowBuilder) {
      int index = getOrAddIndex(key);
      extendColumnIndex(rowBuilder.size());
      setRowElement(index, rowBuilder);
      return this;
    }

    @Override
    public final Builder addRow(Series.Builder rowBuilder) {
      extendIndex(rows());
      extendColumnIndex(rowBuilder.size());
      setRowElement(rows(), rowBuilder);
      return this;
    }

    @Override
    public final Builder readAll(EntryReader entryReader) {
      int entries = rows();
      while (entryReader.hasNext()) {
        DataEntry entry = entryReader.next();
        extendColumnIndex(entry.size());
        entries++;
        readEntry(entry);
      }
      extendIndex(entries);

      return this;
    }

    @Override
    public final Builder read(DataEntry entry) {
      int rows = rows();
      readEntry(entry);
      extendIndex(rows + 1);
      return this;
    }

    private void initRowIndexBuilder() {
      if (rowIndexBuilder == null) {
        rowIndexBuilder = new RangeIndex.Builder(rows());
      }
    }

    private void initColumnIndexBuilder() {
      if (columnIndexBuilder == null) {
        columnIndexBuilder = new RangeIndex.Builder(columns());
      }
    }

    private int getOrAddColumnIndex(Object key) {
      initColumnIndexBuilder();
      return columnIndexBuilder.getOrAdd(key);
    }

    private int getOrAddIndex(Object key) {
      initRowIndexBuilder();
      return rowIndexBuilder.getOrAdd(key);
    }

    /**
     * Set the element at the specified position to the specified value as defined by
     * {@link org.briljantframework.data.series.Convert#to(Class, Object)}
     *
     * @param r the row location
     * @param c the column location
     */
    protected abstract void setElement(int r, int c, Object value);

    protected abstract void setColumnElement(int c, Series.Builder builder);

    protected abstract void setRowElement(int index, Series.Builder builder);

    protected abstract void setElementFromLocation(int r, int c, Series from, int i);

    protected void setElementFrom(int tr, int tc, DataFrame from, int fr, int fc) {
      setElementFromLocation(tr, tc, from.loc().get(fc), fr);
    }

    protected abstract void readEntry(DataEntry entry);
  }

  private class LabelIndexerImpl implements LabelIndexer {
    @Override
    public DataFrame get(Collection<?> rows, Collection<?> columns) {
      rows = rows == BasicIndex.ALL ? getIndex() : rows;
      columns = columns == BasicIndex.ALL ? getColumnIndex() : columns;
      DataFrame.Builder builder = newEmptyBuilder();
      for (Object row : rows) {
        for (Object column : columns) {
          builder.set(row, column, AbstractDataFrame.this.get(row, column));
        }
      }
      return builder.build();
    }

    @Override
    public void set(Collection<?> rows, Collection<?> columns, DataFrame values) {
      rows = rows == BasicIndex.ALL ? getIndex() : rows;
      columns = columns == BasicIndex.ALL ? getColumnIndex() : columns;

      Check.argument(rows.size() == values.rows(), "illegal rows");
      Check.argument(columns.size() == values.columns(), "illegal columns");
      for (Object row : rows) {
        for (Object column : columns) {
          AbstractDataFrame.this.set(rows, column, values.get(row, column));
        }
      }
    }

    @Override
    public Series getColumn(Object label) {
      return AbstractDataFrame.this.get(label);
    }

    @Override
    public void setColumn(Object label, Series column) {
      AbstractDataFrame.this.set(label, column);
    }

    @Override
    public Series getRow(Object label) {
      int position = getIndex().getLocation(label);
      return getRowElement(position);
    }

    @Override
    public void setRow(Object label, Series row) {
      int position = getIndex().getLocation(label);
      setRowElement(position, row);
    }
  }


  private class LocationIndexerImpl implements LocationIndexer {

    @Override
    public DataFrame get(IntArray rows, IntArray columns) {
      rows = rows instanceof BasicIndex ? Range.of(rows()) : rows;
      columns = columns instanceof BasicIndex ? Range.of(columns()) : columns;
      DataFrame.Builder out = newEmptyBuilder();
      for (int i = 0; i < columns.size(); i++) {
        int columnLocation = columns.get(i);
        Object columnKey = getColumnIndex().get(columnLocation);
        Series oldColumn = get(columnLocation);
        Series.Builder newColumn = oldColumn.newBuilder();
        for (int j = 0; j < rows.size(); j++) {
          int rowLocation = rows.get(j);
          Object rowKey = getIndex().get(rowLocation);
          newColumn.setFrom(rowKey, oldColumn, rowKey);
        }
        out.setColumn(columnKey, newColumn);
      }

      return out.build();
    }

    @Override
    public <T> T get(Class<T> cls, int r, int c) {
      return getElement(cls, r, c);
    }

    @Override
    public double getDouble(int r, int c) {
      return getDoubleElement(r, c);
    }

    @Override
    public int getInt(int r, int c) {
      return getIntElement(r, c);
    }

    @Override
    public void set(int pos, Series column) {
      Check.index(pos, columns());
      setColumnElement(pos, column);
    }

    @Override
    public void setRow(int pos, Series row) {
      setRowElement(pos, row);
    }

    @Override
    public void set(int r, int c, Object value) {
      setElement(r, c, value);
    }

    @Override
    public boolean isNA(int r, int c) {
      return isElementNa(r, c);
    }

    @Override
    public Series get(int c) {
      return ImmutableIndexSeries.newInstance(getColumnElement(c), getIndex());
    }

    @Override
    public DataFrame get(IntArray columns) {
      return getIndexElement(columns);
    }

    @Override
    public DataFrame drop(int index) {
      return dropColumnElement(index);
    }

    @Override
    public DataFrame drop(IntArray columns) {
      return dropColumnIndexElement(columns);
    }

    @Override
    public Series getRow(int r) {
      return getRowElement(r);
    }

    @Override
    public DataFrame getRow(IntArray records) {
      return getRowIndexElement(records);
    }
  }

  private class ColumnList extends AbstractList<Series> {

    @Override
    public Series get(int index) {
      return ImmutableIndexSeries.newInstance(getColumnElement(index), getIndex());
    }

    @Override
    public int size() {
      return AbstractDataFrame.this.columns();
    }
  }

  private class RecordList extends AbstractList<Series> {

    @Override
    public Series get(int index) {
      return getRowElement(index);
    }

    @Override
    public int size() {
      return AbstractDataFrame.this.rows();
    }
  }
}
