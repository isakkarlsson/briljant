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
package org.briljantframework.data.reader;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.briljantframework.data.series.Convert;
import org.briljantframework.data.series.Type;

/**
 * Converts a {@link ResultSet} to an entry reader.
 * 
 * @author Isak Karlsson
 */
public class SqlEntryReader implements EntryReader {

  private static final int READ = -1;
  private static final int LEAVE = 0;
  private static final int EMPTY = 1;
  private final ResultSet resultSet;
  private final Type[] types;
  private int state = READ;

  public SqlEntryReader(ResultSet resultSet) throws SQLException {
    this.resultSet = resultSet;
    ResultSetMetaData metaData = resultSet.getMetaData();
    this.types = new Type[metaData.getColumnCount()];
    for (int i = 1; i <= types.length; i++) {
      types[i - 1] = toType(metaData.getColumnType(i));
    }
  }

  /**
   * Convert the result set column type to a suitable Java class.
   * 
   * @param type the type
   * @return a Java class
   */
  private static Type toType(int type) {
    Class<?> result = Object.class;
    switch (type) {
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
        result = String.class;
        break;

      case Types.NUMERIC:
      case Types.DECIMAL:
        result = java.math.BigDecimal.class;
        break;

      case Types.BIT:
        result = Boolean.class;
        break;

      case Types.TINYINT:
      case Types.SMALLINT:
      case Types.INTEGER:
        result = Integer.class;
        break;

      case Types.BIGINT:
        result = Long.class;
        break;

      case Types.REAL:
      case Types.FLOAT:
      case Types.DOUBLE:
        result = Double.class;
        break;

      case Types.BINARY:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
        result = Byte[].class;
        break;

      case Types.DATE:
        result = java.sql.Date.class;
        break;

      case Types.TIME:
        result = java.sql.Time.class;
        break;

      case Types.TIMESTAMP:
        result = java.sql.Timestamp.class;
        break;
    }

    return org.briljantframework.data.series.Types.getType(result);
  }

  @Override
  public List<Type> getTypes() {
    return Collections.unmodifiableList(Arrays.asList(types));
  }

  @Override
  public DataEntry next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }

    try {
      Object[] record = new Object[types.length];
      for (int i = 1; i <= types.length; i++) {
        record[i - 1] = resultSet.getObject(i);
      }
      state = READ;
      return new SqlEntry(types, record);
    } catch (SQLException e) {
      throw new EntryReaderException(e);
    }
  }

  @Override
  public boolean hasNext() {
    try {
      if (state == READ) {
        if (resultSet.next()) {
          state = LEAVE;
        } else {
          state = EMPTY;
        }
      }

      return state != EMPTY;
    } catch (SQLException e) {
      throw new EntryReaderException(e);
    }
  }

  private static class SqlEntry implements DataEntry {

    private final Type[] types;
    private final Object[] values;
    private int current = 0;

    private SqlEntry(Type[] types, Object[] values) {
      this.types = types;
      this.values = values;
    }

    @Override
    public String toString() {
      return Arrays.toString(values);
    }

    @Override
    public <T> T next(Class<T> cls) {
      T value;
      if (types[current].getDataClass().isAssignableFrom(cls)) {
        value = cls.cast(values[current]);
      } else {
        value = Convert.to(cls, values[current]);
      }
      current++;
      return value;
    }

    @Override
    public String nextString() {
      return next(String.class);
    }

    @Override
    public int nextInt() {
      return next(Integer.class);
    }

    @Override
    public double nextDouble() {
      return next(Double.class);
    }

    @Override
    public boolean hasNext() {
      return current < size();
    }

    @Override
    public void skip(int no) {
      if (no > current && no < size()) {
        current = no;
      }
    }

    @Override
    public int size() {
      return types.length;
    }


  }
}
