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

package org.briljantframework.io;

import org.apache.commons.math3.complex.Complex;
import org.briljantframework.io.resolver.Resolver;
import org.briljantframework.io.resolver.Resolvers;
import org.briljantframework.vector.Bit;
import org.briljantframework.vector.DoubleVector;
import org.briljantframework.vector.IntVector;
import org.briljantframework.vector.Is;
import org.briljantframework.vector.Na;

/**
 * A string data entry holds string values and tries to convert them to appropriate types. Such
 * failures won't propagate, instead the respective NA value will be returned.
 */
public class StringDataEntry implements DataEntry {

  public static final String MISSING_VALUE = "?";
  private final String[] values;
  private final String missingValue;
  private int current = 0;

  public StringDataEntry(String... values) {
    this(values, MISSING_VALUE);
  }

  public StringDataEntry(String[] values, String missingValue) {
    this.values = values;
    this.missingValue = missingValue;
  }

  @Override
  public <T> T next(Class<T> cls) {
    String value = nextString();
    if (Is.NA(value)) {
      return Na.of(cls);
    } else {
      Resolver<T> resolver = Resolvers.find(cls);
      if (resolver == null) {
        return Na.of(cls);
      } else {
        return resolver.resolve(value);
      }
    }
  }

  @Override
  public String nextString() {
    String value = values[current++];
    if (value == null) {
      return null;
    } else {
      value = value.trim();
      return value.equals(missingValue) ? null : value;
    }
  }

  @Override
  public int nextInt() {
    String repr = nextString();
    if (repr == null) {
      return IntVector.NA;
    }
    Integer value;
    try {
      value = Integer.parseInt(repr);
    } catch (NumberFormatException e) {
      value = null;
    }
    return value == null ? IntVector.NA : value;
  }

  @Override
  public double nextDouble() {
    String repr = nextString();
    if (repr == null) {
      return DoubleVector.NA;
    } else {
      Double value;
      try {
        value = Double.parseDouble(repr);
      } catch (NumberFormatException e) {
        value = null;
      }
      return value == null ? DoubleVector.NA : value;
    }
  }

  @Override
  public Bit nextBinary() {
    return Bit.valueOf(nextInt());
  }

  @Override
  public Complex nextComplex() {
    return next(Complex.class);
  }

  @Override
  public boolean hasNext() {
    return current < size();
  }

  @Override
  public int size() {
    return values.length;
  }
}