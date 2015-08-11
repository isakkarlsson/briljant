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

package org.briljantframework.classification.linear;

import org.briljantframework.Utils;
import org.briljantframework.classification.Classifier;
import org.briljantframework.classification.LogisticRegression;
import org.briljantframework.dataframe.DataFrame;
import org.briljantframework.dataframe.DataFrames;
import org.briljantframework.dataframe.Datasets;
import org.briljantframework.dataframe.MixedDataFrame;
import org.briljantframework.evaluation.Validators;
import org.briljantframework.evaluation.result.Result;
import org.briljantframework.evaluation.result.Sample;
import org.briljantframework.vector.Is;
import org.briljantframework.vector.Vector;
import org.junit.Test;

public class LogisticRegressionTest {

  @Test
  public void testLogisticRegression() throws Exception {
    Utils.setRandomSeed(102);
    DataFrame iris = DataFrames.permuteRows(Datasets.loadIris());
    DataFrame x = iris.drop("Class").apply(Double.class, v -> !Is.NA(v) ? v : 0);
    Vector y = iris.get("Class").satisfies(String.class, v -> v.equals("Iris-setosa"));
    Classifier reg = LogisticRegression.withIterations(500)
        .withRegularization(100000)
        .build();
    LogisticRegression.Predictor model = (LogisticRegression.Predictor) reg.fit(x, y);

    System.out.println(model.getOddsRatio("(Intercept)"));
    for (Object o : x.getColumnIndex()) {
      System.out.println(model.getOddsRatio(o));
    }

//    reg = RandomForest.withSize(100).withMaximumFeatures(1).build();
    System.out.println(reg);
    long start = System.nanoTime();
    Result result = Validators.crossValidation(10).test(reg, x, y);
    System.out.println((System.nanoTime() - start) / 1e6);
//    System.out.println(result.toDataFrame());
    System.out.println(result.toDataFrame().groupBy("Sample").get(Sample.OUT));

  }

  @Test
  public void testOdds() throws Exception {
    DataFrame x = MixedDataFrame.of(
        "Age", Vector.of(55, 28, 65, 46, 86, 56, 85, 33, 21, 42),
        "Smoker", Vector.of(0, 0, 1, 0, 1, 1, 0, 0, 1, 1)
    );
    Vector y = Vector.of(0, 0, 0, 1, 1, 1, 0, 0, 0, 1);
    System.out.println(x);

    LogisticRegression regression = LogisticRegression.create();
    LogisticRegression.Predictor model = regression.fit(x, y);
    System.out.println(model);

    System.out.println("(Intercept) " + model.getOddsRatio("(Intercept)"));
    for (Object o : x.getColumnIndex()) {
      System.out.println(o + " " + model.getOddsRatio(o));
    }
  }
}