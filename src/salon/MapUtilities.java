package salon;

import java.util.Comparator;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/** Utility class providing various operations on maps. */
public final class MapUtilities {

  /**
   * Finds the first value in the map that matches the given condition.
   *
   * @param condition the condition to be tested on map values
   * @param map the map from which to find the value
   * @param <V> the type of values in the map
   * @param <K> the type of keys in the map
   * @return the first value that matches the condition, or null if none found
   */
  public static <V, K> V find(Predicate<V> condition, Map<K, V> map) {
    for (V value : map.values()) {
      if (condition.test(value)) {
        return value;
      }
    }
    return null;
  }

  /**
   * Aggregates the values in the map by grouping and applying an action.
   *
   * @param map the map to aggregate
   * @param startValue the initial value for the aggregation
   * @param getGroupByKey function to extract the group-by key from the map values
   * @param action the action to apply for the aggregation
   * @param <GK> the type of the group-by key
   * @param <K> the type of keys in the map
   * @param <S> the type of values in the map
   * @param <R> the type of the result of the aggregation
   * @return an Aggregate object containing the grouped results
   */
  public static <GK extends Comparable<? super GK>, K extends Comparable<? super K>, S, R>
      Aggregate<GK, S, R> aggregate(
          Map<K, S> map,
          R startValue,
          BiFunction<S, Integer, GK> getGroupByKey,
          BiFunction<R, S, R> action) {
    return aggregate(map, startValue, getGroupByKey, (a, b, c) -> action.apply(a, b));
  }

  /**
   * Aggregates the values in the map by grouping and applying an action.
   *
   * @param map the map to aggregate
   * @param startValue the initial value for the aggregation
   * @param getGroupByKey function to extract the group-by key from the map values
   * @param action the action to apply for the aggregation, including the current state of the
   *     aggregate
   * @param <GK> the type of the group-by key
   * @param <K> the type of keys in the map
   * @param <S> the type of values in the map
   * @param <R> the type of the result of the aggregation
   * @return an Aggregate object containing the grouped results
   */
  public static <GK extends Comparable<? super GK>, K extends Comparable<? super K>, S, R>
      Aggregate<GK, S, R> aggregate(
          Map<K, S> map,
          R startValue,
          BiFunction<S, Integer, GK> getGroupByKey,
          TriFunction<R, S, Aggregate<GK, S, R>, R> action) {
    Aggregate<GK, S, R> result = new Aggregate<>(startValue, getGroupByKey, action);
    for (S value : map.values()) {
      result.put(value);
    }
    return result;
  }

  /**
   * Sorts the map based on a key extracted from its entries and returns an IndexTree.
   *
   * @param map the map to be sorted
   * @param keyExtractor function to extract the sort key from map entries
   * @param <MK> the type of keys in the map
   * @param <KK> the type of keys in the IndexTree
   * @param <S> the type of values in the map
   * @return an IndexTree sorted based on the extracted keys
   */
  public static <MK extends Comparable<? super MK>, KK extends Comparable<? super KK>, S>
      IndexTree<MK, KK, S> sort(Map<MK, S> map, Function<Map.Entry<MK, S>, KK> keyExtractor) {
    return sort(map, keyExtractor, Comparator.naturalOrder());
  }

  /**
   * Sorts the map based on a key extracted from its entries and a custom comparator, returning an
   * IndexTree.
   *
   * @param map the map to be sorted
   * @param keyExtractor function to extract the sort key from map entries
   * @param comparator the comparator to determine the order of the keys
   * @param <MK> the type of keys in the map
   * @param <KK> the type of keys in the IndexTree
   * @param <S> the type of values in the map
   * @return an IndexTree sorted based on the extracted keys
   */
  public static <MK extends Comparable<? super MK>, KK extends Comparable<? super KK>, S>
      IndexTree<MK, KK, S> sort(
          Map<MK, S> map,
          Function<Map.Entry<MK, S>, KK> keyExtractor,
          Comparator<? super String> comparator) {
    IndexTree<MK, KK, S> index = new IndexTree<MK, KK, S>(keyExtractor, comparator);
    for (Map.Entry<MK, S> entry : map.entrySet()) {
      String sortKey = index.getKeyExtractor().apply(entry);
      index.put(sortKey, entry.getValue());
    }
    return index;
  }
}
