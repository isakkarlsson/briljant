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

import org.briljantframework.array.DoubleArray;
import org.briljantframework.classification.AbstractPredictor;
import org.briljantframework.data.vector.Vector;

/**
 * Represents a Tree based predictor. Uses a
 * {@link org.briljantframework.classification.tree.TreeVisitor} to make predictions.
 * 
 * @author Isak Karlsson
 */
public class TreePredictor<T> extends AbstractPredictor {

  private final TreeVisitor<T> predictionVisitor;

  private final TreeNode<T> node;

  /**
   * Instantiates a new Predictor.
   *
   * @param node the node
   * @param predictionVisitor the prediction visitor
   */
  protected TreePredictor(Vector classes, TreeNode<T> node, TreeVisitor<T> predictionVisitor) {
    super(classes);
    this.node = node;
    this.predictionVisitor = predictionVisitor;
  }

  /**
   * Gets node.
   *
   * @return the node
   */
  public TreeNode<T> getTree() {
    return node;
  }

  @Override
  public DoubleArray estimate(Vector record) {
    DoubleArray probabilities = predictionVisitor.visit(node, record);
//    int max = Bj.argmax(probabilities);
//    for (int i = 0; i < probabilities.size(); i++) {
//      probabilities.set(i, max == i ? 1 : 0);
//    }
    return probabilities;
  }
}
