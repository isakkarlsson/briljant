package org.briljantframework.dataframe;

import junit.framework.TestCase;

import org.briljantframework.dataframe.join.JoinType;
import org.briljantframework.vector.IntVector;
import org.briljantframework.vector.Is;
import org.briljantframework.vector.Na;
import org.briljantframework.vector.StringVector;
import org.briljantframework.vector.Vec;
import org.briljantframework.vector.Vector;

public class InnerJoinTest extends TestCase {

  public void testGuesstimatePerformance() throws Exception {
//    DataFrame cats =
//        MixedDataFrame.of("User", new StringVector("a", "a", "c", "d"), "Number of Cats",
//                          new IntVector(1, 2, 3, 4));
//
//    DataFrame dogs =
//        MixedDataFrame.of("User", new StringVector("b", "a", "a", "a"), "Number of dogs",
//                          new IntVector(1, 2, 2, 3), "Poop", new IntVector(1, 2, 3, 4));
//
//    System.out.println(cats);
//    System.out.println(dogs);
//    System.out.println(DataFrames.outerJoin(cats, dogs, Arrays.asList("User")));
//
    DataFrame connect4 = Datasets.loadConnect4();
    connect4 = connect4.insert(0, "index", IntVector.range(connect4.rows()));
//    connect4.setColumnName(0, "index");
//    System.out.println(connect4);
    DataFrame f = null;
    for (int i = 0; i < 10; i++) {
      f = connect4.join(connect4, 0);
    }

    long s = System.nanoTime();
    boolean bo = false;
    for (int j = 0; j < f.columns(); j++) {
      Vector column = f.get(j);
      for (int i = 0; i < f.rows(); i++) {
        bo = Is.NA(column.get(Object.class, i));
      }
    }
    System.out.println((System.nanoTime() - s) / 1e6);

    s = System.nanoTime();

    Record mode = f.reduce(Vec::mode);
    System.out.println(mode.get(Object.class, "21"));
//    for (int j = 0; j < f.columns(); j++) {
//      Vector column = f.get(j);
//      for (int i = 0; i < f.rows(); i++) {
//        bo = column.isNA(i);
//      }
//    }
    System.out.println((System.nanoTime() - s) / 1e6);


//    System.out.println(DataFrames.permuteRows(f));
    // System.out.println(DataFrames.leftOuterJoin(dogs, cats, Arrays.asList(0)));

  }

  public void testSimpleMerge() throws Exception {
    DataFrame left = MixedDataFrame.of("key", new StringVector("foo", "foo", "ko"),
                                       "lval", new IntVector(1, 2, 4));
    DataFrame right = MixedDataFrame.of("key", new StringVector("foo", "bar"),
                                        "rval", new IntVector(3, 5));

    System.out.println(left);
    System.out.println(right);
//    DataFrame j = DataFrames.innerJoin(left, right);
//    System.out.println(j);
    System.out.println(
        left.join(JoinType.OUTER, right)
            .setRecordIndex(HashIndex.from("q", "A", "b", "D"))
            .sort(SortOrder.DESC)
    );

  }

  public void testComplexMerge() throws Exception {
    DataFrame left = MixedDataFrame.of("key1", new StringVector("foo", "foo", "bar"),
                                       "key2", new StringVector("one", "two", "one"),
                                       "lval", new IntVector(1, 2, 3));
    DataFrame right = MixedDataFrame.of("key1", new StringVector("foo", "foo", "bar", "bar"),
                                        "key2", new StringVector("one", "one", "one", "two"),
                                        "rval", new IntVector(4, 5, 6, 7));

    System.out.println(left.reduce(Vec::mode));
    System.out.println(left.join(JoinType.INNER, right));
  }
}
