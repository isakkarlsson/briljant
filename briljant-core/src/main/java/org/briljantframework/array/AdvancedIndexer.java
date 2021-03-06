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
package org.briljantframework.array;

import java.util.ArrayList;
import java.util.List;

import org.briljantframework.Check;
import org.briljantframework.util.primitive.IntList;

/**
 * An advanced indexer holds {@code n} int arrays of the same {@link #getShape() shape} used for
 * indexing index an array with {@code n} dimensions.
 *
 * // TODO: 4/5/16 indicate the rules for advanced indexing 
 *
 * @author Isak Karlsson <isak-kar@dsv.su.se>
 * @see BasicIndex
 */
public final class AdvancedIndexer {

  private final IntArray[] index;
  private final int[] shape;

  /**
   * Construct an advanced indexer from an array of the specified shape given the specified indexing
   * arrays
   * 
   * @param shape the shape
   * @param arrays the indexing array
   */
  public AdvancedIndexer(int[] shape, List<? extends IntArray> arrays) {
    if (arrays.stream().allMatch(AdvancedIndexer::isBasicIndexer)) {
      this.index = null;
      this.shape = null;
    } else {
      List<IntArray> advancedIndexes = new ArrayList<>();
      boolean hasBasicIndexGap = false;
      int ndims = shape.length;
      IntArray[] indexers = new IntArray[ndims];
      int firstAdvancedIndex = -1;
      for (int i = 0; i < ndims; i++) {
        if (i < arrays.size()) {
          IntArray index = arrays.get(i);
          Check.argument(index != null, "indexer is required.");
          if (!isBasicIndexer(index) && firstAdvancedIndex > 0 && i > 0) {
            hasBasicIndexGap = true;
          }

          if (!isBasicIndexer(index)) {
            advancedIndexes.add(index);
            if (firstAdvancedIndex == -1) {
              firstAdvancedIndex = i;
            }
          }
          indexers[i] = index == BasicIndex.ALL ? Arrays.range(shape[i]) : index;
        } else {
          // include everything from additional dimensions not covered by the index
          indexers[i] = Arrays.range(shape[i]);
        }
      }

      // Broadcast all advanced indicies to the same shape
      advancedIndexes = Arrays.broadcastAll(advancedIndexes);
      int[] broadcastShape = advancedIndexes.get(0).getShape();
      IntList dims = new IntList();
      if (hasBasicIndexGap) {
        // if we cannot correctly place the sub-space defined by the advanced
        // indexer, we place it first.
        dims.addAll(broadcastShape);
        for (IntArray index : indexers) {
          if (isBasicIndexer(index)) {
            dims.add(index.size());
          }
        }
      } else {
        // if we can place the index correctly, we place it at the position of
        // the first advanced index
        for (int i = 0; i < ndims; i++) {
          // place the advanced index at the appropriate position
          if (firstAdvancedIndex == i) {
            dims.addAll(broadcastShape);
          } else {
            IntArray index = indexers[i];
            if (isBasicIndexer(index)) {
              dims.add(index.size());
            }
          }
        }
      }

      int[] newShape = dims.toPrimitiveArray();

      // index arrays broadcast to the shape of the
      IntArray[] indexArrays = new IntArray[ndims];

      if (hasBasicIndexGap) {
        // if we have a gap, insert the advanced indexer shape first and then the
        // basic indexers
        int[] compatibleShape = new int[newShape.length];
        int shapeLocation = broadcastShape.length;
        for (int i = 0; i < ndims; i++) {
          java.util.Arrays.fill(compatibleShape, 1);
          IntArray index = indexers[i];
          if (isBasicIndexer(index)) {
            compatibleShape[shapeLocation] = index.size();
            shapeLocation++;
          } else {
            System.arraycopy(broadcastShape, 0, compatibleShape, 0, broadcastShape.length);
          }
          indexArrays[i] = broadcastCompatible(index, compatibleShape, newShape);
        }
      } else {
        int[] compatibleShape = new int[newShape.length];
        int noSeenAdvanced = 0;
        for (int i = 0; i < ndims; i++) {
          java.util.Arrays.fill(compatibleShape, 1);
          IntArray index = indexers[i];
          if (isBasicIndexer(index)) {
            if (i < firstAdvancedIndex) {
              compatibleShape[i] = index.size();
            } else {
              compatibleShape[i + broadcastShape.length - noSeenAdvanced] = index.size();
            }
          } else {
            for (int k = 0; k < index.dims(); k++) {
              compatibleShape[firstAdvancedIndex + k] = index.size(k);
            }
            noSeenAdvanced++;
          }
          indexArrays[i] = broadcastCompatible(index, compatibleShape, newShape);
        }
      }

      this.index = indexArrays;
      this.shape = newShape;
    }
  }

  private IntArray broadcastCompatible(IntArray i, int[] compatibleShape, int[] newShape) {
    return Arrays.broadcastTo(i.reshape(compatibleShape), newShape);
  }

  /**
   * Returns true if the given array is a basic indexer
   *
   * @param indexer the indexer
   * @return true if basic; false otherwise
   */
  static boolean isBasicIndexer(IntArray indexer) {
    return (indexer instanceof Range && indexer.dims() == 1) || indexer == BasicIndex.ALL;
  }

  /**
   * Returns true if if a basic (view) index should be used instead.
   * 
   * @return true if a view can be returned.
   */
  boolean isBasicIndexer() {
    return index == null;
  }


  /**
   * Returns the indexer for the i:th dimension
   * 
   * @param i the dimension
   * @return the dimensions
   */
  IntArray getIndex(int i) {
    if (index == null) {
      throw illegalStateBasicIndexer();
    }
    return index[i];
  }

  /**
   * Get the number of indexers
   * 
   * @return the indexers
   */
  int size() {
    if (index == null) {
      throw illegalStateBasicIndexer();
    }
    return index.length;
  }

  /**
   * Get the shape of the array resulting from this indexer
   * 
   * @return the shape of the array resulting from this indexer
   */
  int[] getShape() {
    if (shape == null) {
      throw illegalStateBasicIndexer();
    }
    return shape.clone();
  }

  private IllegalStateException illegalStateBasicIndexer() {
    return new IllegalStateException("basic indexer");
  }
}
