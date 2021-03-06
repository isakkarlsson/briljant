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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.briljantframework.util.sort.QuickSort;
import org.briljantframework.util.sort.ElementSwapper;
import org.junit.Test;

public class QuickSortTest {


  @Test
  public void testTest() throws Exception {
    ArrayList<String> list = new ArrayList<>();
    list.addAll(Arrays.asList("a", "b", "c", "q", "f", "d", "r", "a"));
    ElementSwapper s = new ListElementSwapper(list);
    QuickSort.quickSort(0, list.size(), (a, b) -> list.get(a).compareTo(list.get(b)), s);

    assertEquals("a", list.get(0));
    assertEquals("a", list.get(1));
  }

  private static class ListElementSwapper implements ElementSwapper {

    private final List<?> list;

    private ListElementSwapper(List<?> list) {
      this.list = list;
    }

    @Override
    public void swap(int a, int b) {
      Collections.swap(list, a, b);
    }
  }
}
