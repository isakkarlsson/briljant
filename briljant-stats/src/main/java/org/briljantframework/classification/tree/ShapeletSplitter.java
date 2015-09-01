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

package org.briljantframework.classification.tree;

import org.briljantframework.data.dataframe.DataFrame;
import org.briljantframework.distance.Distance;
import org.briljantframework.data.vector.Vector;

/**
 * @author Isak Karlsson
 */
public abstract class ShapeletSplitter {

  protected final Gain gain;
  private final Distance metric;

  protected ShapeletSplitter(Distance metric, Gain gain) {
    this.metric = metric;
    this.gain = gain;
  }

  public final Distance getDistanceMetric() {
    return metric;
  }

  public final Gain getGain() {
    return gain;
  }

  public abstract TreeSplit<ShapeletThreshold> find(ClassSet classSet, DataFrame x, Vector y);
}
