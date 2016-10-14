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
package org.briljantframework;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.briljantframework.array.Array;
import org.briljantframework.array.Arrays;
import org.briljantframework.array.DoubleArray;
import org.briljantframework.array.IntArray;
import org.junit.Test;

/**
 * @author Isak Karlsson <isak-kar@dsv.su.se>
 */
public class ArraysTest {

  @Test
  public void ReadIdx() throws Exception {
    // DoubleArray array = Arrays
    // .readIdx(new FileInputStream(new File("/home/isak/Tmp/mnist/train-images-idx3-ubyte")));
    // DoubleArray x = array.select(2);
    // ArrayPrinter.setMinimumTruncateSize(1000000);
    // System.out.println(x);
    //
    //
    // System.out.println(Arrays.unmodifiableArray(x));

    // ArrayPrinter.setVisiblePerSlice(100000);
    // ArrayPrinter.setPrintSlices(1000000);
    // System.out.println(java.util.Arrays.toString(x.getShape()));
    // System.out.println(x);
    // BufferedImage img =
    // new BufferedImage(x.size(0), x.size(1), BufferedImage.TYPE_BYTE_GRAY);
    //
    // for (int i = 0; i < x.size(0); i++) {
    // for (int j = 0; j < x.size(1); j++) {
    // img.setRGB(i, j, (int) x.get(i, j));
    // }
    // }
    // System.out.println(x.asInt());
    // JFrame frame = new JFrame();
    // frame.getContentPane().add(new JLabel(new ImageIcon(img)));
    // frame.pack();
    // frame.setVisible(true);
  }

  @Test
  public void testPlus() throws Exception {
    DoubleArray a = Arrays.doubleVector(1, 2, 3, 4, 5, 6).reshape(2, 3);
    DoubleArray b = Arrays.doubleVector(1, 2, 3);
    System.out.println(a);
    System.out.println(b);
    System.out.println(Arrays.plus(a, b));

    Arrays.plus(b, a, a);
    System.out.println(a);

  }

  @Test
  public void testBroadcast_BiFunction() throws Exception {
    DoubleArray x = DoubleArray.zeros(3, 3, 3);
    DoubleArray y = DoubleArray.linspace(-1, 1, 3 * 3).reshape(3, 3);

    DoubleArray broadcast = Arrays.broadcastCombine(x, y, Arrays::plus);
    System.out.println(broadcast);

  }

  @Test
  public void testSwapdimensions() throws Exception {
    // IntArray x = IntArray.of(0, 4, 2, 6, 1, 5, 3, 7).reshape(2, 2, 2);
    IntArray x = Arrays.broadcastTo(IntArray.of(1, 2), 2, 2, 2);
    System.out.println(x);

    System.out.println(Arrays.swapDimension(x, 0, 2));
  }

  @Test
  public void testBroadcast_reshape() throws Exception {
    Array<String> a = Array.of("a", "b", "c");
    Array<String> x = Arrays.broadcastTo(a, 6, 3).reshape(2, 9);
    assertEquals(Array.of("a", "a", "a", "a", "a", "a", "b", "b", "b", "b", "b", "b", "c", "c", "c",
        "c", "c", "c").reshape(2, 9), x);
  }

  @Test
  public void testBroadcast_column_vector() throws Exception {
    IntArray a = IntArray.of(0, 1, 2).reshape(3, 1);
    IntArray expected = IntArray.of(0, 1, 2, 0, 1, 2, 0, 1, 2).reshape(3, 3);
    assertEquals(expected, Arrays.broadcastTo(a, 3, 3));
  }

  @Test
  public void testBroadcastTo_row_vector() throws Exception {
    IntArray a = IntArray.of(0, 1, 2).reshape(1, 3);
    IntArray expected = IntArray.of(0, 0, 0, 1, 1, 1, 2, 2, 2).reshape(3, 3);
    assertEquals(expected, Arrays.broadcastTo(a, 3, 3));
  }

  @Test
  public void testBroadcastTo_1darray() throws Exception {
    IntArray a = IntArray.of(0, 1, 2);
    IntArray y = Arrays.broadcastTo(a, 3, 3, 3);
    for (int i = 0; i < y.vectors(2); i++) {
      assertEquals(a, y.getVector(2, i));
    }
  }

  @Test
  public void testBroadcast() throws Exception {
    IntArray a = IntArray.of(10032, 3, 3).reshape(3, 1);
    System.out.println(Arrays.broadcastTo(a, 3, 3, 3));
    IntArray x = IntArray.of(0, 1, 2).reshape(1, 3);
    IntArray y = IntArray.of(0, 1, 2).reshape(3, 1);
    System.out.println(Arrays.broadcastAll(asList(x, y)));
  }

