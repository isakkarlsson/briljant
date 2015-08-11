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

package org.briljantframework.classification.tune;

import org.briljantframework.Check;
import org.briljantframework.classification.Classifier;
import org.briljantframework.dataframe.DataFrame;
import org.briljantframework.evaluation.Validators;
import org.briljantframework.vector.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by isak on 01/10/14.
 */
public class Tuners {

  @SafeVarargs
  public static <C extends Classifier, O extends Classifier.Builder<? extends C>> Configurations crossValidation(
      O builder, DataFrame x, Vector y, Comparator<Configuration> comparator, int folds,
      Updater<O>... updaters) {
    Check.argument(updaters.length > 0, "Can't tune without updaters");
    Check.argument(folds > 1 && folds < x.rows(), "Invalid number of cross-validation folds");
    ArrayList<Updater<O>> updaterList = new ArrayList<>(updaters.length);
    Collections.addAll(updaterList, updaters);
    return new DefaultTuner<>(updaterList, Validators.crossValidation(folds),
                              comparator).tune(builder, x, y);
  }
}