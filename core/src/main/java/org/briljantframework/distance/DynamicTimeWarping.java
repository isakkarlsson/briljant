/*
 * ADEB - machine learning pipelines made easy Copyright (C) 2014 Isak Karlsson
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */

package org.briljantframework.distance;

import org.briljantframework.matrix.DoubleMatrix;
import org.briljantframework.matrix.Matrices;
import org.briljantframework.vector.Vector;

import com.google.common.primitives.Doubles;

/**
 * In time series analysis, dynamic time warping (DTW) is an algorithm for measuring similarity
 * between two temporal sequences which may vary in time or speed.
 * <p>
 * In general, DTW is a method that calculates an optimal match between two given sequences (e.g.
 * time series) with certain restrictions. The sequences are "warped" non-linearly in the time
 * dimension to determine a measure of their similarity independent of certain non-linear variations
 * in the time dimension. This sequence alignment method is often used in time series
 * classification. Although DTW measures a distance-like quantity between two given sequences, it
 * doesn't guarantee the triangle inequality to hold.
 * <p>
 * Created by Isak Karlsson on 01/09/14.
 */
public class DynamicTimeWarping implements Distance {

  /**
   * The Distance.
   */
  protected final Distance distance;
  private final int constraint;

  /**
   * Instantiates a new Dynamic time warping.
   *
   * @param distance the local distance function
   * @param constraint the local constraint (i.e. width of the band)
   */
  public DynamicTimeWarping(Distance distance, int constraint) {
    this.distance = distance;
    this.constraint = constraint;
  }


  /**
   * Delegated to the injected distance function
   *
   * @param a scalar
   * @param b scalar
   * @return the distance between a and b
   */
  @Override
  public final double compute(double a, double b) {
    return distance.compute(a, b);
  }

  @Override
  public double compute(Vector a, Vector b) {
    int n = a.size(), m = b.size();
    DoubleMatrix dwt = Matrices.newDoubleMatrix(n, m).assign(Double.POSITIVE_INFINITY);
    dwt.set(0, 0, 0);

    int width = Math.max(constraint, Math.abs(n - m));
    for (int i = 1; i < n; i++) {
      int end = constraint <= -1 ? m : Math.min(m, i + width);
      int start = constraint <= -1 ? 1 : Math.max(1, i - width);
      for (int j = start; j < end; j++) {
        double cost = distance.compute(a.getAsDouble(i), b.getAsDouble(j));
        dwt.set(i, j,
            cost + Doubles.min(dwt.get(i - 1, j), dwt.get(i, j - 1), dwt.get(i - 1, j - 1)));
      }
    }

    return dwt.get(n - 1, m - 1);
  }

  @Override
  public double max() {
    return distance.max();
  }

  @Override
  public double min() {
    return distance.min();
  }

  @Override
  public String toString() {
    return String.format("Dynamic time warping (w=%s)", constraint);
  }
}
