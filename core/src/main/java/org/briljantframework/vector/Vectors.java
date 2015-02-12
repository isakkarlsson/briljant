package org.briljantframework.vector;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.*;
import java.util.function.Predicate;

import org.briljantframework.IndexComparator;
import org.briljantframework.QuickSort;

import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.primitives.Ints;

/**
 * @author Isak Karlsson
 */
public final class Vectors {

  public static final Set<VectorType> NUMERIC = Sets.newIdentityHashSet();
  public static final Set<VectorType> CATEGORIC = Sets.newIdentityHashSet();
  static {
    NUMERIC.add(DoubleVector.TYPE);
    NUMERIC.add(IntVector.TYPE);
    NUMERIC.add(ComplexVector.TYPE);

    CATEGORIC.add(StringVector.TYPE);
    CATEGORIC.add(BitVector.TYPE);
  }

  private Vectors() {}

  public static int find(Vector vector, Value value) {
    for (int i = 0; i < vector.size(); i++) {
      if (vector.compare(i, value) == 0) {
        return i;
      }
    }
    return -1;
  }

  public static int find(Vector vector, int value) {
    return find(vector, Convert.toValue(value));
  }

  public static int find(Vector vector, String value) {
    return find(vector, Convert.toValue(value));
  }

  public static int find(Vector vector, Predicate<Value> predicate) {
    for (int i = 0; i < vector.size(); i++) {
      if (predicate.test(vector.getAsValue(i))) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Creates a double vector of {@code size} filled with {@code NA}
   * 
   * @param size the size
   * @return a new vector
   */
  public static DoubleVector newDoubleNA(int size) {
    return new DoubleVector.Builder(size).build();
  }

  public static IntVector newIntVector(int... values) {
    return IntVector.newBuilderWithInitialValues(values).build();
  }

  /**
   * @param in the vector
   * @return a new vector sorted in ascending order
   */
  public static Vector sortAsc(Vector in) {
    Vector.Builder builder = in.newCopyBuilder();
    QuickSort.quickSort(0, in.size(), builder::compare, builder);
    return builder.build();
  }

  /**
   * @param in the vector
   * @return a new vector sorted in ascending order
   */
  public static Vector sortDesc(Vector in) {
    Vector.Builder builder = in.newCopyBuilder();
    QuickSort.quickSort(0, in.size(), (a, b) -> builder.compare(b, a), builder);
    return builder.build();
  }

  public static Vector sort(Vector in, IndexComparator<? super Vector> cmp) {
    Vector.Builder builder = in.newCopyBuilder();
    Vector tmp = builder.getTemporaryVector();
    QuickSort.quickSort(0, in.size(), (a, b) -> cmp.compare(tmp, a, b), builder);
    return builder.build();
  }

  /**
   * <p>
   * Create a vector of length {@code num} with evenly spaced values between {@code start} and
   * {@code end}.
   * </p>
   * 
   * <p>
   * Returns a vector of {@link org.briljantframework.vector.DoubleVector#TYPE}
   * </p>
   * 
   * 
   * @param start the start value
   * @param stop the end value
   * @param num the number of steps (i.e. intermediate values)
   * @return a vector
   */
  public static Vector linspace(double start, double stop, int num) {
    DoubleVector.Builder builder = new DoubleVector.Builder(0, num);
    double step = (stop - start) / (num - 1);
    double value = start;
    for (int index = 0; index < num; index++) {
      builder.set(index, value);
      value += step;
    }

    return builder.build();
  }

  /**
   * Returns a vector of length {@code 50}. With evenly spaced values in the range {@code start} to
   * {@code end}.
   * 
   * @param start the start value
   * @param stop the end value
   * @return a vector
   */
  public static Vector linspace(double start, double stop) {
    return linspace(start, stop, 50);
  }

  /**
   * <p>
   * Split {@code vector} into {@code chunks}. Handles the case when {@code vector.size()} is not
   * evenly dividable by chunks by making some chunks larger.
   * </p>
   *
   * <p>
   * This implementation is lazy, i.e. chunking is done 'on-the-fly'. To get a list,
   * {@code new ArrayList<>(Vectors.split(vec, 10))}
   * </p>
   * 
   * <p>
   * Ensures that {@code vector.getType()} is preserved.
   * </p>
   * 
   * @param vector the vector
   * @param chunks the number of chunks
   * @return a collection of {@code chunk} chunks
   */
  public static Collection<Vector> split(Vector vector, int chunks) {
    checkArgument(vector.size() >= chunks, "size must be shorter than chunks");
    if (vector.size() == chunks) {
      return Collections.singleton(vector);
    }
    int bin = vector.size() / chunks;
    int remainder = vector.size() % chunks;

    return new AbstractCollection<Vector>() {
      @Override
      public Iterator<Vector> iterator() {
        return new UnmodifiableIterator<Vector>() {
          private int current = 0;
          private int remainders = 0;

          @Override
          public boolean hasNext() {
            return current < vector.size();
          }

          @Override
          public Vector next() {
            int binSize = bin;
            if (remainders < remainder) {
              remainders++;
              binSize += 1;
            }
            Vector.Builder builder = vector.newBuilder();
            for (int i = 0; i < binSize; i++) {
              builder.add(vector, current++);
            }
            return builder.build();
          }
        };
      }

      @Override
      public int size() {
        return chunks;
      }
    };
  }

  /**
   * @param vector the vector
   * @return the standard deviation
   */
  public static double std(Vector vector) {
    return std(vector, mean(vector));
  }

  /**
   * @param vector the vector
   * @param mean the mean
   * @return the standard deviation
   */
  public static double std(Vector vector, double mean) {
    double var = var(vector, mean);
    return Is.NA(var) ? DoubleVector.NA : Math.sqrt(var);
  }

  /**
   * @param vector the vector
   * @return the mean; or NA
   */
  public static double mean(Vector vector) {
    double mean = 0;
    int nonNA = 0;
    for (int i = 0; i < vector.size(); i++) {
      if (!vector.isNA(i)) {
        mean += vector.getAsDouble(i);
        nonNA += 1;
      }
    }

    return nonNA == 0 ? DoubleVector.NA : mean / (double) nonNA;
  }

  /**
   * @param vector the vector
   * @param mean the mean
   * @return the variance; or NA
   */
  public static double var(Vector vector, double mean) {
    double var = 0;
    int nonNA = 0;
    for (int i = 0; i < vector.size(); i++) {
      if (!vector.isNA(i)) {
        double residual = vector.getAsDouble(i) - mean;
        var += residual * residual;
        nonNA += 1;
      }
    }
    return nonNA == 0 ? DoubleVector.NA : var / (double) nonNA;
  }

  /**
   * @param vector the vector
   * @return the variance
   */
  public static double var(Vector vector) {
    return var(vector, mean(vector));
  }

  /**
   * @param vector the vector
   * @return the indexes of {@code vector} sorted in increasing order by value
   */
  public static int[] sortIndex(Vector vector) {
    return sortIndex(vector,
        (o1, o2) -> Double.compare(vector.getAsDouble(o1), vector.getAsDouble(o2)));
  }

  /**
   * @param vector the vector
   * @param comparator the comparator
   * @return the indexes of {@code vector} sorted according to {@code comparator} by value
   */
  public static int[] sortIndex(Vector vector, Comparator<Integer> comparator) {
    int[] indicies = new int[vector.size()];
    for (int i = 0; i < indicies.length; i++) {
      indicies[i] = i;
    }
    List<Integer> tempList = Ints.asList(indicies);
    Collections.sort(tempList, comparator);
    return indicies;
  }

  /**
   * Inner product, i.e. the dot product x * y
   *
   * @param x a vector
   * @param y a vector
   * @return the dot product
   */
  public static double dot(Vector x, Vector y) {
    return dot(x, 1, y, 1);
  }

  /**
   * Take the inner product of two vectors (m x 1) and (1 x m) scaling them by alpha and beta
   * respectively
   *
   * @param x a row vector
   * @param alpha scaling factor for a
   * @param y a column vector
   * @param beta scaling factor for y
   * @return the inner product
   */
  public static double dot(Vector x, double alpha, Vector y, double beta) {
    org.briljantframework.Check.size(x, y);
    int size = y.size();
    double dot = 0;
    for (int i = 0; i < size; i++) {
      dot += (alpha * x.getAsDouble(i)) * (beta * y.getAsDouble(i));
    }
    return dot;
  }

  /**
   * Compute the sigmoid between a and b, i.e. 1/(1+e^(a'-b))
   *
   * @param a a vector
   * @param b a vector
   * @return the sigmoid
   */
  public static double sigmoid(Vector a, Vector b) {
    return 1.0 / (1 + Math.exp(dot(a, 1, b, -1)));
  }

  /**
   * @param vector the vector
   * @return the sum
   */
  public static double sum(Vector vector) {
    double sum = 0;
    for (int i = 0; i < vector.size(); i++) {
      sum += vector.getAsDouble(i);
    }
    return sum;
  }

  public static Vector unique(Vector... vectors) {
    vectors = checkNotNull(vectors);
    checkArgument(vectors.length > 0);
    Vector.Builder builder = vectors[0].newBuilder();
    Set<Value> taken = new HashSet<>();
    for (Vector vector : vectors) {
      for (int i = 0; i < vector.size(); i++) {
        Value value = vector.getAsValue(i);
        if (!taken.contains(value)) {
          taken.add(value);
          builder.add(vector, i);
        }
      }
    }
    return builder.build();
  }
}
