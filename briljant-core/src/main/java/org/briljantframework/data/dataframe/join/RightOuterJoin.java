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
package org.briljantframework.data.dataframe.join;

import org.briljantframework.data.dataframe.DataFrame;

/**
 * @author Isak Karlsson
 */
public final class RightOuterJoin implements JoinOperation {

  private static final RightOuterJoin INSTANCE = new RightOuterJoin();

  private RightOuterJoin() {}

  public static RightOuterJoin getInstance() {
    return INSTANCE;
  }

  @Override
  public Joiner createJoiner(JoinKeys keys) {
    return new ReversedJoinerDelegate(LeftOuterJoin.getInstance().createJoiner(keys));
  }


  private static class ReversedJoinerDelegate extends Joiner {

    private final Joiner joiner;

    ReversedJoinerDelegate(Joiner joiner) {
      super(joiner.getColumnKeys());
      this.joiner = joiner;
    }

    @Override
    public DataFrame join(DataFrame a, DataFrame b) {
      return super.join(b, a);
    }

    @Override
    public int getLeftIndex(int i) {
      return joiner.getLeftIndex(i);
    }

    @Override
    public int getRightIndex(int i) {
      return joiner.getRightIndex(i);
    }

    @Override
    public int size() {
      return joiner.size();
    }
  }
}
