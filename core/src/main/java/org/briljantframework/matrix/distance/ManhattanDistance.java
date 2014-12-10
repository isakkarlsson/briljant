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

package org.briljantframework.matrix.distance;

import org.briljantframework.DoubleArray;

/**
 * Manhattan distance, i.e sum of absolute difference
 * <p>
 * Created by Isak Karlsson on 01/09/14.
 */
public class ManhattanDistance implements Distance {

  public static Distance INSTANCE = new ManhattanDistance();

  private ManhattanDistance() {

  }

  @Override
  public double distance(double a, double b) {
    return Math.abs(a - b);
  }

  @Override
  public double distance(DoubleArray a, DoubleArray b) {
    int size = Math.min(a.size(), b.size());
    double distance = 0.0;
    for (int i = 0; i < size; i++) {
      distance += Math.abs(a.get(i) - b.get(i));
    }
    return distance;
  }

  @Override
  public double max() {
    return Double.POSITIVE_INFINITY;
  }

  @Override
  public double min() {
    return 0;
  }

  @Override
  public String toString() {
    return "ManhattanDistance";
  }
}
