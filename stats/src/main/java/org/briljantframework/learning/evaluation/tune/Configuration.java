/*
 * ADEB - machine learning pipelines made easy
 * Copyright (C) 2014  Isak Karlsson
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.briljantframework.learning.evaluation.tune;

import com.google.common.base.Strings;
import org.briljantframework.learning.Classifier;
import org.briljantframework.learning.evaluation.result.ConfusionMatrix;
import org.briljantframework.learning.evaluation.result.Metric;
import org.briljantframework.learning.evaluation.result.Result;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

/**
 * The type Optimization result.
 *
 * @param <C> the type parameter
 */
public class Configuration<C extends Classifier> implements
        Comparable<Configuration<C>> {
    private final C classifier;
    private final Result result;

    private final Map<String, Object> parameters;

    /**
     * Instantiates a new Optimization result.
     *
     * @param classifier the classifier
     * @param result     the error
     * @param parameters the parameters
     */
    public Configuration(C classifier, Result result, Map<String, Object> parameters) {
        this.classifier = classifier;
        this.result = result;
        this.parameters = parameters;
    }

    /**
     * Metric comparator.
     *
     * @param <C>    the type parameter
     * @param <T>    the type parameter
     * @param metric the metric
     * @return the comparator
     */
    public static <C extends Classifier, T extends Metric> Comparator<Configuration<C>> metricComparator(
            Class<T> metric) {
        return (o1, o2) -> o1.getMetric(metric).compareTo(o2.getMetric(metric));
    }

    /**
     * Create optimization result.
     *
     * @param <C>        the type parameter
     * @param classifier the classifier
     * @param error      the error
     * @param map        the map
     * @return the optimization result
     */
    public static <C extends Classifier> Configuration<C> create(C classifier, Result error, Map<String, Object> map) {
        return new Configuration<>(classifier, error, map);
    }

    /**
     * Gets classifier.
     *
     * @return the classifier
     */
    public C getClassifier() {
        return classifier;
    }

    /**
     * Gets error.
     *
     * @return the error
     */
    public double getError() {
        return result.getAverageError();
    }

    /**
     * Gets result.
     *
     * @return the result
     */
    public Result getResult() {
        return result;
    }

    /**
     * Gets average confusion matrix.
     *
     * @return the average confusion matrix
     */
    public ConfusionMatrix getAverageConfusionMatrix() {
        return result.getAverageConfusionMatrix();
    }

    /**
     * Gets average.
     *
     * @param key the key
     * @return the average
     */
    public double getAverage(Class<? extends Metric> key) {
        return result.getAverage(key);
    }

    /**
     * Gets standard deviation.
     *
     * @param key the key
     * @return the standard deviation
     */
    public double getStandardDeviation(Class<? extends Metric> key) {
        return result.getStandardDeviation(key);
    }

    /**
     * Get t.
     *
     * @param key the key
     * @return the t
     */
    public <T extends Metric> T getMetric(Class<T> key) {
        return result.get(key);
    }

    /**
     * Get object.
     *
     * @param key the key
     * @return the object
     */
    public Object get(String key) {
        return parameters.get(key);
    }

    /**
     * Keys set.
     *
     * @return the set
     */
    public Set<String> keys() {
        return parameters.keySet();
    }

    /**
     * Values collection.
     *
     * @return the collection
     */
    public Collection<Object> values() {
        return parameters.values();
    }

    /**
     * Gets parameters.
     *
     * @return the parameters
     */
    public Set<Map.Entry<String, Object>> entries() {
        return parameters.entrySet();
    }

    @Override
    public int compareTo(Configuration<C> o) {
        return Double.compare(getError(), o.getError());
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(getClassifier().toString()).append("\n\n");
        int longestParameter = entries().stream().mapToInt(x -> x.getKey().length()).max().getAsInt();
        if (longestParameter < 9) {
            longestParameter = 9;
        }

        out.append("Settings\n")
                .append("Parameter")
                .append(Strings.repeat(" ", longestParameter > 12 ? longestParameter - 9 : 4))
                .append("Value\n");
        for (Map.Entry<String, Object> kv : entries()) {
            out.append("")
                    .append(kv.getKey())
                    .append(Strings.repeat(" ", (longestParameter - kv.getKey().length()) + 4))
                    .append(kv.getValue())
                    .append("\n");
        }
        out.append("\n");
        out.append(getResult());
        return out.toString();
//        return String.format("Configuration(%s, %.2f, %s)", classifier, result.getAverageError(), parameters);
    }
}
