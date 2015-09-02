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

package org.briljantframework.data.vector;

import org.apache.commons.math3.complex.Complex;
import org.briljantframework.Check;

/**
 * @author Isak Karlsson
 */
class SingletonVector extends AbstractVector {

  private static final Vector EMPTY = new SingletonVector(null, 0);

  private final Class<?> cls;
  private final Object value;
  private final VectorType type;
  private final int size;

  SingletonVector(Object value, int size) {
    this.cls = value != null ? value.getClass() : Object.class;
    this.value = value;
    this.size = size;
    type = VectorType.from(cls);
  }

  public static final Vector empty() {
    return EMPTY;
  }

  @Override
  public <T> T getAt(Class<T> cls, int index) {
    Check.elementIndex(index, size());
    Object obj = value;
    if (Is.NA(obj)) {
      return Na.from(cls);
    }
    if (!cls.isInstance(obj)) {
      if (cls.equals(String.class)) {
        return cls.cast(obj.toString());
      } else {
        if (this.cls.equals(Number.class)) {
          Number num = Number.class.cast(obj);
          if (cls.equals(Double.class)) {
            return cls.cast(num.doubleValue());
          } else if (cls.equals(Integer.class)) {
            return cls.cast(num.intValue());
          }
        }
      }
      return Na.from(cls);
    }
    return cls.cast(obj);
  }

  @Override
  public String toStringAt(int index) {
    Check.elementIndex(index, size());
    return value != null ? value.toString() : "NA";
  }

  @Override
  public boolean isNaAt(int index) {
    Check.elementIndex(index, size());
    return Is.NA(value);
  }

  @Override
  public boolean hasNA() {
    return Is.NA(value);
  }

  @Override
  public double getAsDoubleAt(int i) {
    Check.elementIndex(i, size());
    return value instanceof Number ? ((Number) value).doubleValue() : Na.DOUBLE;
  }

  @Override
  public int getAsIntAt(int i) {
    Check.elementIndex(i, size());
    return value instanceof Number ? ((Number) value).intValue() : Na.INT;
  }

  public Logical getAsBit(int index) {
    Check.elementIndex(index, size());
    return value instanceof Number ? Logical.valueOf(((Number) value).intValue())
                                   : value instanceof Logical ? (Logical) value :
                                     value instanceof Boolean ? Logical.valueOf((boolean) value) :
                                     Logical.NA;
  }

  public Complex getAsComplex(int index) {
    Check.elementIndex(index, size());
    return value instanceof Complex ? (Complex) value :
           value instanceof Number ? Complex.valueOf(((Number) value).doubleValue())
                                   : Na.from(Complex.class);
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public VectorType getType() {
    return type;
  }

  @Override
  public int compareAt(int a, Vector other, int b) {
    return getType().compare(a, this, b, other);
  }
}