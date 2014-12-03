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

package org.briljantframework.classification;

import java.util.ArrayList;
import java.util.List;

import org.briljantframework.dataframe.DataFrame;
import org.briljantframework.vector.Vector;

/**
 * <p>
 * A classifier is a function {@code f(X, y)} which produces a hypothesis {@code g = X -> y} that as
 * accurately as possible model the true function {@code h} used to generate {@code X -> y}.
 * </p>
 * 
 * <p>
 * The input {@code x} is usually denoted as the instances and the output {@code y} as the classes.
 * The input instances is represented as a {@link org.briljantframework.dataframe.DataFrame} which
 * consists of possibly heterogeneous vectors of values characterizing each instance.
 * </p>
 * 
 * <p>
 * The output of the classifier is a {@link org.briljantframework.classification.Classifier.Model}
 * (i.e., the {@code g}) which (hopefully) approximates {@code h}. To estimate how well {@code g}
 * approximates {@code h}, cross-validation
 * {@link org.briljantframework.evaluation.Evaluators#crossValidation(int)} can be employed.
 * </p>
 * 
 * A classifier is always atomic, i.e. does not have mutable state.
 * 
 * <p>
 * Implementors are encouraged to also provide a convenient builder for the classifier, for example
 * in cases where the number of parameters are large. For example,
 * {@code RandomForest forest = RandomForest.withSize(100).withMaximumFeatures(2).build();} is more
 * clear than {@code RandomForest forest = new RandomForest(100, 2);}. If the builder implements
 * {@link org.briljantframework.classification.Classifier.Builder}, the
 * {@link org.briljantframework.classification.tune.Tuner} can be used to optimize parameter
 * configurations.
 * 
 * <pre>
 * Tuners.crossValidation(new RandomForest.Builder(), x, y,
 *     Configuration.metricComparator(Accuracy.class), 10,
 *     range(&quot;No. trees&quot;, RandomForest.Builder::withSize, 10, 1000, 10),
 *     range(&quot;No. features&quot;, RandomForest.Builder::withMaximumFeatures, 2, x.columns(), 1));
 * </pre>
 * 
 * </p>
 * 
 * @author Isak Karlsson
 */
public interface Classifier {

  /**
   * Fit a hypothesis using the instances in {@code x} to the output classes in {@code y}
   * 
   * @param x the instances
   * @param y the classes
   * @return a classification model
   */
  public abstract Model fit(DataFrame x, Vector y);

  /**
   * The interface Builder.
   */

  public static interface Builder<C extends Classifier> {
    /**
     * Create classifier.
     *
     * @return the classifier
     */
    C build();
  }

  /**
   * The interface Model.
   * <p>
   * TODO(isak) - below:
   * <p>
   * In some cases models produce additional measurements. For example, a random forest produces
   * variable importance and a linear models produce standard error, t-statistics R^2 etc. One
   * question is how to incorporate these measurements into the different models. Perhaps they may
   * feeling is that they don't belong here, but rather to the particular implementation. However,
   * it might be useful to have for example a summary() function or similar. Perhaps even a
   * plot(onto) function.
   */
  public static interface Model {

    /**
     * Determine the class label of every instance in {@code x}
     *
     * @param x to determine class labels for
     * @return the predictions
     */
    default Predictions predict(DataFrame x) {
      List<Prediction> predictions = new ArrayList<>();
      for (Vector e : x) {
        predictions.add(predict(e));
      }
      return Predictions.create(predictions);
    }

    /**
     * Predict the class label of a specific {@link org.briljantframework.vector.Vector}
     *
     * @param row to which the class label shall be assigned
     * @return the prediction
     */
    Prediction predict(Vector row);
  }
}
