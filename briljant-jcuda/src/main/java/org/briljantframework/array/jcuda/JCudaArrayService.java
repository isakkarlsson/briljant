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
package org.briljantframework.array.jcuda;

import org.briljantframework.array.api.ArrayBackend;
import org.briljantframework.array.api.ArrayService;

import jcuda.jcublas.JCublas;

/**
 * Created by isak on 9/6/16.
 */
public class JCudaArrayService implements ArrayService {
  private static boolean isAvailable = true;

  static {
    try {
      JCublas.cublasInit();
    } catch (Exception ignore) {
      System.err.println("JCudaArrayBackend is unavailable.");
      ignore.printStackTrace();
      isAvailable = false;
    }
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        JCublas.cublasShutdown();
      }
    });
  }

  @Override
  public boolean isAvailable() {
    return isAvailable;
  }

  @Override
  public int getPriority() {
    return 200;
  }

  @Override
  public ArrayBackend getArrayBackend() {
    return JCudaArrayBackend.getInstance();
  }
}