  @Test
  public void testVsplit2d() throws Exception {
    int m = 6;
    int n = 3;
    IntArray x = Arrays.range(m * n).reshape(m, n);
    List<IntArray> split = Arrays.vsplit(x, 3);
    assertEquals(Arrays.intVector(0, 1, 6, 7, 12, 13).reshape(2, 3), split.get(0));
    assertEquals(Arrays.intVector(2, 3, 8, 9, 14, 15).reshape(2, 3), split.get(1));
    assertEquals(Arrays.intVector(4, 5, 10, 11, 16, 17).reshape(2, 3), split.get(2));
  }

  @Test
  public void testVsplitnd() throws Exception {
    IntArray x = Arrays.range(6 * 3 * 3).reshape(6, 3, 3);
    List<IntArray> split = Arrays.vsplit(x, 3);
    assertEquals(
        Arrays.intVector(2, 3, 8, 9, 14, 15, 20, 21, 26, 27, 32, 33, 38, 39, 44, 45, 50, 51)
            .reshape(2, 3, 3),
        split.get(1));
  }

  @Test
  public void testHsplit2d() throws Exception {
    int m = 6;
    int n = 3;
    IntArray x = Arrays.range(n * m).reshape(n, m);
    List<IntArray> split = Arrays.hsplit(x, 3);
    assertEquals(Arrays.intVector(0, 1, 2, 3, 4, 5).reshape(3, 2), split.get(0));
    assertEquals(Arrays.intVector(6, 7, 8, 9, 10, 11).reshape(3, 2), split.get(1));
    assertEquals(Arrays.intVector(12, 13, 14, 15, 16, 17).reshape(3, 2), split.get(2));
  }

  @Test
  public void testVstacknd() throws Exception {
    IntArray x = Arrays.range(6 * 3 * 3).reshape(6, 3, 3);
    List<IntArray> split = Arrays.vsplit(x, 3);
    IntArray vstack = Arrays.vstack(split);
    assertEquals(x, vstack);
  }

  @Test
  public void testRepeat() throws Exception {
    IntArray x = Arrays.range(3 * 3).reshape(3, 3);
    assertEquals(Arrays.intVector(0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 7,
        7, 7, 8, 8, 8), Arrays.repeat(x, 3));
  }

  @Test
  public void testRepeatNd() throws Exception {
    IntArray x = Arrays.range(3 * 3 * 3).reshape(3, 3, 3);
    IntArray repeatDim2 = Arrays.repeat(2, x, 3);
    IntArray repeatDim1 = Arrays.repeat(1, x, 3);
    assertEquals(Arrays.intVector(3, 4, 5), repeatDim2.getVector(0, 1));
    assertEquals(Arrays.intVector(1, 1, 1, 4, 4, 4, 7, 7, 7), repeatDim1.getVector(1, 1));
  }

  @Test
  public void testTile3d() throws Exception {
    IntArray x = Arrays.range(2 * 2 * 2).reshape(4, 2);
    IntArray tile = Arrays.tile(x, 2, 2, 2);
    assertArrayEquals(new int[] {2, 8, 4}, tile.getShape());
    assertEquals(Arrays.intVector(0, 4, 0, 4), tile.getVector(2, 0));
    assertEquals(Arrays.intVector(1, 5, 1, 5), tile.getVector(2, 2));
    assertEquals(Arrays.intVector(2, 6, 2, 6), tile.getVector(2, 4));
    assertEquals(Arrays.intVector(3, 7, 3, 7), tile.getVector(2, 6));
    assertEquals(Arrays.intVector(0, 4, 0, 4), tile.getVector(2, 8));
    assertEquals(Arrays.intVector(1, 5, 1, 5), tile.getVector(2, 10));
  }

  @Test
  public void testTile() throws Exception {
    IntArray a = Arrays.intVector(0, 1, 2);
    IntArray a2 = Arrays.intVector(0, 1, 2, 0, 1, 2);
    assertEquals(a2, Arrays.tile(a, 2));

    IntArray a22 = Arrays.tile(a, 2, 2);
    assertEquals(a2, a22.getVector(1, 0));
    assertEquals(a2, a22.getVector(1, 1));

    DoubleArray b = Arrays.doubleMatrix(new double[][] {{1, 2}, {3, 4}});
    DoubleArray bexpected = Arrays
        .doubleMatrix(new double[][] {{1, 2, 1, 2}, {3, 4, 3, 4}, {1, 2, 1, 2}, {3, 4, 3, 4}});

    assertEquals(bexpected, Arrays.tile(b, 2, 2));
  }

  @Test
  public void testHstackedNd() throws Exception {
    IntArray x = Arrays.range(3 * 6 * 3).reshape(3, 6, 3);
    List<IntArray> split = Arrays.hsplit(x, 3);
    System.out.println(split);
    IntArray hstack = Arrays.hstack(split);
    assertEquals(x, hstack);
  }

  @Test
  public void testMeshgrid() throws Exception {
    IntArray x = Arrays.range(3);
    List<IntArray> meshgrid = Arrays.meshgrid(x, x);
    IntArray x1 = meshgrid.get(1);
    IntArray x2 = meshgrid.get(0);
    assertEquals(3, x1.size(0));
    assertEquals(3, x1.size(1));
    assertEquals(3, x2.size(0));
    assertEquals(3, x2.size(1));

    for (int i = 0; i < x2.vectors(0); i++) {
      assertEquals(x, x2.getVector(0, i));
    }

    for (int i = 0; i < x1.vectors(1); i++) {
      assertEquals(x, x1.getVector(1, i));
    }
  }

  @Test
  public void testBisectLeft() throws Exception {
    IntArray a = IntArray.of(1, 2, 9, 10, 12);
    assertEquals(4, Arrays.bisectLeft(a, 12));
  }

  @Test
  public void testSort_DoubleArray() throws Exception {
    DoubleArray x = DoubleArray.of(3, 2, 5, 1, 9, 3);
    assertEquals(DoubleArray.of(1, 2, 3, 3, 5, 9), Arrays.sort(x));
  }

  @Test
  public void testSort_DoubleArray_2d() throws Exception {
    DoubleArray x = DoubleArray.of(3, 2, 1, 9, 8, 10, 12, 3, 1).reshape(3, 3);
    DoubleArray sort = Arrays.sort(0, x);
    assertEquals(DoubleArray.of(1, 2, 3, 8, 9, 10, 1, 3, 12).reshape(3, 3), sort);
  }

  @Test
  public void testSort_IntArray() throws Exception {
    IntArray x = IntArray.of(3, 2, 5, 1, 9, 3);
    assertEquals(IntArray.of(1, 2, 3, 3, 5, 9), Arrays.sort(x));
  }

  @Test
  public void testOrder() throws Exception {
    DoubleArray array = DoubleArray.of(2, 3, 1, 9, 1);
    assertEquals(IntArray.of(2, 4, 0, 1, 3), Arrays.order(array));
  }

  @Test
  public void testOrderDimension() throws Exception {
    DoubleArray array = DoubleArray.of(1, 9, 1, 9, 2, 4).reshape(3, 2);
    assertEquals(IntArray.of(0, 2, 1, 1, 2, 0).reshape(3, 2), Arrays.order(0, array));
  }

  @Test
  public void testConcatenate() throws Exception {
    IntArray x = Arrays.range(2 * 2 * 3).reshape(2, 2, 3);

    IntArray concat_0 = Arrays.concatenate(asList(x, x, x), 0);
    IntArray concat_1 = Arrays.concatenate(asList(x, x, x), 1);
    IntArray concat_2 = Arrays.concatenate(asList(x, x, x), 2);

    IntArray expected_0 = IntArray.of(0, 1, 0, 1, 0, 1, 2, 3, 2, 3, 2, 3, 4, 5, 4, 5, 4, 5, 6, 7, 6,
        7, 6, 7, 8, 9, 8, 9, 8, 9, 10, 11, 10, 11, 10, 11);
    IntArray expected_1 = IntArray.of(0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 4, 5, 6, 7, 4, 5, 6, 7, 4,
        5, 6, 7, 8, 9, 10, 11, 8, 9, 10, 11, 8, 9, 10, 11);
    IntArray expected_2 = IntArray.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0, 1, 2, 3, 4, 5, 6, 7,
        8, 9, 10, 11, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);

    assertEquals(expected_0.reshape(6, 2, 3), concat_0);
    assertEquals(expected_1.reshape(2, 6, 3), concat_1);
    assertEquals(expected_2.reshape(2, 2, 9), concat_2);
  }

  @Test
  public void testConcat_1d() throws Exception {
    IntArray x = Arrays.range(10);
    IntArray y = Arrays.range(10);
    System.out.println(Arrays.concatenate(asList(x, y), 0));
  }

  @Test
  public void testVstack_1d() throws Exception {
    IntArray x = Arrays.range(10);
    IntArray y = Arrays.range(10);
    System.out.println(Arrays.vstack(asList(x, y)));
    System.out.println(Arrays.hstack(asList(x, y)));
    System.out.println(Arrays.concatenate(asList(x, y), 0));
  }

  @Test
  public void testSplit_1d() throws Exception {
    System.out.println(Arrays.split(Arrays.range(10), 10));
  }

  @Test
  public void testSplit() throws Exception {
    IntArray x = Arrays.range(2 * 2 * 3).reshape(2, 2, 3);
    assertEquals(x, Arrays.concatenate(Arrays.split(x, 2, 0), 0));
    assertEquals(x, Arrays.concatenate(Arrays.split(x, 2, 1), 1));
    assertEquals(x, Arrays.concatenate(Arrays.split(x, 3, 2), 2));
  }
}
